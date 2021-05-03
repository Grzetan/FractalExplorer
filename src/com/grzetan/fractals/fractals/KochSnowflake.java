package com.grzetan.fractals.fractals;

import com.grzetan.fractals.FractalFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KochSnowflake extends JPanel{
    final int WIDTH = 1000;
    final int HEIGHT = 800;

    Thread thread;
    Graphics graphics;
    Image image;

    int limit = 4;
    int followMouse = -1;
    boolean firstFrame = true;

    //Zoom
    AffineTransform a;
    double zoom = 1;
    double prevZoom = 1;
    double xOffset = 0;
    double yOffset = 0;
    int mouseX;
    int mouseY;

    //Dragging
    boolean mousePressed = false;
    double lastPointX;
    double lastPointY;

    FractalFrame frame;

    String helpMsg = "HELP\nCTRL+H - Show help\nCTRL+Q - Make snowflake follow your mouse\nCTRL+S - Take screenshot\nESCAPE - go back to menu";

    public KochSnowflake(FractalFrame frame){
        this.frame = frame;
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
        this.setFocusable(true);
        this.setLayout(null);
        this.setBackground(Color.BLACK);
        this.addMouseMotionListener(new MA());
        this.addMouseListener(new ML());
        this.addMouseWheelListener(new SA());

        initKeyBindings();

        //Start game
        thread = new Thread(this::run);
        thread.start();
    }

    public void paintSnowflake(Graphics g){
        a = new AffineTransform();
        Graphics2D g2 = (Graphics2D) g;

        double zoomDiv = zoom / prevZoom;
        xOffset = zoomDiv * xOffset + (1-zoomDiv) * mouseX;
        yOffset = zoomDiv * yOffset + (1-zoomDiv) * mouseY;

        if(mousePressed){
            double draggedX = lastPointX - mouseX;
            double draggedY = lastPointY - mouseY;
            xOffset -= draggedX;
            yOffset -= draggedY;
            lastPointX = mouseX;
            lastPointY = mouseY;
        }

        //Prevents generating starting point of image in frame
        if(xOffset > 0){
            xOffset = 0;
        }
        if(yOffset > 0){
            yOffset = 0;
        }
        if(xOffset < -zoom*WIDTH+WIDTH){
            xOffset = -zoom*WIDTH+WIDTH;
        }
        if(yOffset < -zoom*HEIGHT+HEIGHT){
            yOffset = -zoom*HEIGHT+HEIGHT;
        }

        a.translate(xOffset,yOffset);
        a.scale(zoom,zoom);
        prevZoom = zoom;
        g2.transform(a);

        image = createImage(WIDTH,HEIGHT);
        graphics = image.getGraphics();
        //Make black bg color
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0,0,image.getWidth(this), image.getHeight(this));
        draw(graphics);
        g2.drawImage(image, 0,0,this);
    }

    public void draw(Graphics g){
        g.setColor(Color.WHITE);
        //Bottom side
        kochCurve(WIDTH-200,HEIGHT-200,WIDTH-400, -Math.PI / 2,limit,g);
        //Left side
        kochCurve(200,HEIGHT-200,WIDTH-400, 5*Math.PI / 6,limit,g);
        //Right side
        int x = (int) (200 + (WIDTH-400) * Math.sin(5*Math.PI / 6));
        int y = (int) (HEIGHT-200 + (WIDTH-400) * Math.cos(5*Math.PI / 6));
        kochCurve(x,y,WIDTH-400, Math.PI / 6,limit,g);
    }

    public void run(){
        long now;
        long updateTime;
        long wait;

        final int TARGET_FPS = 30;
        final long OPTIMAL_TIME = 1_000_000_000 / TARGET_FPS;

        while (true){
            now = System.nanoTime();

            paintSnowflake(this.getGraphics());

            updateTime = now - System.nanoTime();

            wait = (OPTIMAL_TIME - updateTime) / 1_000_000;
            try{
                thread.sleep(wait);
            }catch(Exception e){
                e.printStackTrace();
            }

            if(firstFrame){
                firstFrame = false;
                JOptionPane.showMessageDialog(null, helpMsg);
            }
        }
    }

    public void takeSS(){
        BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.paintSnowflake(img.getGraphics());
        //Make filename
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String path = "Koch_Snowflake-"+formatter.format(date)+".png";

        File outputfile = new File(path);
        try {
            ImageIO.write(img, "png", outputfile);
            JOptionPane.showMessageDialog(null, "Screenshot saved as "+path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void kochCurve(int x,int y,int len, double angle, int limit, Graphics g){
        if(limit == 0){
            int x2 = (int) (x + len * Math.sin(angle));
            int y2 = (int) (y + len * Math.cos(angle));
            g.drawLine(x,y,x2,y2);
            return;
        }
        int newLen = len / 3;

        //First segment
        kochCurve(x,y, newLen, angle, limit-1, g);
        //Second segment
        int x2 = (int) (x + newLen * Math.sin(angle));
        int y2 = (int) (y + newLen * Math.cos(angle));
        kochCurve(x2,y2,newLen,angle+Math.PI / 3, limit-1,g);
        //Third segment
        int x3 = (int) (x2 + newLen * Math.sin(angle+Math.PI / 3));
        int y3 = (int) (y2 + newLen * Math.cos(angle+Math.PI / 3));
        kochCurve(x3,y3,newLen, angle-Math.PI / 3, limit-1,g);
        //Fourth segment
        int x4 = (int) (x + newLen*2 * Math.sin(angle));
        int y4 = (int) (y + newLen*2 * Math.cos(angle));
        kochCurve(x4,y4,newLen,angle, limit-1,g);
    }

    public void moveSnowflake(MouseEvent e){
        int x = e.getX();

        limit = (int) ((x/(double) WIDTH) * 7);
    }

    public void initKeyBindings(){
        //Go back to menu
        Action goBackToMenuAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                thread.stop();
                frame.change("menu");
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "MENU");
        this.getActionMap().put("MENU", goBackToMenuAction);

        //Make snowflake follow your mouse
        Action followMouseAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                followMouse *= -1;
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK), "FOLLOW");
        this.getActionMap().put("FOLLOW", followMouseAction);

        //Take SS
        Action takeSSAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                takeSS();
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK), "SS");
        this.getActionMap().put("SS", takeSSAction);

        //Help
        Action helpAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JOptionPane.showMessageDialog(null, helpMsg);
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('H', InputEvent.CTRL_DOWN_MASK), "HELP");
        this.getActionMap().put("HELP", helpAction);
    }

    public class ML implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent mouseEvent) {

        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            mousePressed = true;
            lastPointX = mouseEvent.getX();
            lastPointY = mouseEvent.getY();
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            mousePressed = false;
        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {

        }
    }

    public class MA extends MouseAdapter{

        @Override
        public void mouseDragged(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();

            if(followMouse > 0) {
                moveSnowflake(e);
            }
        }
    }

    public class SA implements MouseWheelListener{

        @Override
        public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
            if(mouseWheelEvent.getWheelRotation() < 0 && zoom < 5){
                zoom *= 1.05;
            }

            if(mouseWheelEvent.getWheelRotation() > 0 && zoom > 1){
                zoom /= 1.05;
            }
        }
    }

}
