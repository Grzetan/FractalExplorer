package com.grzetan.fractals.fractals;

import com.grzetan.fractals.FractalFrame;
import com.grzetan.fractals.jzoom.JZoom;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SierpinskiCarpet extends JPanel {

    final int WIDTH = 1000;
    final int HEIGHT = 800;

    Thread thread;
    FractalFrame frame;

    JZoom jZoom = new JZoom(WIDTH,HEIGHT);

    int limit = 7;
    int followMouse = -1;

    boolean firstFrame = true;

    String helpMsg = "HELP\nCTRL+H - Show help\nCTRL+Q - Make carpet follow your mouse\nCTRL+S - Take screenshot\nESCAPE - go back to menu";

    public SierpinskiCarpet(FractalFrame frame){
        this.frame = frame;
        this.setFocusable(true);
        this.setBackground(Color.BLACK);
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));

        this.addMouseMotionListener(new MA());
        jZoom.installMouseAdapter(this);
        jZoom.setBackground(Color.BLACK);
        initKeyBindings();

        thread = new Thread(this::run);
        thread.start();
    }

    public void carpet(int len, int x ,int y, int limit){
        if(limit <= 0){
            return;
        }
        int oneThird = len / 3;
        jZoom.fillRect(x+oneThird, y+oneThird, oneThird, oneThird, Color.WHITE);

        carpet(oneThird, x,y,limit-1);
        carpet(oneThird,x+oneThird,y,limit-1);
        carpet(oneThird,x+oneThird*2,y,limit-1);
        carpet(oneThird,x,y+oneThird,limit-1);
        carpet(oneThird,x+oneThird*2,y+oneThird,limit-1);
        carpet(oneThird,x,y+oneThird*2,limit-1);
        carpet(oneThird,x+oneThird,y+oneThird*2,limit-1);
        carpet(oneThird,x+oneThird*2,y+oneThird*2,limit-1);
    }

    public void draw(){
        carpet(729,(WIDTH - 729) / 2,(HEIGHT-729)/2,limit);
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

        //Follow mouse
        AbstractAction a1 = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                followMouse *= -1;
                System.out.println(followMouse);
            }
        };
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK), "FOLLOW MOUSE");
        this.getActionMap().put("FOLLOW MOUSE", a1);

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

    public class MA extends MouseAdapter{
        @Override
        public void mouseMoved(MouseEvent e) {
            if(followMouse > 0){
                limit = (int) ((e.getX() / (double) WIDTH) * 7 + 1);
            }
        }
    }
}
