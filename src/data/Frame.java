package data;


import java.util.Arrays;

/** One Frame of an Animation of Cube size */
public class Frame {
    /** 2D Array of RGB values -> 1.Dimension: Layer, 2.Dimension: (y*size)+x coordinates*/
    private int[][] rgbs;
    private int size;

    public Frame(int size) {
        this.size=size;
        rgbs=new int[size][size * size];
        for(int i=0; i<size; i++) {
            Arrays.fill(rgbs[i], 0);
        }
    }

    public void setRGBs(int[] rgb, int layer) {
        for(int i=0; i<rgb.length; i++) {
            rgbs[layer][i]=rgb[i];
        }
    }

    public int[][] getRGBS() {
        return rgbs;
    }

    public int[] getRGBLayer(int layer) {
        return rgbs[layer];
    }

    public void setFill(int val) {
        for(int i=0; i<size; i++) {
            Arrays.fill(rgbs[i], val);
        }
    }

    @Override
    protected Frame clone() {
        Frame frame=new Frame(size);
        for(int i=0; i<size; i++) {
            frame.setRGBs(rgbs[i], i);
        }
        return frame;
    }

}
