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

public class FractalTree extends JPanel {

    final int WIDTH = 1000;
    final int HEIGHT = 800;

    FractalFrame frame;
    Thread thread;
    Graphics graphics;
    Image image;

    double zoom = 1;
    double prevZoom = 1;
    int mouseX;
    int mouseY;
    double xOffset;
    double yOffset;
    AffineTransform a;

    JMenu menu;
    JMenuBar menuBar;
    JMenuItem menuItem;

    public int followMouse = -1;
    public int randomizeTree = -1;

    double THETA = Math.PI / 6;
    float TRUNK_RADIO = 0.67F;
    int LIMIT = 12;

    String helpMsg = "HELP\nCTRL+H - Show help\nCTRL+R - Randomize tree\nCTRL+Q - Make tree follow your mouse\nCTRL+S - Take screenshot\nESCAPE - go back to menu";

    public FractalTree(FractalFrame frame){
        this.frame = frame;
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
        this.setFocusable(true);
        this.setBackground(Color.BLACK);

        this.addMouseMotionListener(new MA());
        this.addMouseWheelListener(new SA());
        initKeyBindings();

        //Start game
        thread = new Thread(this::run);
        thread.start();
    }

    public void paintTree(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        a = new AffineTransform();

        double zoomDiv = zoom / prevZoom;
        xOffset = zoomDiv * xOffset + (1-zoomDiv) * mouseX;
        yOffset = zoomDiv * yOffset + (1-zoomDiv) * mouseY;

        a.translate(xOffset,yOffset);
        a.scale(zoom,zoom);
        prevZoom = zoom;
        g2.transform(a);

        if(randomizeTree == 1){
            randomizeTree++;
        }

        image = createImage(WIDTH,HEIGHT);
        graphics = image.getGraphics();
        //Make black bg color
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0,0,image.getWidth(this), image.getHeight(this));
        draw(graphics);
        g2.drawImage(image, 0,0,this);
    }

    public void moveTree(MouseEvent e){
        int x = e.getX();
        int y = e.getY();

        THETA = ((x/(double)WIDTH) * Math.PI);
        TRUNK_RADIO = (float) ((y/(double)HEIGHT) * (0.9-0.4) + 0.4);
    }

    public void branch(int x,int y, double angle, int len, int limit,Graphics g){
        if(limit <= 0){
            return;
        }

        int x2 = (int) (x - len * Math.sin(angle));
        int y2 = (int) (y - len * Math.cos(angle));
        g.drawLine(x,y,x2,y2);

        if(randomizeTree > 0){
            int numberOfBranches = (int) getRandomNumber(1,5);
            for(int i = 0; i<numberOfBranches; i++){
                double randTheta = getRandomNumber(-Math.PI / 4 ,Math.PI / 4);
                double randTrunkRadio = getRandomNumber(0.4,0.9);
                branch(x2,y2,angle+randTheta, (int) (len*randTrunkRadio), limit-1,g);
            }
        }else{
            branch(x2,y2,angle+THETA, (int) (len*TRUNK_RADIO), limit-1,g);
            branch(x2,y2,angle-THETA, (int) (len*TRUNK_RADIO), limit-1,g);
        }
    }

    public void draw(Graphics g){
        g.setColor(Color.WHITE);
        branch(WIDTH/2,HEIGHT, 0,200,LIMIT,g);
    }

    public void run(){
        long now;
        long updateTime;
        long wait;

        final int TARGET_FPS = 20;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

        boolean firstFrame = true;

        while (true){
            now = System.nanoTime();

            if(randomizeTree != 2) {
                paintTree(this.getGraphics());
            }

            updateTime = now - System.nanoTime();

            wait = (OPTIMAL_TIME - updateTime) / 1000000;
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
        this.paintTree(img.getGraphics());
        //Make filename
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String path = "FractalTree-"+formatter.format(date)+".png";

        File outputfile = new File(path);
        try {
            ImageIO.write(img, "png", outputfile);
            JOptionPane.showMessageDialog(null, "Screenshot saved as "+path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initKeyBindings(){
        //Make tree follow your mouse
        Action followMouseAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                followMouse *= -1;
                randomizeTree = -1;
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK), "FOLLOW");
        this.getActionMap().put("FOLLOW", followMouseAction);

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

        //Take SS
        Action takeSSAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                takeSS();
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK), "SS");
        this.getActionMap().put("SS", takeSSAction);

        //Randomize tree
        Action randomizeTreeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(randomizeTree == -1){
                    followMouse = -1;
                    randomizeTree = 1;
                }else if(randomizeTree == 2){
                    randomizeTree = 1;
                }
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('R',InputEvent.CTRL_DOWN_MASK),"RANDOMIZE");
        this.getActionMap().put("RANDOMIZE", randomizeTreeAction);

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

    public double getRandomNumber(double min, double max) {
        return (double) ((Math.random() * (max - min)) + min);
    }

    public class MA extends MouseAdapter{
        @Override
        public void mouseMoved(MouseEvent event){
            mouseX = event.getX();
            mouseY = event.getY();
            if(followMouse > 0){
                moveTree(event);
            }
        }
    }

    public class SA implements MouseWheelListener{
        @Override
        public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
            if(mouseWheelEvent.getWheelRotation() < 0 && zoom < 5){
                zoom *= 1.03;
            }

            if(mouseWheelEvent.getWheelRotation() > 0 && zoom > 1){
                zoom /= 1.03;
            }
        }
    }

}
