package com.grzetan.fractals.menu;

import com.grzetan.fractals.FractalFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FractalMenu extends JPanel {

    final static int WIDTH = 800;
    final static int HEIGHT = 600;

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

        //julia set link
        JButton juliaSetLink = new JButton("Julia set");
        juliaSetLink.setBounds(50,
                300,
                (int) juliaSetLink.getPreferredSize().getWidth(),
                (int) juliaSetLink.getPreferredSize().getHeight());
        juliaSetLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                frame.change("julia set");
            }
        });
        this.add(juliaSetLink);

        //sierpinski carpet
        JButton carpet = new JButton("Sierpinski carpet");
        carpet.setBounds(50,
                400,
                (int) carpet.getPreferredSize().getWidth(),
                (int) carpet.getPreferredSize().getHeight());
        carpet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                frame.change("carpet");
            }
        });
        this.add(carpet);

        //benice equation
        JButton benice = new JButton("Benice equation");
        benice.setBounds(50,
                500,
                (int) benice.getPreferredSize().getWidth(),
                (int) benice.getPreferredSize().getHeight());
        benice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                frame.change("benice");
            }
        });
        this.add(benice);

        //burning ship
        JButton ship = new JButton("Burning ship");
        ship.setBounds(300,
                500,
                (int) ship.getPreferredSize().getWidth(),
                (int) ship.getPreferredSize().getHeight());
        ship.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                frame.change("ship");
            }
        });
        this.add(ship);
    }
}
