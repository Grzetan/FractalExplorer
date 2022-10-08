package com.grzetan.fractals.fractals;

import com.grzetan.fractals.FractalFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JuliaSet extends JPanel{
    final int WIDTH = 1920;
    final int HEIGHT = 1080;

    FractalFrame frame;
    BufferedImage image;
    Thread thread;

    double xmin = -2;
    double xmax = 2;
    double ymin = -2;
    double ymax = 2;
    int maxIterations = 90;
    double[] c = {randomNumber(-0.5,0.5),randomNumber(-0.5,0.5)};

    double epsilon = 0.005;
    boolean firstFrame = true;

    double mouseX;
    double mouseY;
    int followMouse = -1;
    double lastPointX;
    double lastPointY;

    String helpMsg = "HELP\nCTRL+H - Show help\nCTRL+Q - Make set follow your mouse\nCTRL+R - Randomize set\nCTRL+S - Take screenshot\nESCAPE - go back to menu";

    public JuliaSet(FractalFrame frame){
        this.frame = frame;
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
        this.setFocusable(true);
        this.setBackground(Color.BLACK);

        this.addMouseWheelListener(new JuliaSet.SA());
        this.addMouseMotionListener(new JuliaSet.MA());
        this.addMouseListener(new JuliaSet.ML());
        this.initKeyBinding();

        thread = new Thread(this::run);
        thread.start();
    }

    public void paintSet(Graphics g){
        image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
        julia(image);
        Graphics g2 = image.getGraphics();
        g.drawImage(image,0,0,this);
    }

    public void julia(BufferedImage img){
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
                double [] z = {originalA,originalB};
                double i = 0;

                while(i<maxIterations){
                    //Calculate z^2
                    z = new double[]{z[0]*z[0] - z[1]*z[1] + c[0], 2*z[0]*z[1] + c[1]};
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
                    Color c = new Color((int) (i/maxIterations*255), (int) (i/maxIterations*255) ,(int) (i/maxIterations*255));
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

            System.out.println(this.c[0] + ", " + this.c[1]);

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

    public double randomNumber(double min, double max){
        return Math.random() * Math.abs(max-min) + min;
    }

    public void takeSS(){
        BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.paintSet(img.getGraphics());
        //Make filename
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String path = "julia_set-"+formatter.format(date)+".png";

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

        //randomize
        AbstractAction randomizeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                c = new double[] {randomNumber(-0.5,0.5), randomNumber(-0.5,0.5)};
            }
        };
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('R', InputEvent.CTRL_DOWN_MASK),"RANDOMIZE");
        this.getActionMap().put("RANDOMIZE", randomizeAction);

        //add to cX
        Action addtoXAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                c[0] += epsilon;
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "ADD_TO_X");
        this.getActionMap().put("ADD_TO_X", addtoXAction);

        //subtract from cX
        Action subtractFromXAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                c[0] -= epsilon;
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "SUBTRACT_FROM_X");
        this.getActionMap().put("SUBTRACT_FROM_X", subtractFromXAction);

        //add to cY
        Action addtoYAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                c[1] += epsilon;
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "ADD_TO_Y");
        this.getActionMap().put("ADD_TO_Y", addtoYAction);

        //subtract from cX
        Action subtractFromYAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                c[1] -= epsilon;
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "SUBTRACT_FROM_Y");
        this.getActionMap().put("SUBTRACT_FROM_Y", subtractFromYAction);

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

    public class ML extends MouseAdapter {
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
                double originalA = mouseX/(double) WIDTH * Math.abs(xmax - xmin) + xmin;
                double originalB = mouseY/(double) HEIGHT * Math.abs(ymax - ymin) + ymin;
                c = new double[] {originalA,originalB};
            }
        }
    }

    public class SA implements MouseWheelListener {
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
