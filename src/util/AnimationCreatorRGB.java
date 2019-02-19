package util;/*
 * First Attempt at generating the TLC output code from an image.
 * Run this with "java AnimationCreator"
 * It will read any image file in the current directory and create an animation for the TLC library.
 *
 * Right now this only works with 1 TLC with 16 LEDS connected to it, where
 * output0 is the bottom and output15 is the top.
 *
 * For best results make your files 16 pixels high and as wide as you want.  Each vertical pixel
 * corresponds to an LED output.
 *
 * Alex Leone <acleone ~AT~ gmail.com>, 2008-11-12
 */

import fx.Frame;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.Color;

import java.io.*;
import java.util.ArrayList;


public class AnimationCreatorRGB {

static int LEDs;
    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            LEDs=Integer.parseInt(args[0]);
        }
		else
			LEDs=48/3;
		//autoProcess();
    }

    /*
    public static void autoProcess() throws IOException {
        File currentDirectory = new File (".");
        File[] files = currentDirectory.listFiles();
        int animationCount = 1;
        for (File file : files) {
            if (!file.isFile())
                continue;
            String fileName = file.getName();
            String suffix = fileName.substring(fileName.indexOf('.') + 1);
            if(!canReadFormat(suffix))
                continue;
            String baseName = fileName.substring(0, fileName.indexOf('.'));
            String varName = "ani_" + baseName.toLowerCase();
            String outputName = varName + "_RGB.h";
            System.out.println("Writing " + outputName);
            BufferedImage image = ImageIO.read(file);
            PrintStream output = new PrintStream(new File(outputName));
            output.println("#define    " + varName.toUpperCase() + "_FRAMES    " + image.getWidth());
            output.println("const uint16_t " + varName + "["+ varName.toUpperCase() + "_FRAMES][NUM_TLCS * 16] PROGMEM = {");
            int[] rowRGB = new int[LEDs];
            for (int w = 0; w < image.getWidth(); w++) {
                for (int h = 0; h < LEDs; h++) {
                    rowRGB[h] = image.getRGB(w, (LEDs-1) - h);
                }
                parseRow(rowRGB, output);
            }
            output.println("};");
            System.out.println("Wrote " + image.getWidth() + " frames to " + outputName);
            animationCount++;
        }
    }*/
    public static void processAnimation(ArrayList<Frame> frames, File file, int size, boolean rgb, int frameTime) throws IOException
    {
            String name = file.getName();
            int pos = name.lastIndexOf(".");
            if (pos > 0)
                name = name.substring(0, pos);

            String varName = "ani_" + name.toLowerCase();
            String outputName = name + ".h";
            System.out.println("\tWriting " + file.getAbsoluteFile());

            PrintStream output = new PrintStream(file);
            output.println("//" + size+"x"+(rgb?"RGB":"Mono"));
            output.println("#define " + varName.toUpperCase() + "_FRAMES " +frames.size());
            output.println("#define " + varName.toUpperCase() + "_FRAMETIME " +frameTime);
            output.println("const uint8_t " + varName + "["+ varName.toUpperCase() + "_FRAMES][CUBE_SIZE*CUBE_SIZE*CUBE_SIZE] PROGMEM = {");
            for (int w = 0; w < frames.size(); w++)
                parseFrame(frames.get(w).getRGBS(), output,rgb);
            output.println("};");
            output.close();
            System.out.println("\tWrite complete");
           // System.out.println("Wrote " + image.getWidth() + " frames to " + outputName);
    }

    public static void processAnimationRGBit(ArrayList<Frame> frames, File file, int size, boolean rgb, int frameTime) throws IOException
    {
        String name = file.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0)
            name = name.substring(0, pos);

        String varName = "ani_" + name.toLowerCase();
        String outputName = name + ".h";
        System.out.println("\tWriting " + file.getAbsoluteFile());

        PrintStream output = new PrintStream(file);
        output.println("//" + size+"x"+(rgb?"RGB":"Mono"));
        output.println("#define " + varName.toUpperCase() + "_FRAMES " +frames.size());
        output.println("#define " + varName.toUpperCase() + "_FRAMETIME " +frameTime);
        output.println("const uint8_t " + varName + "["+ varName.toUpperCase() + "_FRAMES][CUBE_SIZE*CUBE_SIZE*CUBE_SIZE*3/8] PROGMEM = {");
        for (int w = 0; w < frames.size(); w++)
            parseFrameRGBit(frames.get(w).getRGBS(), output,rgb);
        output.println("};");
        output.close();
        System.out.println("\tWrite complete");
        // System.out.println("Wrote " + image.getWidth() + " frames to " + outputName);
    }
    // Returns true if the specified format name can be read

    public static double rgbToGrayscaleIntensity(int rgb) {
        Color c = new Color(rgb);
        return 0.2989 * c.getRed() + 0.5870 * c.getGreen() + 0.1140 * c.getBlue();
    }
    public static int rgbTo8BitColor(int rgb) {
        Color c = new Color(rgb);
        int red = (int)((c.getRed() * 8) / 256);
        int green = (int)((c.getGreen() * 8) / 256);
        int blue = (int)((c.getBlue() * 4) / 256);
        return (int)((red << 5) | (green << 2) | blue);
    }

