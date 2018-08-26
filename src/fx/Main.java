package fx;


import com.sun.javafx.css.converters.StringConverter;
import demo3d.View3D;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.NumberStringConverter;
import util.AnimationCreatorRGB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Main extends Application
{
    public static int SIZE=4,FRAMETIME=200;

    private Stage window;
    private TextField animText;
    private TextArea infoText;
    private Label frameCount;
    private LayerDisplay[] layers;
    private ArrayList<Frame> frames;
    private Spinner<Integer> frameSpin;
    private boolean isRGB=true;
    private int mouseX,mouseY;
    private HashMap<Integer,LayerDisplay> copyLayer;
    private SpinnerValueFactory.IntegerSpinnerValueFactory spinnerValueFactory;
    private int currFrame=0;
    private BorderPane borderPane;

    private CheckBox keepFr;
    private  View3D view3D;
    private SimpleIntegerProperty frameTime=new SimpleIntegerProperty(FRAMETIME);
    public static final Set<KeyCode> pressedKeys = new HashSet<>();
    public static void main(String[] args) {
	// write your code here

        if(args.length>0)
            SIZE=Integer.parseInt(args[0]);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        window=primaryStage;
        copyLayer=new HashMap<>();
        borderPane=new BorderPane();
        //region TOP
        MenuBar menuBar = new MenuBar();

        // Create menus
        Menu fileMenu = new Menu("File");
        Menu editMenu = new Menu("Edit");
        Menu cubeMenu = new Menu("Cube");

        // Create MenuItems
        MenuItem newItem = new MenuItem("New Animation");
        newItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newItem.setOnAction(ev->{initCube();});

        MenuItem openFileItem = new MenuItem("Open Animation");
        openFileItem.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));
        openFileItem.setOnAction(ev->openAnimation());

        MenuItem saveFileItem = new MenuItem("Save");
        saveFileItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        saveFileItem.setOnAction(ev->save(new File(System.getProperty("user.dir")+"\\"+animText.getText()+".h")));

        MenuItem saveAsFileItem = new MenuItem("Save As...");
        //saveAsFileItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        saveAsFileItem.setOnAction(ev->save(saveFile()));

        MenuItem exitItem = new MenuItem("Exit");
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
        copyItem.setOnAction(ev->copyLayers());
        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setAccelerator(KeyCombination.keyCombination("Ctrl+V"));
        pasteItem.setOnAction(ev->pasteLayers());

        MenuItem selItem = new MenuItem("Select All");
        selItem.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
        selItem.setOnAction(ev->selAllLayer());

        MenuItem cube4x4Item = new MenuItem("RGB 4x4x4");
        //selItem.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
        cube4x4Item.setOnAction(ev->{SIZE=4;
            isRGB=true;initCube();});

        MenuItem cube6x6Item = new MenuItem("RGB 6x6x6");
        //selItem.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
        cube6x6Item.setOnAction(ev->{SIZE=6;
            isRGB=true;initCube();});

        MenuItem cube8x8Item = new MenuItem("RGB 8x8x8");
        //selItem.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
        cube8x8Item.setOnAction(ev->{SIZE=8;
            isRGB=true;initCube();});

        MenuItem cube8x8MonoItem = new MenuItem("Mono 8x8x8");
        //selItem.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
        cube8x8MonoItem.setOnAction(ev->{SIZE=8;
            isRGB=false;initCube();});

        fileMenu.getItems().addAll(openFileItem,saveFileItem,saveAsFileItem,newItem,selItem , exitItem);
        editMenu.getItems().addAll(copyItem, pasteItem);
        cubeMenu.getItems().addAll(cube4x4Item,cube6x6Item,cube8x8Item,cube8x8MonoItem);

        // Add Menus to the MenuBar
        menuBar.getMenus().addAll(fileMenu, editMenu,cubeMenu);
        borderPane.setTop(menuBar);
        //endregion

        frameCount=new Label("Frames:\n0");
        //region RIGHT
        VBox frameControl=new VBox(10);
        frameControl.setAlignment(Pos.TOP_CENTER);
        animText=new TextField("animName");

        frameCount.setAlignment(Pos.TOP_CENTER);
        frameSpin=new Spinner<>();
        frameSpin.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        spinnerValueFactory=new SpinnerValueFactory.IntegerSpinnerValueFactory(0,0,0);
        frameSpin.setValueFactory(spinnerValueFactory);
        frameSpin.valueProperty().addListener(
                (obs,old,newVal)->
                {
                    saveFrame(old);
                    currFrame=newVal;
                    frameCount.setText("Frames:\n"+frames.size());
                    view3D.setLEDs(frames.get(currFrame).getRGBS());
                    reloadFrame();
                });
        HBox hBox=new HBox(4);
        hBox.setAlignment(Pos.TOP_CENTER);
        Button nextFr=new Button("Next");
        nextFr.setOnAction(ev->{ nextFrame(false);});
        Button addFr=new Button("+");
        addFr.setOnAction(ev->{ addFrame(currFrame+1);nextFrame(false);});
        Button prevFr=new Button("Prev");
        prevFr.setOnAction(ev-> {prevFrame(false);});

        Button clearFr=new Button("Clear Frame");
        clearFr.setAlignment(Pos.BOTTOM_CENTER);
        clearFr.setOnAction(ev->{frames.get(currFrame).setFill(0);reloadFrame();});

        keepFr=new CheckBox("Keep Frame");
        keepFr.setAlignment(Pos.CENTER);

        Button removeFr=new Button("Remove Frame");
        removeFr.setAlignment(Pos.BOTTOM_CENTER);
        removeFr.setOnAction(ev->{
            if(frames.size()>1)
            {
                System.out.println("Remove Frame");
                frames.remove(currFrame);
                prevFrame(true);
                frameCount.setText("Frames:\n"+frames.size());
                spinnerValueFactory.setMax(frames.size()-1);
            }});
        hBox.getChildren().addAll(prevFr,addFr,nextFr);

        Button view3D=new Button("View 3D");
        view3D.setAlignment(Pos.BOTTOM_CENTER);
        view3D.setOnAction(ev->show3D());

        TextField frameTimeText=new TextField("");
        frameTimeText.textProperty().bindBidirectional(frameTime,new NumberStringConverter());
        view3D.setAlignment(Pos.BOTTOM_CENTER);
        view3D.setOnAction(ev->show3D());

        //region Shift LEDs/Layer
        GridPane shiftPane=new GridPane();
        shiftPane.setVgap(5);
        shiftPane.setHgap(5);
        shiftPane.setAlignment(Pos.CENTER);
        shiftPane.add(new Label("Shift LEDs"),0,0,3,1);
        //HBox slideBox=new HBox();
        Button leftBtn=new Button("\u2BC7");
        leftBtn.setOnAction(ev->shift(2));

        Button rightBtn=new Button("\u2BC8");
        rightBtn.setOnAction(ev->shift(3));

        Button upBtn=new Button("\u2BC5");
        upBtn.setOnAction(ev->shift(0));

        Button downBtn=new Button("\u2BC6");
        downBtn.setOnAction(ev->shift(1));
        //slideBox.setAlignment(Pos.CENTER);
        //slideBox.getChildren().addAll(leftBtn,rightBtn);
        shiftPane.add(leftBtn,0,2);
        shiftPane.add(rightBtn,2,2);
        shiftPane.add(upBtn,1,1);
        shiftPane.add(downBtn,1,3);

        shiftPane.add(new Label("Shift Layer"),0,4,3,1);
        Button layerUpBtn=new Button("\u2BC5");
        layerUpBtn.setOnAction(ev->shiftLayer(0));

        Button layerDownBtn=new Button("\u2BC6");
        layerDownBtn.setOnAction(ev->shiftLayer(1));

        shiftPane.add(layerUpBtn,1,5);
        shiftPane.add(layerDownBtn,1,6);
        //endregion
        frameControl.getChildren().addAll(animText,frameCount,hBox,frameSpin,clearFr,keepFr,removeFr,shiftPane,view3D,frameTimeText);

        borderPane.setRight(frameControl);
        //endregion

        //region LEFT
        Scene scene = new Scene(borderPane);
        //scene.addEventFilter(MouseEvent.ANY, e -> System.out.println( e));
        scene.getStylesheets().add(getClass().getResource("color.css").toExternalForm());

        MyCustomColorPicker myCustomColorPicker = new MyCustomColorPicker();
        myCustomColorPicker.setCurrentColor(LayerDisplay.sceneColorProperty.get());
        CustomMenuItem itemColor = new CustomMenuItem(myCustomColorPicker);
        itemColor.setHideOnClick(false);
        LayerDisplay.sceneColorProperty.bindBidirectional(myCustomColorPicker.customColorProperty());
        ContextMenu contextMenu = new ContextMenu(itemColor);
        contextMenu.show(primaryStage,0,0);

        VBox leftBox=new VBox(10);
        Button fillFr=new Button("Fill Frame");
        VBox.setMargin(fillFr,new Insets(0,0,0,10));
        //fillFr.setAlignment(Pos.TOP_RIGHT);
        fillFr.setOnAction(ev->{
            for(int i=0; i<layers.length; i++)
            {
                layers[i].setRGBs(LayerDisplay.sceneColorProperty.get());
            }});

        infoText=new TextArea("Ctrl+A\t\t:Select All\nCtrl+MouseLeft\t:Copy Color\nCtrl+R\t:Open Anim\nCtrl+S\t:Save Anim\n");
        VBox.setMargin(infoText,new Insets(0,10,10,10));
        infoText.setMaxSize(200,200);

        leftBox.getChildren().addAll(myCustomColorPicker,fillFr,infoText);

        borderPane.setLeft(leftBox);
        //endregion
        initCube();

        scene.setOnKeyPressed(e -> pressedKeys.add(e.getCode()));
        scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));

        scene.addEventFilter(ScrollEvent.SCROLL,ev->scrollFrames((int)ev.getDeltaY()));
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Animation Creator RGB");
        primaryStage.show();
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setMaxHeight(primScreenBounds.getHeight());
        primaryStage.setX((primScreenBounds.getWidth()/2 + primaryStage.getWidth())/2 );
        primaryStage.setY( primScreenBounds.getHeight()/16);
        borderPane.requestFocus();
        //System.out.println(System.getProperty("user.dir"));
    }

    private void show3D()
    {
        saveFrame(currFrame);

        //view3D=new View3D();
        //view3D.setLEDs(frames.get(currFrame).getRGBS());
        view3D.setX(window.getX()+window.getWidth());
        view3D.setY(window.getY());
        view3D.show();

    }

    private void pasteLayers()
    {
        copyLayer.forEach((k,v)->
        {
            layers[k].setRGBs(v.getRGBs());
        });
        System.out.println("Pasted selection");
        reload3D();
        //copyLayer.clear();
    }

    private void copyLayers()
    {

        copyLayer.clear();
        for(int layer=0; layer<SIZE; layer++)
        {
            if(layers[layer].isSelected())
                copyLayer.put(layer,layers[layer].clone());
            if(layers[layer].intersects(mouseX,mouseY,1,1))
            {
                copyLayer.clear();
                copyLayer.put(layer,layers[layer].clone());
                break;
            }

        }
        System.out.println("Copied selection");
       // System.out.println("---------");


    }

    private void initCube()
    {
        frames=new ArrayList<>();
        frames.add(new Frame(SIZE));
        currFrame=0;
        System.out.println("Initialize "+SIZE+"x"+SIZE+"x"+SIZE+(isRGB?"RGB":"Mono")+" Cube");
        //region CENTER
        view3D=new View3D(SIZE,this);
        view3D.setLEDs(frames.get(0).getRGBS());


        VBox vBox=new VBox();
        vBox.setStyle("-fx-background-color: #adadad");
        vBox.setPadding(new Insets(10));
        layers=new LayerDisplay[SIZE];
        for(int i=0; i<SIZE; i++)
        {
            layers[i]=new LayerDisplay(SIZE,i,this);
            vBox.getChildren().add(0,layers[i]);
        }
        //Mouse Position
        //vBox.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {mouseX=(int)e.getX();mouseY=(int)e.getY();});
        borderPane.setCenter(vBox);
        BorderPane.setMargin(vBox,new Insets(10,10,0,0));
        spinnerValueFactory.setMax(0);
        spinnerValueFactory.setValue(0);
        frameCount.setText("Frames:\n1");
        animText.setText("animName");
        window.sizeToScene();


        //endregion

    }

    private File saveFile()
    {
        final FileChooser fileChooser=new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("H files (*.h)", "*.h"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File animFile=fileChooser.showSaveDialog(window);
        if(animFile!=null)
        {
            return animFile;
        }
        return null;
    }

    private void selAllLayer()
    {
        boolean allSelected= true;
        for(int i=0; i<layers.length; i++)
        {
            if(!layers[i].isSelected())
                allSelected=false;
        }
        for(int i=0; i<layers.length; i++)
        {
            layers[i].setSelect(!allSelected);
        }
    }

    private void scrollFrames(int dir)
    {
       // System.out.println(dir);
        if(dir<0)
        {
            if(currFrame-1>=0)
                frameSpin.getValueFactory().setValue(--currFrame);
        }
        else if(dir>0)
        {
            if(currFrame+1<=spinnerValueFactory.getMax())
                frameSpin.getValueFactory().setValue(++currFrame);
        }
    }

    private void shiftLayer(int dir)
    {
        int[][] layerRGBs=new int[SIZE][SIZE*SIZE];
        for(int j=0; j<SIZE; j++)
        {
                layerRGBs[j]=layers[j].getRGBs();
        }
        for(int i=0; i<SIZE; i++)
        {
            if(dir==1) //UP
                layers[i].setRGBs(layerRGBs[(i+1)%SIZE]);
            else if(dir==0) //UP
                layers[i].setRGBs(layerRGBs[i-1<0? SIZE-1:i-1]);
        }
        reload3D();
    }

    private void shift(int dir)
    {
        for(int i=0; i<SIZE; i++)
        {
            if(!layers[i].isSelected())
                continue;
            Color[][] colors=layers[i].getColors();
            for(int x=0; x<SIZE; x++)
            {
                for(int y=0; y<SIZE; y++)
                {
                    switch(dir)
                    {
                        case 0: //UP
                            layers[i].setLed(x,y,colors[x][(y+1)%SIZE]);
                            break;
                        case 1: //DOWN
                            layers[i].setLed(x,y,colors[x][y-1<0? SIZE-1:y-1]);
                            break;
                        case 2: //LEFT
                            layers[i].setLed(x,y,colors[(x+1)%SIZE][y]);
                            break;
                        case 3: //RIGHT
                            layers[i].setLed(x,y,colors[x-1<0? SIZE-1:x-1][y]);
                            break;
                    }

                }
            }
        }
        reload3D();
    }

    private void openAnimation()
    {
        final FileChooser fileChooser=new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("H files (*.h)", "*.h"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File animFile=fileChooser.showOpenDialog(window);
        if(animFile!=null)
        {

            frames=new ArrayList<>();
            try
            {
                String aniName = animFile.getName();
                int pos = aniName.lastIndexOf(".");
                if (pos > 0)
                    aniName = aniName.substring(0, pos);

                StringBuffer contents = new StringBuffer();
                BufferedReader reader = null;

                reader = new BufferedReader(new FileReader(animFile));
                String text = null;
                System.out.println("Open "+aniName+".h");

                //Get Type of Cube ("//4xRGB")
                String[] type=reader.readLine().split("//|[x]");
                SIZE=Integer.parseInt(type[1]);
                initCube();
                if(type[2].equals("RGB"))
                {
                    isRGB=true;
                }
                else  if(type[2].equals("Mono"))
                {
                    isRGB=false;
                }
                //FrameCount
                text=reader.readLine();
                String[] frames=text.split(" ");
                this.frames=new ArrayList<>();
                int frameCount=Integer.parseInt(frames[frames.length-1]);
                for(int i=0; i<frameCount; i++)
                {
                    Frame frame=new Frame(SIZE);
                    this.frames.add(frame);
                    //frame.setRGBs(framesRGB[i],);
                }
                //FrameTime
                text=reader.readLine();
                frames=text.split(" ");
                frameTime.set(Integer.parseInt(frames[frames.length-1]));

                int k=-1;
                reader.readLine();
                // repeat until all lines is read
                while ((text = reader.readLine()) != null)
                {
                   // String[] split=StringUtils.splitString(text,"[,{};]");
                    String[] name=text.split("[,{};]");
                    if(name.length==0)
                        break;
                    String[] split=new String[name.length-1];
                    for(int i=1; i<name.length; i++)
                    {
                        split[i-1]=name[i];
                    }
                   // if(split.length!=SIZE*SIZE*SIZE*3)
                    //    break;
                    k++;
                    int[][] frameRGB=new int[SIZE][SIZE*SIZE];
                    final double scale =(255.0/4095);
                    for(int layer=0; layer<SIZE; layer++)
                    {
                        //16 RGB LEDs in einem Layer
                        for(int i=0; i<SIZE*SIZE; i++)
                        {
                            try
                            {
                                //RGB
                                if(isRGB)
                                {
                                    int rgb=(int) (scale * Integer.parseInt(split[layer * SIZE * SIZE * 3 + i * 3]));
                                    rgb=(rgb << 8) + (int) (scale * Integer.parseInt(split[layer * SIZE * SIZE * 3 + i * 3 + 1]));
                                    rgb=(rgb << 8) + (int) (scale * Integer.parseInt(split[layer * SIZE * SIZE * 3 + i * 3 + 2]));
                                    // System.out.println(String.format("0x%08X", isRGB));
                                    frameRGB[layer][i]=rgb;
                                }
                                //MONO
                                else
                                {
                                    int rgb=(int) (scale * Integer.parseInt(split[layer * SIZE * SIZE + i ]));
                                    rgb=(rgb << 8) + (int) (scale * Integer.parseInt(split[layer * SIZE * SIZE + i ]));
                                    rgb=(rgb << 8) + (int) (scale * Integer.parseInt(split[layer * SIZE * SIZE + i ]));

                                    frameRGB[layer][i]=rgb;
                                }
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                                infoText.setStyle("-fx-text-fill: red");
                                infoText.setText(e.getMessage());
                            }
                        }
                        this.frames.get(k).setRGBs(frameRGB[layer],layer);
                    }
                }
                reader.close();

                spinnerValueFactory.setMax(k);
                spinnerValueFactory.setValue(0);
                currFrame=0;
                reloadFrame();
                this.frameCount.setText("Frames:\n"+frameCount);
                this.animText.setText(aniName);

            }
            catch(Exception e)
            {
                e.printStackTrace();
                infoText.setStyle("-fx-text-fill: red");
                infoText.setText(e.getMessage());
            }
        }
    }

    private void reloadFrame()
    {
        for(int i=0; i<layers.length; i++)
        {
            layers[i].setRGBs(frames.get(currFrame).getRGBLayer(i));
        }
        view3D.setLEDs(frames.get(currFrame).getRGBS());
    }

    private void save(File saveFile)
    {
        saveFrame(currFrame);
        try
        {
            if(saveFile!=null)
            {
                System.out.println("Saving "+saveFile.getAbsoluteFile()+" for "+SIZE+"x"+SIZE+"x"+SIZE+(isRGB?"RGB":"Mono")+" Cube...");
                AnimationCreatorRGB.processAnimation(frames, saveFile, SIZE, isRGB,frameTime.get());
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            infoText.setStyle("-fx-text-fill: red");
            infoText.setText(e.getMessage());
        }
        System.out.println("Saved");
    }
    private void saveFrame(int frame)
    {
        //System.out.println("Save");
        if(frame>frames.size()-1)
            return;
        for(int i=0; i<SIZE; i++)
        {
            frames.get(frame).setRGBs(layers[i].getRGBs(),i);
        }
    }
    private void nextFrame(boolean removed)
    {
        saveFrame(currFrame);
        if(frames.size()-1<++currFrame)
        {
           addFrame(currFrame);
        }
        if(keepFr.isSelected())
        {
            Frame frame=frames.get(currFrame);
            for(int i=0; i<SIZE; i++)
            {
                if(layers[i].isSelected())
                    frame.setRGBs(frames.get(currFrame-1).getRGBLayer(i),i);
                else
                    frame.setRGBs(frames.get(currFrame).getRGBLayer(i),i);
            }
            //frames.set(currFrame,frame);

        }
        frameSpin.getValueFactory().setValue(currFrame);
    }

    private void addFrame(int index)
    {
        frames.add(index,new Frame(SIZE));
        spinnerValueFactory.setMax(frames.size()-1);
        frameCount.setText("Frames:\n"+frames.size());
        System.out.println("Add Frame");
    }

    private void prevFrame(boolean removed)
    {
        saveFrame(currFrame);
        boolean first=false;
        if(--currFrame<0)
        {
            currFrame=0;
            first=true;
        }
        if(keepFr.isSelected())
        {
            Frame frame=frames.get(currFrame);
            for(int i=0; i<SIZE; i++)
            {
                if(!removed&&layers[i].isSelected())
                    frame.setRGBs(frames.get(first? 0:currFrame+1).getRGBLayer(i),i);
                else
                    frame.setRGBs(frames.get(currFrame).getRGBLayer(i),i);
            }
            //frames.set(currFrame,frames.get(first? 0:currFrame+1).clone());
        }
        frameSpin.getValueFactory().setValue(currFrame);
    }

    public int getFrameTime()
    {
        return frameTime.get();
    }

    public ArrayList<Frame> getFrames()
    {
        return frames;
    }

    public int getCurrFrame()
    {
        return currFrame;
    }

    public void reload3D()
    {
       saveFrame(currFrame);
       view3D.setLEDs(frames.get(currFrame).getRGBS());
    }
}
