package fx;

import demo3d.View3D;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

public class LayerDisplay extends Pane
{
    public static final ObjectProperty<Color> sceneColorProperty =
            new SimpleObjectProperty<>(Color.WHITE);
    public static int RADIUS=8;
    public static int HIT_RADIUS=0;
    public static int VERT_OFFSET=10;

    private Main mainWindow;
    private CheckBox checkBox;
    private int layerNo;

    private int offset=20;
    private int size=4;
    Circle[][] Leds;
    public LayerDisplay(int size, int no, Main mainWindow)
    {
        VERT_OFFSET=10/(int)(size*0.75);
        RADIUS=8-size/4;
        this.layerNo=no;
        this.mainWindow=mainWindow;
        this.size=size;
        //setStyle("-fx-background-color: #adadad");
        setPadding(new Insets(VERT_OFFSET));
        offset=(size)*VERT_OFFSET;
        Leds=new Circle[size][size];
        HBox hBox=new HBox(10);
        Pane ledsPane=new Pane();
        for(int x=0; x<size; x++)
        {
            for(int y=0; y<size; y++)
            {
                Leds[x][y]=new Circle(RADIUS);
                Leds[x][y].setStrokeType(StrokeType.INSIDE);
                //Leds[x][y].setStroke(Color.rgb(176,176,176));
                Leds[x][y].setStroke(Color.TRANSPARENT);
                Leds[x][y].setStrokeWidth(RADIUS/2);
                Leds[x][y].setCenterX(offset +x*2*RADIUS+x*VERT_OFFSET);
                Leds[x][y].setCenterY(offset +y*2*RADIUS-6+x*VERT_OFFSET+y*VERT_OFFSET);
                final int a=x,b=y;
                Leds[x][y].setOnMousePressed(ev->LedClicked(a,b,ev.getButton()));
                Leds[x][y].setOnDragDetected(ev->this.startFullDrag());
                Leds[x][y].setOnMouseDragged(ev->LedDrag(a,b,ev));
                //Leds[x][y].setOnMouseDragOver(ev-> LedDrag(a,b,ev.getButton()));
                ledsPane.getChildren().add(Leds[x][y]);
            }
        }
        //ledsPane.setMinSize(Double.MAX_VALUE,Double.MAX_VALUE);

        checkBox=new CheckBox();
        checkBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(checkBox,ledsPane);
        this.getChildren().add(hBox);

    }
    public boolean isSelected()
    {
        return checkBox.isSelected();
    }
    private void LedDrag(int x, int y, MouseEvent ev)
    {
        for(int i=0; i<size; i++)
        {
            for(int j=0; j<size; j++)
            {

                //int Lx=(int)Leds[i][j].getCenterX();
                //int Ly=(int)Leds[i][j].getCenterY();
                if(Leds[i][j].intersects(ev.getX(),ev.getY(),0,0))
                //if(new Rectangle2D(Lx-Leds[i][j].getStrokeWidth()/2,Ly-Leds[i][j].getStrokeWidth()/2,
                  //      Leds[i][j].getStrokeWidth(),Leds[i][j].getStrokeWidth()).intersects(ev.getX(),ev.getY(),1,1))
                {
                    //System.out.println("Dragged  " + ": X:\t" + i + "\tY:\t" + j);
                    Leds[i][j].setFill(sceneColorProperty.getValue());
                }
            }
        }
        mainWindow.reload3D();
    }

    private void LedClicked(int x,int y,MouseButton button)
    {
        if(button==MouseButton.PRIMARY)
        {
            if(Main.pressedKeys.contains(KeyCode.CONTROL))
            {
                //System.out.println("Control");
                sceneColorProperty.set((Color)Leds[x][y].getFill());
            }
            else
            {
                Leds[x][y].setFill(sceneColorProperty.getValue());
                System.out.println("Clicked Layer " + layerNo + ": X:\t" + x + "\tY:\t" + y);
            }
        }
        else if(button==MouseButton.SECONDARY)
        {
            for(int xL=0; xL<size; xL++)
            {
                for(int yL=0; yL<size; yL++)
                {
                    Leds[xL][yL].setFill(sceneColorProperty.getValue());
                }
            }
        }
        else if(button==MouseButton.MIDDLE)
        {
            for(int xL=0; xL<size; xL++)
            {
                Leds[xL][y].setFill(sceneColorProperty.getValue());
            }
        }
        mainWindow.reload3D();

    }
    public void setRGBs(int[] vals)
    {
        for(int x=0; x<size; x++)
        {
            for(int y=0; y<size; y++)
            {
                java.awt.Color awtColor= new java.awt.Color(vals[x+y*size]);
                //System.out.println(String.format("0x%08X", vals[x+y*size]));
                Leds[x][y].setFill(Color.rgb(awtColor.getRed(),awtColor.getGreen(),awtColor.getBlue()));
            }
        }
    }
    public void setLed(int x, int y,int val)
    {
        java.awt.Color awtColor= new java.awt.Color(val);
       // System.out.println(String.format("0x%08X", val));
        Leds[x][y].setFill(Color.rgb(awtColor.getRed(),awtColor.getGreen(),awtColor.getBlue()));
    }
    public void setLed(int x, int y,Color val)
    {
        Leds[x][y].setFill(val);
    }
    public Color getLed(int x,int y)
    {
        return (Color)Leds[x][y].getFill();
    }
    public Color[][] getColors()
    {
        Color[][] colors=new Color[size][size];
        for(int x=0; x<size; x++)
        {
            for(int y=0; y<size; y++)
            {
                colors[x][y]=getLed(x,y);
            }
        }
        return colors;
    }
    public void setColors(Color[][] colors)
    {
        for(int x=0; x<size; x++)
        {
            for(int y=0; y<size; y++)
            {
                setLed(x,y,colors[x][y]);
            }
        }
    }
    public void setRGBs(Color val)
    {
        for(int x=0; x<size; x++)
        {
            for(int y=0; y<size; y++)
            {
                Leds[x][y].setFill(val);
            }
        }
    }
    public int[] getRGBs()
    {
        int[] rgbs=new int[size*size];
        for(int x=0; x<size; x++)
        {
            for(int y=0; y<size; y++)
            {
                Color c=(Color)Leds[x][y].getFill();
                String hex = String.format( "%02X%02X%02X",
                        (int)( c.getRed() * 255 ),
                        (int)( c.getGreen() * 255 ),
                        (int)( c.getBlue() * 255 ) );
                rgbs[x+y*size]=Integer.parseInt(hex,16);
                //System.out.println(String.format("0x%08X",rgbs[x+y*size]));
            }
        }
        return rgbs;
    }
    public void printRGBs()
    {
        int[] rgbs=getRGBs();
        for(int i=0; i<rgbs.length; i++)
        {
            System.out.println(String.format("0x%08X",rgbs[i]));
        }
    }

    public void setSelect(boolean b)
    {
        checkBox.setSelected(b);
    }

    public LayerDisplay clone()
    {
        LayerDisplay layerDisplay=new LayerDisplay(size,layerNo, mainWindow);
        layerDisplay.setRGBs(getRGBs());
        layerDisplay.setSelect(isSelected());
        return layerDisplay;
    }
}
