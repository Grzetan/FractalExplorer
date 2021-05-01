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
        image = createImage(WIDTH-120,HEIGHT);
        graphics = image.getGraphics();
        //Make black bg color
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0,0,image.getWidth(this), image.getHeight(this));
        draw(graphics);
        g.drawImage(image, 120,0,this);
    }

    public void draw(Graphics g){
        g.setColor(Color.WHITE);
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

    public void segment(int x1,int y1,int x2,int y2, int limit){
        if(limit <= 0){
            return;
        }



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
