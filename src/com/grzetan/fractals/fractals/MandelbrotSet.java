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
    BufferedImage image;
    Thread thread;

    double xmin = -2.5;
    double xmax = 2;
    double ymin = -2;
    double ymax = 2;
    int maxIterations = 80;

    double mouseX;
    double mouseY;
    int followMouse = -1;
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
        image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
        mandelbrot(image);
        g.drawImage(image,0,0,this);
    }

    private static int makeRGBColor(int red, int green, int blue)
    {
        int rgb = 0;
        rgb = red*65536 + green*256 + blue;
        return rgb;
    }

    public void mandelbrot(BufferedImage img){
        for(int y=0;y<HEIGHT;y++){
            for(int x=0;x<WIDTH;x++){
                /*
                Formula
                    a = real component (x axis)
                    b = imaginary component (y axis)
                    i = sqrt(-1)
                    c = a + bi
                    z starts as 0 and increment as long as z < maxIterations
                    With each iteration z becomes z from previous iteration

                    First iteration:
                    z=0
                    F(0) = 0^2 + c
                    Second iteration:
                    z = c
                    F(c) = c^2 + c
                    Third iteration:
                    z = c^2 + c
                    F(c^2+c) = (c^2+c)^2 + c
                    So we need to know if z^2 goes to infinity with each iteration
                    or rather stays relatively close to some number (doesn't grow).
                    z^2 = (a + bi) * (a + bi) =
                    a^2 + abi + abi + bi^2 =
                    (i is a square root of -1 so if we square it we get -1)
                    a^2 + 2abi - b^2 = a^2 - b^2 + 2abi
                    Each iteration we need to calculate a^-b^2 and 2ab
                */
                double originalA = x/(double) WIDTH * Math.abs(xmax - xmin) + xmin;
                double originalB = y/(double) HEIGHT * Math.abs(ymax - ymin) + ymin;
                double a = originalA;
                double b = originalB;
                double i = 0;

                while(i<maxIterations){
                    //Calculate z^2
                    double realComponent = a*a - b*b;
                    double imaginaryComponent = 2*a*b;
                    a = realComponent + originalA;
                    b = imaginaryComponent + originalB;
                    //If z is too high (goes to infinity), break out
                    if(a+b > 5){
                        break;
                    }
                    i++;
                }

                double brightness2 = (i/maxIterations);
                int brightness = (int) (Math.sqrt(brightness2) / (double) 1 * 255);
                if(i == maxIterations){
                    brightness = 0;
                }
                img.setRGB(x,y,makeRGBColor(brightness,brightness,brightness));
            }
        }
    }

    public void run(){
        long now;
        long waitTime;
        long refreshTime;
        final int TARGET_FPS = 20;
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

        //follow mouse
        AbstractAction followMouseAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                followMouse *= -1;
            }
        };
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK),"FOLLOW_MOUSE");
        this.getActionMap().put("FOLLOW_MOUSE", followMouseAction);
    }

    public class ML implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent mouseEvent) {

        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            mousePressed = true;
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
            if(followMouse > 0){
                maxIterations = (int) (mouseX / WIDTH *  100 + 2);
            }
        }
    }

    public class SA implements MouseWheelListener{

        @Override
        public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
            if(mouseWheelEvent.getWheelRotation() < 0){

            }
            if(mouseWheelEvent.getWheelRotation() > 0){
            }
        }
    }
}
