package com.grzetan.fractals;

import com.grzetan.fractals.fractals.*;
import com.grzetan.fractals.menu.FractalMenu;

import javax.swing.*;

public class FractalFrame extends JFrame {
    FractalMenu menu;
    FractalTree tree;
    KochSnowflake snowflake;
    MandelbrotSet mandelbrot;
    JuliaSet julia;
    SierpinskiCarpet carpet;
    BeniceEquation benice;

    FractalFrame(){
        menu = new FractalMenu(this);
        this.setVisible(true);
        this.setFocusable(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.getContentPane().add(menu);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Explore fractals");
    }

    public void change(String fractal){
        this.getContentPane().removeAll();
        this.getContentPane().invalidate();
        switch(fractal){
            case "tree":
                tree = new FractalTree(this);
                this.getContentPane().add(tree);
                break;
            case "koch snowflake":
                snowflake = new KochSnowflake(this);
                this.getContentPane().add(snowflake);
                break;
            case "menu":
                menu = new FractalMenu(this);
                this.getContentPane().add(menu);
                break;
            case "mandelbrot set":
                mandelbrot = new MandelbrotSet(this);
                this.getContentPane().add(mandelbrot);
                break;
            case "julia set":
                julia = new JuliaSet(this);
                this.getContentPane().add(julia);
                break;
            case "carpet":
                carpet = new SierpinskiCarpet(this);
                this.getContentPane().add(carpet);
                break;
            case "benice":
                benice = new BeniceEquation(this);
                this.getContentPane().add(benice);
                break;
        }
        this.getContentPane().revalidate();
    }
}
