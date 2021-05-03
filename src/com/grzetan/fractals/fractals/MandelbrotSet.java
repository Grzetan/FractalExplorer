package com.grzetan.fractals.fractals;

import com.grzetan.fractals.FractalFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
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

    public MandelbrotSet(FractalFrame frame){
        this.frame = frame;
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
        this.setFocusable(true);
        this.setBackground(Color.BLACK);

        this.initKeyBinding();

        thread = new Thread(this::run);
        thread.start();
    }

    public void paintSet(Graphics g){
        image = createImage(WIDTH,HEIGHT);
        graphics = image.getGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0,0,WIDTH,HEIGHT);
        draw(graphics);
        g.drawImage(image,0,0,this);
    }

    public void draw(Graphics g){
        g.setColor(Color.WHITE);
        g.fillRect(0,0,100,100);
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

}