/*
    public static void parseRow(int[] rowRGB, PrintStream output) {
        output.print("\t{");
		double scale=(double)4095/255;
        for (int i = rowRGB.length - 1; i >= 0; i --) {
			Color c = new Color(rowRGB[i]);
            //int a = (255 - (int)Math.round(rgbToGrayscaleIntensity(rowRGB[i])));
            //int b = (255 - (int)Math.round(rgbToGrayscaleIntensity(rowRGB[i - 1])));
			//output.print((int)(scale*(int)Math.round(rgbToGrayscaleIntensity(rowRGB[i])))+",");
			output.print((int)(scale*c.getRed())+","+(int)(scale*c.getGreen())+","+(int)(scale*c.getBlue())+",");
        }
		output.print("},");
        output.println();
    }*/
    public static void parseFrame(int[][] rowRGB, PrintStream output,boolean rgb) {
        output.print("{");
        double scale=(double)4095/255;
        for(int l=0; l<rowRGB.length; l++)
        {
           // for(int i=rowRGB[0].length - 1; i >= 0; i--)
            for(int i=0; i < rowRGB[0].length; i++)
            {
                Color c=new Color(rowRGB[l][i]);
                //int a = (255 - (int)Math.round(rgbToGrayscaleIntensity(rowRGB[i])));
                //int b = (255 - (int)Math.round(rgbToGrayscaleIntensity(rowRGB[i - 1])));
                //output.print((int)(scale*(int)Math.round(rgbToGrayscaleIntensity(rowRGB[i])))+",");
                if(rgb)
                    output.print(rgbTo8BitColor(c.getRGB()) + ",");
                else
                    output.print((int)Math.round(scale*rgbToGrayscaleIntensity(c.getRGB()))+",");
            }
        }
        output.print("},");
        output.println();
    }
    public static void parseFrameRGBit(int[][] rowRGB, PrintStream output,boolean rgb) {
        output.print("{");
        int bit=0;
        //output.print("B");
        for(int l=0; l<rowRGB.length; l++) {
            for(int i=0; i < rowRGB[0].length; i++) {
                Color c=new Color(rowRGB[l][i]);
                for(int j=0; j<3; j++) {
                    if(bit==0)
                        output.print("B");
                    switch(j){
                        case 0:
                            if(c.getRed()>0)
                                output.print("1");
                            else
                                output.print("0");
                            break;
                        case 1:
                            if(c.getGreen()>0)
                                output.print("1");
                            else
                                output.print("0");
                            break;
                        case 2:
                            if(c.getBlue()>0)
                                output.print("1");
                            else
                                output.print("0");
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
}