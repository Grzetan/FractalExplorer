package com.grzetan.fractals.jzoom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public class JZoom {
    int WIDTH;
    int HEIGHT;
    private double bounds[];
    private Image image;
    private Graphics g;
    private Point mousePos;
    private double zoom = 1;
    public float ZOOM_STRENGTH = 0.05F;
    private Point positionInFrame = new Point(0,0);
    private Color background = Color.BLACK;

    public JZoom(int width, int height){
        this.WIDTH = width;
        this.HEIGHT = height;
        this.bounds = new double[]{0, width, 0, height};
        this.image = new BufferedImage(this.WIDTH,this.HEIGHT,BufferedImage.TYPE_INT_RGB);
        this.g = this.image.getGraphics();
    }

    //Shapes
    public void drawLine(double x1, double y1, double x2, double y2, Color color){
        g.setColor(color);
        Point p1 = mapPoint(x1,y1);
        Point p2 = mapPoint(x2,y2);
        g.drawLine(p1.x,p1.y, p2.x, p2.y);
    }

    public void drawRect(double x, double y, double width, double height,Color color){
        g.setColor(color);
        Point p = mapPoint(x,y);
        g.drawRect(p.x,p.y, (int) ((int) width * zoom), (int) ((int)height*zoom));
    }

    public void fillRect(double x, double y, double width, double height,Color color){
        g.setColor(color);
        Point p = mapPoint(x,y);
        g.fillRect(p.x,p.y, (int) ((int) width * zoom), (int) ((int)height*zoom));
    }

    public void drawOval(double x, double y, double r1, double r2, Color color){
        g.setColor(color);
        Point p = mapPoint(x,y);
        g.drawOval(p.x,p.y, (int) (r1*zoom),(int) (r2*zoom));
    }

    public void fillOval(double x, double y, double r1, double r2, Color color){
        g.setColor(color);
        Point p = mapPoint(x,y);
        g.fillOval(p.x,p.y, (int) (r1*zoom),(int) (r2*zoom));
    }

    public void drawPolygon(double[] x, double[] y,int num, Color color){
        g.setColor(color);
        int x2[] = new int[num];
        int y2[] = new int[num];
        for(int i=0;i<num;i++){
            Point p = mapPoint(x[i],y[i]);
            x2[i] = p.x;
            y2[i] = p.y;
        }
        g.drawPolygon(x2,y2,num);
    }

    public void fillPolygon(double[] x, double[] y,int num, Color color){
        g.setColor(color);
        int x2[] = new int[num];
        int y2[] = new int[num];
        for(int i=0;i<num;i++){
            Point p = mapPoint(x[i],y[i]);
            x2[i] = p.x;
            y2[i] = p.y;
        }
        g.fillPolygon(x2,y2,num);
    }

    public void drawImage(BufferedImage image, double x, double y, ImageObserver observer){
        BufferedImage scaled = scale(image, (int) (zoom * image.getWidth()), (int) (zoom * image.getHeight()));
        Point p = mapPoint(x,y);
        g.drawImage(scaled, p.x, p.y, observer);
    }

    public static BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight) {
        BufferedImage scaledImage = null;
        if (imageToScale != null) {
            scaledImage = new BufferedImage(dWidth, dHeight, imageToScale.getType());
            Graphics2D graphics2D = scaledImage.createGraphics();
            graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
            graphics2D.dispose();
        }
        return scaledImage;
    }

    public void setBackground(Color color){
        background = color;
    }

    public Image generateImage(){
        Image imageToReturn = this.image;
        this.image = new BufferedImage(this.WIDTH,this.HEIGHT,BufferedImage.TYPE_INT_RGB);
        this.g = this.image.getGraphics();
        this.g.setColor(background);
        this.g.fillRect(0,0,WIDTH,HEIGHT);

        return imageToReturn;
    }

    public void render(Graphics g, int x, int y, ImageObserver imageObserver){
        positionInFrame = new Point(x,y);
        g.drawImage(generateImage(), x,y,imageObserver);
    }

    private Point mapPoint(double x, double y){
        int newX = (int) ((x - bounds[0]) * zoom);
        int newY = (int) ((y - bounds[2]) * zoom);

        return new Point(newX, newY);
    }

    private void correctBounds(){
        if(bounds[0] < 0){
            bounds[1] += Math.abs(bounds[0]);
            bounds[0] = 0;
        }
        if(bounds[1] > WIDTH){
            bounds[0] -= bounds[1] - WIDTH;
            bounds[1] = WIDTH;
        }
        if(bounds[2] < 0){
            bounds[3] += Math.abs(bounds[2]);
            bounds[2] = 0;
        }
        if(bounds[3] > HEIGHT){
            bounds[2] -= bounds[3] - HEIGHT;
            bounds[3] = HEIGHT;
        }
    }

    private void updateZoom(String operation){
        double scaleX = (bounds[1] - bounds[0]) * ZOOM_STRENGTH;
        double scaleY = (bounds[3] - bounds[2]) * ZOOM_STRENGTH;

        double mousePosX = mousePos.getX() / WIDTH;
        double mousePosY = mousePos.getY() / HEIGHT;

        if(operation == "zoom in"){
            bounds[0] += scaleX * mousePosX;
            bounds[1] -= scaleX * (1-mousePosX);
            bounds[2] += scaleY * mousePosY;
            bounds[3] -= scaleY * (1-mousePosY);
        }else if(operation == "zoom out"){
            bounds[0] -= scaleX * mousePosX;
            bounds[1] += scaleX * (1-mousePosX);
            bounds[2] -= scaleY * mousePosY;
            bounds[3] += scaleY * (1-mousePosY);
        }
        zoom = WIDTH / (bounds[1] - bounds[0]);
        if(zoom < 1){
            zoom = 1;
            bounds = new double[]{0,WIDTH,0,HEIGHT};
            return;
        }

        correctBounds();
    }

    public void installMouseAdapter(JPanel panel){
        MA ma = new MA();
        panel.addMouseMotionListener(ma);
        panel.addMouseListener(ma);
        panel.addMouseWheelListener(ma);
    }

    public float getZOOM_STRENGTH() {
        return ZOOM_STRENGTH;
    }

    public void setZOOM_STRENGTH(float ZOOM_STRENGTH) {
        this.ZOOM_STRENGTH = ZOOM_STRENGTH;
    }

    class MA extends MouseAdapter{
        Point lastPoint;

        private Point getMousePos(Point e){
            int x = e.x;
            int y = e.y;
            if(x > positionInFrame.x && x < positionInFrame.x+WIDTH && y > positionInFrame.y && y < positionInFrame.y + HEIGHT){
                return new Point(e.x - positionInFrame.x, e.y - positionInFrame.y);
            }else{
                return null;
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mousePos = getMousePos(e.getPoint());
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if(mousePos == null){
                return;
            }

            if(e.getWheelRotation() < 0){
                updateZoom("zoom in");
            }else if(e.getWheelRotation() > 0 && zoom > 1){
                updateZoom("zoom out");
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            lastPoint = null;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mousePos = getMousePos(e.getPoint());
            if(mousePos == null){
                lastPoint = null;
                return;
            }

            if(lastPoint == null){
                lastPoint = mousePos;
                return;
            }

            double offsetX = ((lastPoint.x - mousePos.x) / (double) WIDTH) * (bounds[1] - bounds[0]);
            double offsetY = ((lastPoint.y - mousePos.y) / (double) HEIGHT) * (bounds[3] - bounds[2]);
            bounds[0] += offsetX;
            bounds[1] += offsetX;
            bounds[2] += offsetY;
            bounds[3] += offsetY;

            lastPoint = mousePos;
            correctBounds();
        }
    }
}
