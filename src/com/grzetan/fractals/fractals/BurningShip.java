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

public class BurningShip extends JPanel {

    final int WIDTH = 1920;
    final int HEIGHT = 1080;

    FractalFrame frame;
    BufferedImage image;
    Thread thread;

    double xmin = -4;
    double xmax = 4;
    double ymin = -4;
    double ymax = 4;
    int maxIterations = 80;

    boolean firstFrame = true;

    double mouseX;
    double mouseY;
    int followMouse = -1;
    double lastPointX;
    double lastPointY;

    String helpMsg = "HELP\nCTRL+H - Show help\nCTRL+Q - Change quality by moving your mouse\nCTRL+S - Take screenshot\nESCAPE - go back to menu";

    public BurningShip(FractalFrame frame){
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
        image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
        burningShip(image);
        Graphics g2 = image.getGraphics();
        g.drawImage(image,0,0,this);
    }

    public void burningShip(BufferedImage img){
        for(int y=0;y<HEIGHT;y++){
            for(int x=0;x<WIDTH;x++){
                double originalA = x/(double) WIDTH * Math.abs(xmax - xmin) + xmin;
                double originalB = y/(double) HEIGHT * Math.abs(ymax - ymin) + ymin;
                double [] z = {0,0};
                double i = 0;

                while(i<maxIterations){
                    //Calculate z^2
                    z = new double[]{z[0]*z[0] - z[1]*z[1] + originalA, 2*Math.abs(z[0]*z[1]) + originalB};
                    //If z is too high (goes to infinity), break out
                    if(z[0]*z[0] + z[1]*z[1] > 5){
                        break;
                    }
                    i++;
                }

                if(i == maxIterations){
                    Color c = new Color(0,0,0);
                    img.setRGB(x,y,c.getRGB());
                }else if(i > 1){
                    Color c = new Color((int) (i/maxIterations*255), 80 ,100);
                    img.setRGB(x,y,c.getRGB());
                }else{
                    Color c = new Color(50,50,50);
                    img.setRGB(x,y,c.getRGB());
                }
            }
        }
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
            if(firstFrame){
                firstFrame = false;
                JOptionPane.showMessageDialog(null, helpMsg);
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

        //follow mouse
        AbstractAction followMouseAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                followMouse *= -1;
            }
        };
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK),"FOLLOW_MOUSE");
        this.getActionMap().put("FOLLOW_MOUSE", followMouseAction);

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

    public void dragging(){
        double diffX = lastPointX - mouseX;
        double diffY = lastPointY - mouseY;
        double radioX = diffX / WIDTH;
        double radioY = diffY / HEIGHT;
        double radioOnPlane = Math.abs(xmax-xmin);
        xmin += radioOnPlane*radioX;
        xmax += radioOnPlane*radioX;
        ymin += radioOnPlane*radioY;
        ymax += radioOnPlane*radioY;
        lastPointX = mouseX;
        lastPointY = mouseY;
    }

    public class ML extends MouseAdapter{
        @Override
        public void mousePressed(MouseEvent e) {
            lastPointX = e.getX();
            lastPointY = e.getY();
        }
    }

    public class MA extends MouseAdapter{
        @Override
        public void mouseDragged(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            dragging();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            if(followMouse > 0){
                maxIterations = (int) (mouseX / WIDTH *  200 + 2);
            }
        }
    }

    public class SA implements MouseWheelListener{
        @Override
        public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
            //Zoom in
            double scale = Math.abs(xmax-xmin) * 0.1;
            double msx = mouseX / (double) WIDTH;
            double msy = mouseY / (double) HEIGHT;
            if(mouseWheelEvent.getWheelRotation() < 0){
                xmin += scale * msx;
                xmax -= scale * (1-msx);
                ymin += scale * msy;
                ymax -= scale * (1-msy);
            }
            //Zoom out
            if(mouseWheelEvent.getWheelRotation() > 0){
                if(Math.abs((xmax+scale*msx)-(xmin-scale*(1-msx))) < 4){
                    xmin -= scale * (1-msx);
                    xmax += scale * msx;
                    ymin -= scale * (1-msy);
                    ymax += scale * msy;
                }
            }
        }
    }
}