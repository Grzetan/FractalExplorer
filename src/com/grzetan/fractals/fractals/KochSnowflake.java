package com.grzetan.fractals.fractals;

import com.grzetan.fractals.FractalFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class KochSnowflake extends JPanel{
    final int WIDTH = 1000;
    final int HEIGHT = 800;

    Thread thread;
    Graphics graphics;
    Image image;

    FractalFrame frame;

    public KochSnowflake(FractalFrame frame){
        this.frame = frame;
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
        this.setFocusable(true);
        this.setLayout(null);
        this.setBackground(Color.BLACK);

        initKeyBindings();

        //Start game
        thread = new Thread(this::run);
        thread.start();
    }

    public void paintSnowflake(Graphics g){
        image = createImage(WIDTH,HEIGHT);
        graphics = image.getGraphics();
        //Make black bg color
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0,0,image.getWidth(this), image.getHeight(this));
        draw(graphics);
        g.drawImage(image, 0,0,this);
    }

    public void draw(Graphics g){
        g.setColor(Color.WHITE);
        kochCurve(100,HEIGHT/2,WIDTH-200, 90,4,g);
    }

    public void run(){
        long now;
        long updateTime;
        long wait;

        final int TARGET_FPS = 30;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

        while (true){
            now = System.nanoTime();

            paintSnowflake(this.getGraphics());

            updateTime = now - System.nanoTime();

            wait = (OPTIMAL_TIME - updateTime) / 1000000;
            try{
                thread.sleep(wait);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void kochCurve(int x,int y,int len, int angle, int limit, Graphics g){
        if(limit == 0){
            double inRadians = Math.toRadians(angle);
            int x2 = (int) (x + len * Math.sin(inRadians));
            int y2 = (int) (y + len * Math.cos(inRadians));
            g.drawLine(x,y,x2,y2);
            return;
        }
        int newLen = len / 3;

        double inRadians = Math.toRadians(angle);
        //First segment
        kochCurve(x,y, newLen, angle, limit-1, g);
        //Second segment
        int x2 = (int) (x + newLen * Math.sin(inRadians));
        int y2 = (int) (y + newLen * Math.cos(inRadians));
        kochCurve(x2,y2,newLen,angle+60, limit-1,g);
        //Third segment
        int x3 = (int) (x2 + newLen * Math.sin(inRadians+Math.toRadians(60)));
        int y3 = (int) (y2 + newLen * Math.cos(inRadians+Math.toRadians(60)));
        kochCurve(x3,y3,newLen, angle-60, limit-1,g);
        //Fourth segment
        int x4 = (int) (x + newLen*2 * Math.sin(inRadians));
        int y4 = (int) (y + newLen*2 * Math.cos(inRadians));
        kochCurve(x4,y4,newLen,angle, limit-1,g);
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
    }

}
