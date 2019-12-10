package util;

import data.Frame;
import fx.Main;

import java.awt.Color;

import java.io.*;
import java.util.ArrayList;

public class AnimationCreatorRGB {

    /** Writes RGBit Animation to file*/
    public static void processAnimationRGBit(ArrayList<Frame> frames, File file, int size, boolean rgb, int frameTime) throws IOException {
        String name=file.getName();
        int pos=name.lastIndexOf(".");
        if(pos>0) {
            name=name.substring(0, pos);
        }

        String varName="ani_" + name.toLowerCase();
        String outputName=name + ".h";
        Main.showInfo("\tWriting " + file.getAbsoluteFile()+"...", Main.INFO_TYPE.INFO);

        PrintStream output=new PrintStream(file);
        /** Write Cube Type, Size, FrameCount and FrameTime to file */
        output.println("//" + size + "x" + (rgb ? "RGB" : "Mono"));
        output.println("#define " + varName.toUpperCase() + "_FRAMES " + frames.size());
        output.println("#define " + varName.toUpperCase() + "_FRAMETIME " + frameTime);
        /** Write RGBit Frame Array to File */
        output.println("const uint8_t " + varName + "[" + varName.toUpperCase() + "_FRAMES][CUBE_SIZE*CUBE_SIZE*CUBE_SIZE*3/8] PROGMEM = {");
        for(int w=0; w<frames.size(); w++)
            parseFrameRGBit(frames.get(w).getRGBS(), output, rgb);
        output.println("};");
        output.close();
        Main.showInfo("\tWrite complete.", Main.INFO_TYPE.INFO);
    }

    /** Get Grayscale Intensity of rgb color*/
    public static double rgbToGrayscaleIntensity(int rgb) {
        Color c=new Color(rgb);
        return 0.2989 * c.getRed() + 0.5870 * c.getGreen() + 0.1140 * c.getBlue();
    }

    public static int rgbTo8BitColor(int rgb) {
        Color c=new Color(rgb);
        int red=(int) ((c.getRed() * 8) / 256);
        int green=(int) ((c.getGreen() * 8) / 256);
        int blue=(int) ((c.getBlue() * 4) / 256);
        return (int) ((red << 5) | (green << 2) | blue);
    }

    /** Write a single Frame to output file */
    public static void parseFrame(int[][] rowRGB, PrintStream output, boolean rgb) {
        output.print("{");
        double scale=(double) 4095 / 255;
        for(int l=0; l<rowRGB.length; l++) {
            for(int i=0; i<rowRGB[0].length; i++) {
                Color c=new Color(rowRGB[l][i]);
                if(rgb) {
                    output.print(rgbTo8BitColor(c.getRGB()) + ",");
                }
                else {
                    output.print((int) Math.round(scale * rgbToGrayscaleIntensity(c.getRGB())) + ",");
                }
            }
        }
        output.print("},");
        output.println();
    }

    /** Write a single Frame as RGBit to output file */
    public static void parseFrameRGBit(int[][] rowRGB, PrintStream output, boolean rgb) {
        output.print("{");
        int bit=0;
        for(int l=0; l<rowRGB.length; l++) {
            for(int i=0; i<rowRGB[0].length; i++) {
                Color c=new Color(rowRGB[l][i]);
                for(int j=0; j<3; j++) {
                    if(bit==0) {
                        output.print("B");
                    }
                    switch(j) {
                        case 0:
                            if(c.getRed()>0) {
                                output.print("1");
                            }
                            else {
                                output.print("0");
                            }
                            break;
                        case 1:
                            if(c.getGreen()>0) {
                                output.print("1");
                            }
                            else {
                                output.print("0");
                            }
                            break;
                        case 2:
                            if(c.getBlue()>0) {
                                output.print("1");
                            }
                            else {
                                output.print("0");
                            }
                            break;
                    }
                    if(++bit==8) {
                        bit=0;
                        output.print(",");
                    }
                }
            }
        }
        output.print("},");
        output.println();
    }

    @Deprecated
    public static void processAnimation(ArrayList<Frame> frames, File file, int size, boolean rgb, int frameTime) throws IOException {
        String name=file.getName();
        int pos=name.lastIndexOf(".");
        if(pos>0) {
            name=name.substring(0, pos);
        }

        String varName="ani_" + name.toLowerCase();
        String outputName=name + ".h";
        Main.showInfo("\tWriting " + file.getAbsoluteFile(), Main.INFO_TYPE.INFO);

        PrintStream output=new PrintStream(file);
        output.println("//" + size + "x" + (rgb ? "RGB" : "Mono"));
        output.println("#define " + varName.toUpperCase() + "_FRAMES " + frames.size());
        output.println("#define " + varName.toUpperCase() + "_FRAMETIME " + frameTime);
        output.println("const uint8_t " + varName + "[" + varName.toUpperCase() + "_FRAMES][CUBE_SIZE*CUBE_SIZE*CUBE_SIZE] PROGMEM = {");
        for(int w=0; w<frames.size(); w++)
            parseFrame(frames.get(w).getRGBS(), output, rgb);
        output.println("};");
        output.close();
        Main.showInfo("\ttWrite complete!", Main.INFO_TYPE.INFO);
    }
}