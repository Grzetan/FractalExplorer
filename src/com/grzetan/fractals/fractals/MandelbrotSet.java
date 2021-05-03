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

public class MandelbrotSet extends JPanel {

    final int WIDTH = 1000;
    final int HEIGHT = 800;

    FractalFrame frame;
    Image image;
    Thread thread;
    Graphics graphics;

    double mouseX;
    double mouseY;

    AffineTransform a;
    double zoom = 1;
    double prevZoom = 1;
    double xOffset = 0;
    double yOffset = 0;
    double lastPointX = 0;
    double lastPointY = 0;
    boolean mousePressed = false;

    public MandelbrotSet(FractalFrame frame){
        this.frame = frame;
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
        this.setFocusable(true);
        this.setBackground(Color.BLACK);

        this.addMouseWheelListener(new SA());
        this.addMouseMotionListener(new MA());
        this.addMouseListener(new ML());

        this.initKeyBinding();

        thread = new Thread(this::run);
        thread.start();
    }

    public void paintSet(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        a = new AffineTransform();

        double zoomDiv = zoom / prevZoom;
        xOffset = zoomDiv * xOffset + (1-zoomDiv) * mouseX;
        yOffset = zoomDiv * yOffset + (1-zoomDiv) * mouseY;

        if(mousePressed){
            xOffset += mouseX - lastPointX;
            yOffset += mouseY - lastPointY;
            lastPointX = mouseX;
            lastPointY = mouseY;
        }

        if(xOffset > 0){
            xOffset = 0;
        }else if(xOffset < -(zoom*WIDTH - WIDTH)){
            xOffset = -(zoom*WIDTH - WIDTH);
        }
        if(yOffset > 0){
            yOffset = 0;
        }else if(yOffset < -(zoom*HEIGHT-HEIGHT)){
            yOffset = -(zoom*HEIGHT-HEIGHT);
        }

        prevZoom = zoom;
        a.translate(xOffset,yOffset);
        a.scale(zoom,zoom);
        g2.transform(a);

        image = createImage(WIDTH,HEIGHT);
        graphics = image.getGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0,0,WIDTH,HEIGHT);
        draw(graphics);
        g2.drawImage(image,0,0,this);
    }

    public void draw(Graphics g){
        g.setColor(Color.WHITE);
        g.fillRect(100,100,800,600);
    }

    public void run(){
        long now;
        long waitTime;
        long refreshTime;
        final int TARGET_FPS = 30;
        final long OPTIMAL_TIME = 1_000_000_000 / TARGET_FPS;

        while(true){
            now = System.nanoTime();

            paintSet(this.getGraphics());

            refreshTime = now - System.nanoTime();
            waitTime = (OPTIMAL_TIME - refreshTime) / 1_000_000;
            try {
                thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void takeSS(){
        BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.paintSet(img.getGraphics());
        //Make filename
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String path = "Mandelbrot_set-"+formatter.format(date)+".png";

        File outputfile = new File(path);
        try {
            ImageIO.write(img, "png", outputfile);
            JOptionPane.showMessageDialog(null, "Screenshot saved as "+path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initKeyBinding(){
        //ESCAPE
        AbstractAction goBack = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                thread.stop();
                frame.change("menu");
            }
        };
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
        this.getActionMap().put("ESCAPE", goBack);

        //take SS
        AbstractAction takeSS = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                takeSS();
            }
        };
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK), "TAKE_SS");
        this.getActionMap().put("TAKE_SS", takeSS);
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
