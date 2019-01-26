package org.fortytwo.c64.video;

//import java.awt.Canvas;
import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Dimension;

import java.util.Arrays;
/**
 * The actual video screen that we want to display to
 */
public class Screen extends JPanel
{
    protected BufferedImage screenImage;
    //    protected VolatileImage screenImage = null;
    protected Graphics2D screenGraphic;

    protected int[][] prevRGBLine;
    protected int[][] RGBLine;

    private int width = 0;
    private int height = 0;
    Dimension dim = null;
    boolean firstTime = true;
    public Screen()
    {
        setBackground(Color.blue);
    }

    public void paint(Graphics g){
        update(g);
    }

    public void setSize(int width, int height){
        super.setSize(width, height);

        this.width = width;
        this.height = height;
    }

    public void update(Graphics g){
        Graphics2D g2 = (Graphics2D)g;
        
        
        if (firstTime){
            initialize();
        }
        
        for (int y = 0; y < dim.height; y++){
            if (! Arrays.equals(prevRGBLine[y],RGBLine[y])){
                System.arraycopy(RGBLine[y],0,prevRGBLine[y],0,RGBLine.length);
                screenImage.setRGB(0,y,RGBLine[y].length,1,RGBLine[y],0,1);
            }
        }
        
	int scaledWidth = g2.getClipBounds().width;
	int scaledHeight = g2.getClipBounds().height;
	//g2.drawImage(screenImage.getScaledInstance(scaledWidth,scaledHeight,1), 0,0, this);
    g2.drawImage(screenImage, 0,0,scaledWidth, scaledHeight, this);
	
    }

    public void setLine(int x, int y, int[] rgb){
        if (firstTime){
            initialize();
        }
        System.arraycopy(rgb,0,RGBLine[y],x,rgb.length);
        //screenImage.setRGB(x,y,rgb.length,1,rgb,0,1);
    }

    public void setPixel(int x, int y, int rgb){
        if (firstTime){
            initialize();
        }
        //System.out.println("Setting (" + x + "," + y + ") to " + color);
        try {
            RGBLine[y][x] = rgb;
        }
        catch (Throwable t){
            t.printStackTrace();
            System.out.println("Invalid x = " + x + " y = " + y);
        }
        //screenImage.setRGB(x,y,rgb);
    }
    public void setPixel(int x, int y, Color color){
        setPixel(x,y,color.getRGB());
    }

    public int getPixel(int x, int y){
        return RGBLine[y][x];
    }

    private void initialize(){
        if (firstTime){
	    dim = getSize();
            screenImage = (BufferedImage)createImage(dim.width,dim.height);
            //screenImage = createVolatileImage(dim.width,dim.height);
            screenGraphic = screenImage.createGraphics();
            prevRGBLine = new int[dim.height][dim.width];
            System.out.println("dim.height = " + dim.height +  "dim.width=" + dim.width);
            RGBLine = new int[dim.height][dim.width];

            for (int i = 0; i < dim.height; i++){
                for (int j = 0; j < dim.width; j++){
                    RGBLine[i][j] = -1;
                }
            }
            firstTime = false;
        }
    }
}
