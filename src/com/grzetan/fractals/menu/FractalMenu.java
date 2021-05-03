package com.grzetan.fractals.menu;

import com.grzetan.fractals.FractalFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

public class FractalMenu extends JPanel {

    final static int WIDTH = 1000;
    final static int HEIGHT = 800;

    public FractalMenu(FractalFrame frame){
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
        this.setFocusable(true);
        this.setBackground(Color.BLACK);
        this.setLayout(null);

        //Add header
        JLabel header = new JLabel("Pick a fractal to play with");
        header.setFont(new Font("Courier", Font.BOLD, 30));
        header.setBounds((int) (WIDTH/2 - header.getPreferredSize().getWidth() / 2),
                50,
                (int) header.getPreferredSize().getWidth(),
                (int) header.getPreferredSize().getHeight());
        this.add(header);

        //tree link
        JButton treeBtn = new JButton("Fractal tree");
        treeBtn.setBounds(50,
                50,
                (int) treeBtn.getPreferredSize().getWidth(),
                (int) treeBtn.getPreferredSize().getHeight());
        treeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                frame.change("tree");
            }
        });
        this.add(treeBtn);

        //koch snowflake link
        JButton snowflakeBtn = new JButton("Koch snowflake");
        snowflakeBtn.setBounds(50,
                100,
                (int) snowflakeBtn.getPreferredSize().getWidth(),
                (int) snowflakeBtn.getPreferredSize().getHeight());
        snowflakeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                frame.change("koch snowflake");
            }
        });
        this.add(snowflakeBtn);

        //mandelbrot set link
        JButton mandelbrotBtn = new JButton("Mandelbrot set");
        mandelbrotBtn.setBounds(50,
                200,
                (int) mandelbrotBtn.getPreferredSize().getWidth(),
                (int) mandelbrotBtn.getPreferredSize().getHeight());
        mandelbrotBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                frame.change("mandelbrot set");
            }
        });
        this.add(mandelbrotBtn);
    }
}
