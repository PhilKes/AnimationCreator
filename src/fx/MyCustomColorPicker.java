package fx;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.util.StringConverter;

import java.util.ArrayList;


public class MyCustomColorPicker extends VBox {

    private final ObjectProperty<Color> currentColorProperty=
            new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> customColorProperty=
            new SimpleObjectProperty<>(Color.TRANSPARENT);

    private Pane colorRect;
    private final Pane colorBar;
    private final Pane colorRectOverlayOne;
    private final Pane colorRectOverlayTwo;
    private Region colorRectIndicator;
    private final Region colorBarIndicator;
    private Pane newColorRect;

    private DoubleProperty hue=new SimpleDoubleProperty(-1);
    private DoubleProperty sat=new SimpleDoubleProperty(-1);
    private DoubleProperty bright=new SimpleDoubleProperty(-1);

    private DoubleProperty alpha=new SimpleDoubleProperty(100) {
        @Override
        protected void invalidated() {
            setCustomColor(new Color(getCustomColor().getRed(), getCustomColor().getGreen(),
                    getCustomColor().getBlue(), clamp(alpha.get() / 100)));
        }
    };

    public MyCustomColorPicker() {

        getStyleClass().add("my-custom-color");

        VBox box=new VBox();

        box.getStyleClass().add("color-rect-pane");
        customColorProperty().addListener((ov, t, t1) -> colorChanged());

        colorRectIndicator=new Region();
        colorRectIndicator.setId("color-rect-indicator");
        colorRectIndicator.setManaged(false);
        colorRectIndicator.setMouseTransparent(true);
        colorRectIndicator.setCache(true);

        final Pane colorRectOpacityContainer=new StackPane();

        colorRect=new StackPane();
        colorRect.getStyleClass().addAll("color-rect", "transparent-pattern");

        Pane colorRectHue=new Pane();
        colorRectHue.backgroundProperty().bind(new ObjectBinding<Background>() {

            {
                bind(hue);
            }

            @Override
            protected Background computeValue() {
                return new Background(new BackgroundFill(
                        Color.hsb(hue.getValue(), 1.0, 1.0),
                        CornerRadii.EMPTY, Insets.EMPTY));

            }
        });

        colorRectOverlayOne=new Pane();
        colorRectOverlayOne.getStyleClass().add("color-rect");
        colorRectOverlayOne.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(255, 255, 255, 1)),
                        new Stop(1, Color.rgb(255, 255, 255, 0))),
                CornerRadii.EMPTY, Insets.EMPTY)));

        EventHandler<MouseEvent> rectMouseHandler=event -> {
            final double x=event.getX();
            final double y=event.getY();
            sat.set(clamp(x / colorRect.getWidth()) * 100);
            bright.set(100 - (clamp(y / colorRect.getHeight()) * 100));
            updateHSBColor();
        };

        colorRectOverlayTwo=new Pane();
        colorRectOverlayTwo.getStyleClass().addAll("color-rect");
        colorRectOverlayTwo.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(0, 0, 0, 0)), new Stop(1, Color.rgb(0, 0, 0, 1))),
                CornerRadii.EMPTY, Insets.EMPTY)));
        colorRectOverlayTwo.setOnMouseDragged(rectMouseHandler);
        colorRectOverlayTwo.setOnMousePressed(rectMouseHandler);

        Pane colorRectBlackBorder=new Pane();
        colorRectBlackBorder.setMouseTransparent(true);
        colorRectBlackBorder.getStyleClass().addAll("color-rect", "color-rect-border");

        colorBar=new Pane();
        colorBar.getStyleClass().add("color-bar");
        colorBar.setBackground(new Background(new BackgroundFill(createHueGradient(),
                CornerRadii.EMPTY, Insets.EMPTY)));

        colorBarIndicator=new Region();
        colorBarIndicator.setId("color-bar-indicator");
        colorBarIndicator.setMouseTransparent(true);
        colorBarIndicator.setCache(true);

        colorRectIndicator.layoutXProperty().bind(
                sat.divide(100).multiply(colorRect.widthProperty()));
        colorRectIndicator.layoutYProperty().bind(
                Bindings.subtract(1, bright.divide(100)).multiply(colorRect.heightProperty()));
        colorBarIndicator.layoutXProperty().bind(
                hue.divide(360).multiply(colorBar.widthProperty()));
        colorRectOpacityContainer.opacityProperty().bind(alpha.divide(100));

        EventHandler<MouseEvent> barMouseHandler=event -> {
            final double x=event.getX();
            hue.set(clamp(x / colorRect.getWidth()) * 360);
            updateHSBColor();
        };

        colorBar.setOnMouseDragged(barMouseHandler);
        colorBar.setOnMousePressed(barMouseHandler);
        newColorRect=new Pane();
        newColorRect.getStyleClass().add("color-new-rect");
        newColorRect.setId("new-color");
        newColorRect.backgroundProperty().bind(new ObjectBinding<Background>() {
            {
                bind(customColorProperty);
            }

            @Override
            protected Background computeValue() {
                return new Background(new BackgroundFill(customColorProperty.get(), CornerRadii.EMPTY, Insets.EMPTY));
            }
        });

        colorBar.getChildren().setAll(colorBarIndicator);
        colorRectOpacityContainer.getChildren().setAll(colorRectHue, colorRectOverlayOne, colorRectOverlayTwo);
        colorRect.getChildren().setAll(colorRectOpacityContainer, colorRectBlackBorder, colorRectIndicator);
        VBox.setVgrow(colorRect, Priority.SOMETIMES);

        HBox colsPref=new HBox(4);
        TextField rgbText=new TextField("#FFFFFF");
        rgbText.setPrefColumnCount(7);
        rgbText.setMaxWidth(64);
        //rgbText.textProperty().bindBidirectional(new SimpleStringProperty(currentColorProperty.getName()));
        rgbText.textProperty().bindBidirectional(customColorProperty, new StringConverter<Color>() {
            @Override
            public String toString(Color color) {
                return String.format("#%02X%02X%02X",
                        (int) (color.getRed() * 255),
                        (int) (color.getGreen() * 255),
                        (int) (color.getBlue() * 255));
            }

            @Override
            public Color fromString(String string) {
                try {
                    return Color.web(string);
                }
                catch(Exception e) {
                    return Color.WHITE;
                }
            }
        });
        ArrayList<Button> prefColors=new ArrayList<>();
        /*
        int rgb=0xFF;
        for(int colors=0; colors<3; colors++)
        {
            prefColors.add(createColorButton(rgb));
            for(int i=0; i<colors; i++)
            {
                int rgb2=0xFF << ((colors) * 8);
                prefColors.add(createColorButton(rgb + rgb2));
            }
            rgb=rgb<<8;
        }*/
        prefColors.add(createColorButton(0x00));
        prefColors.add(createColorButton(0xFF));
        prefColors.add(createColorButton(0xFF00));
        prefColors.add(createColorButton(0xFFFF));
        prefColors.add(createColorButton(0xFF0000));
        prefColors.add(createColorButton(0xFF00FF));
        prefColors.add(createColorButton(0xFFFF00));
        prefColors.add(createColorButton(0xFFFFFF));

        colsPref.getChildren().addAll(rgbText);
        for(Button button : prefColors)
            colsPref.getChildren().add(button);
        box.getChildren().addAll(colorBar, colorRect, newColorRect, colsPref);
        getChildren().add(box);

        if(currentColorProperty.get()==null) {
            currentColorProperty.set(Color.TRANSPARENT);
        }
        updateValues();

    }

    private Button createColorButton(int rgb) {
        Button col=new Button();
        col.setOnAction(ev -> setCustomColor((Color) col.getTextFill()));
        String hex=Integer.toHexString(rgb);
        String colorHex="";
        for(int i=0; i<6 - hex.length(); i++) {
            colorHex+="0";
        }
        colorHex="#" + colorHex + hex;

        col.setMinWidth(12);
        col.setStyle("-fx-background-color:" + colorHex + ";-fx-text-fill:" + colorHex + ";");
        return col;
    }

    private void updateValues() {
        hue.set(getCurrentColor().getHue());
        sat.set(getCurrentColor().getSaturation() * 100);
        bright.set(getCurrentColor().getBrightness() * 100);
        alpha.set(getCurrentColor().getOpacity() * 100);
        setCustomColor(Color.hsb(hue.get(), clamp(sat.get() / 100),
                clamp(bright.get() / 100), clamp(alpha.get() / 100)));
    }

    private void colorChanged() {
        hue.set(getCustomColor().getHue());
        sat.set(getCustomColor().getSaturation() * 100);
        bright.set(getCustomColor().getBrightness() * 100);
    }

    private void updateHSBColor() {
        Color newColor=Color.hsb(hue.get(), clamp(sat.get() / 100),
                clamp(bright.get() / 100), clamp(alpha.get() / 100));
        setCustomColor(newColor);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        colorRectIndicator.autosize();
    }

    static double clamp(double value) {
        return value<0 ? 0 : value>1 ? 1 : value;
    }

    private static LinearGradient createHueGradient() {
        double offset;
        Stop[] stops=new Stop[255];
        for(int x=0; x<255; x++) {
            offset=(double) ((1.0 / 255) * x);
            int h=(int) ((x / 255.0) * 360);
            stops[x]=new Stop(offset, Color.hsb(h, 1.0, 1.0));
        }
        return new LinearGradient(0f, 0f, 1f, 0f, true, CycleMethod.NO_CYCLE, stops);
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColorProperty.set(currentColor);
        updateValues();
    }

    Color getCurrentColor() {
        return currentColorProperty.get();
    }

    public final ObjectProperty<Color> customColorProperty() {
        return customColorProperty;
    }

    void setCustomColor(Color color) {
        customColorProperty.set(color);
    }

    Color getCustomColor() {
        return customColorProperty.get();
    }
}