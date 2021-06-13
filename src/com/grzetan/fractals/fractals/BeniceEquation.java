package com.grzetan.fractals.fractals;

import com.grzetan.fractals.FractalFrame;
import com.grzetan.fractals.jzoom.JZoom;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class BeniceEquation extends JPanel{

    final int WIDTH = 1000;
    final int HEIGHT = 800;

    Thread thread;
    FractalFrame frame;

    JZoom jZoom = new JZoom(WIDTH,HEIGHT);
    Circle[] circles;
    ArrayList<double[]> path = new ArrayList<>();

    boolean firstFrame = true;

    String helpMsg = "HELP\nCTRL+H - Show help\nCTRL+S - Take screenshot\nCTRL+F - Follow last circle\nCTRL+R - Randomize\nESCAPE - go back to menu";

    boolean followLastCircle = false;
    int inside = 1;
    double shrinkRatio = 3;
    int numOfCircles = 10;
    double k = -4;

    public BeniceEquation(FractalFrame frame){
        this.frame = frame;
        this.setFocusable(true);
        this.setBackground(Color.BLACK);
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));

        jZoom.installMouseAdapter(this);
        jZoom.setBackground(Color.BLACK);
        jZoom.allowDragging(false);
        jZoom.allowZooming(false);
        initKeyBindings();
        initCircles();

        thread = new Thread(this::run);
        thread.start();
    }

    public void initCircles(){
        Circle[] circs = new Circle[numOfCircles];
        Circle c1 = new Circle(150, null,0);
        circs[0] = c1;

        for(int i=1;i<numOfCircles;i++){
            double newR = circs[i-1].r / shrinkRatio;
            Circle c = new Circle(newR, circs[i-1],i);
            circs[i-1].setChild(c);
            circs[i] = c;
        }

        this.circles = circs;
    }

    public void draw(){
        for(Circle circle : circles){
            circle.draw();
        }

        if(path.size() > 2){
            for(int i=2;i<path.size();i++){
                jZoom.drawLine(path.get(i - 1)[0], path.get(i - 1)[1], path.get(i)[0], path.get(i)[1],Color.RED);
            }
        }
    }

    public void move(){
        for(Circle circle : circles){
            circle.move();
        }
        double[] pos = circles[circles.length - 1].getCurrentPos();
        path.add(pos);
        if(followLastCircle){
            jZoom.followPoint(pos[0], pos[1], 2);
        }
    }

    public void paint(Graphics g){
        draw();
        jZoom.render(g,0,0,this);
    }

    public void run(){
        long now;
        long waitTime;
        long updateTime;
        long TARGET_FPS = 30;
        long PREFERED_TIME = 1_000_000_000 / TARGET_FPS;

        while(true){
            now = System.nanoTime();

            move();
            repaint();

            updateTime = now - System.nanoTime();
            waitTime = (PREFERED_TIME - updateTime) / 1_000_000;
            try {
                thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(firstFrame){
                JOptionPane.showMessageDialog(null, helpMsg);
                firstFrame = false;
            }
        }
    }

    public double getRandomNumber(double min, double max) {
        return (double) ((Math.random() * (max - min)) + min);
    }

    public void takeSS(){
        BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.paint(img.getGraphics());
        //Make filename
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String path = "Sierpinski_carpet-"+formatter.format(date)+".png";

        File outputfile = new File(path);
        try {
            ImageIO.write(img, "png", outputfile);
            JOptionPane.showMessageDialog(null, "Screenshot saved as "+path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initKeyBindings(){
        //ESCAPE
        AbstractAction a = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                thread.stop();
                frame.change("menu");
            }
        };
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
        this.getActionMap().put("ESCAPE", a);

        //Take SS
        Action takeSSAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                takeSS();
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK), "SS");
        this.getActionMap().put("SS", takeSSAction);

        //Follow last circle
        Action followAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(followLastCircle){
                    followLastCircle = false;
                    jZoom.allowDragging(true);
                    jZoom.allowZooming(true);
                }else{
                    followLastCircle = true;
                    jZoom.allowDragging(false);
                    jZoom.allowZooming(false);
                }
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK), "FOLLOW");
        this.getActionMap().put("FOLLOW", followAction);

        //Randomize
        Action randomize = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                k = (int) getRandomNumber(-6,0);
                numOfCircles = (int) getRandomNumber(3,20);
                inside = (getRandomNumber(0,1) > 0.5) ? 1 : -1;
                shrinkRatio = (int) getRandomNumber(2,6);
                path = new ArrayList<>();
                circles = new Circle[]{};
                initCircles();
            }
        };
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('R', InputEvent.CTRL_DOWN_MASK), "RANDOMIZE");
        this.getActionMap().put("RANDOMIZE", randomize);

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

    class Circle{
        double r;
        double x;
        double y;
        Circle parent;
        Circle child;
        Double speed;
        double angle = 0;

        public Circle(double r, Circle parent, int level) {
            this.r = r;
            this.parent = parent;
            this.speed = Math.toRadians(Math.pow(k,level-1));
            if(parent == null){
                this.x = WIDTH/2;
                this.y = HEIGHT/2;
            }
        }

        public void move(){
            if(speed != null){
                angle += speed;
            }
        }

        public void draw(){
            if(parent != null){
                x = parent.x + (parent.r + inside*r) * Math.sin(angle);
                y = parent.y + (parent.r + inside*r) * Math.cos(angle);
            }
            jZoom.drawOval(x-r,y-r,r*2,r*2,Color.WHITE);
        }

        public void setChild(Circle child) {
            this.child = child;
        }

        public double[] getCurrentPos(){
            return new double[]{x,y};
        }
    }
}
