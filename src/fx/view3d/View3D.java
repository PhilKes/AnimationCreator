package fx.view3d;

import data.Frame;
import fx.Main;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

/**
 * 3D View of LED Cube
 */
public class View3D extends Stage {
    private static double CAMERA_INITIAL_DISTANCE=-450;
    private static double CAMERA_INITIAL_X_ANGLE=-30.0;
    private static double CAMERA_INITIAL_Y_ANGLE=-45.0;
    private static double CAMERA_NEAR_CLIP=0.1;
    private static double CAMERA_FAR_CLIP=10000.0;
    private static double MOUSE_SPEED=0.1;
    private static double ROTATION_SPEED=2.0;
    private static int HEIGHT=500, WIDTH=500;

    private final Timeline timeline=new Timeline();

    private PerspectiveCamera camera=new PerspectiveCamera(true);
    private XformBox cameraXform=new XformBox();
    private XformBox ballXForm=new XformBox();
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    private double mouseDeltaX, mouseDeltaY;

    Group root=new Group();

    public View3D(int size, Main mainWindow) {
        root.setDepthTest(DepthTest.ENABLE);

        // Build Camera
        root.getChildren().add(camera);
        cameraXform.getChildren().add(camera);
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.addRotation(CAMERA_INITIAL_X_ANGLE, Rotate.X_AXIS);
        cameraXform.addRotation(CAMERA_INITIAL_Y_ANGLE, Rotate.Y_AXIS);
        timeline.stop();
        timeline.getKeyFrames().clear();
        ballXForm.getChildren().add(new Cube3D(size));
        ballXForm.addRotation(90, new Point3D(0, 0, 0));
        root.getChildren().addAll(ballXForm);

        Scene scene=new Scene(root, WIDTH, HEIGHT, true);
        scene.setFill(Color.rgb(58, 58, 58));
        setScene(scene);

        setMouseHandlers(scene);
        setKeyHandlers(mainWindow);
        setOnCloseRequest(ev -> {
            ballXForm.reset();
            timeline.stop();
            timeline.getKeyFrames().clear();
        });
        setTitle("3D View");
        scene.setCamera(camera);
    }

    private void setMouseHandlers(Scene scene) {
        scene.setOnMousePressed(me -> {
            mousePosX=me.getSceneX();
            mousePosY=me.getSceneY();
            mouseOldX=me.getSceneX();
            mouseOldY=me.getSceneY();
        });
        scene.setOnMouseDragged(me -> {
            mouseOldX=mousePosX;
            mouseOldY=mousePosY;
            mousePosX=me.getSceneX();
            mousePosY=me.getSceneY();
            mouseDeltaX=(mousePosX - mouseOldX);
            mouseDeltaY=(mousePosY - mouseOldY);

            if(me.isPrimaryButtonDown()) {
                ballXForm.addRotation(-mouseDeltaX * MOUSE_SPEED * ROTATION_SPEED, Rotate.Y_AXIS);
                ballXForm.addRotation(mouseDeltaY * MOUSE_SPEED * ROTATION_SPEED, Rotate.X_AXIS);
            }
        });
    }

    private void setKeyHandlers(Main mainWindow) {
        getScene().setOnKeyPressed(ev -> {
            ArrayList<Frame> frames=mainWindow.getFrames();
            /** Press P to pause/unpause*/
            if(ev.getCode()==KeyCode.P) {
                Animation.Status status=timeline.getStatus();
                if(status==Animation.Status.RUNNING) {
                    timeline.pause();
                }
                else if(status==Animation.Status.PAUSED) {
                    timeline.play();
                }
                else if(status==Animation.Status.STOPPED) {
                    timeline.getKeyFrames().clear();
                    timeline.setCycleCount(Animation.INDEFINITE);
                    timeline.setAutoReverse(false);

                    for(int i=0; i<frames.size(); i++) {
                        final int j=i;
                        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(j * mainWindow.getFrameTime()),
                                e -> setLEDs(frames.get(j).getRGBS())));
                    }
                    timeline.play();
                }
            }
            /** Press Q to show current Frame*/
            else if(ev.getCode()==KeyCode.Q) {
                timeline.getKeyFrames().clear();
                timeline.stop();
                setLEDs(frames.get(mainWindow.getCurrFrame()).getRGBS());
            }
            /** Press R to reset 3D View*/
            else if(ev.getCode()==KeyCode.R) {
                reset();
            }
        });
    }

    /**
     * Update all LEDs to frame - 2D Array of rgb values
     */
    public void setLEDs(int[][] rgbs) {
        Group group=(Group) root.getChildren().get(0);
        Cube3D cube=(Cube3D) group.getChildren().get(0);
        for(int i=0; i<rgbs.length; i++) {
            for(int j=0; j<rgbs[0].length; j++) {
                int idx=i * rgbs[0].length + j;
                java.awt.Color col=new java.awt.Color(rgbs[i][j]);
                Sphere led=(Sphere) cube.getChildren().get(idx);
                PhongMaterial material=new PhongMaterial(Color.rgb(col.getRed(), col.getGreen(), col.getBlue()));
                led.setMaterial(material);
            }
        }
    }

    public void reset() {
        ballXForm.reset();
    }

    static class XformBox extends Group {

        XformBox() {
            super();
            getTransforms().add(new Affine());
        }

        /**
         * Accumulate rotation about specified axis
         */
        public void addRotation(double angle, Point3D axis) {
            Rotate r=new Rotate(angle, axis);
            /**
             * This is the important bit and thanks to bronkowitz in this post
             * https://stackoverflow.com/questions/31382634/javafx-3d-rotations for
             * getting me to the solution that the rotations need accumulated in
             * this way
             */
            getTransforms().set(0, r.createConcatenation(getTransforms().get(0)));
        }

        /**
         * Reset transform to identity transform
         */
        public void reset() {
            getTransforms().set(0, new Affine());
        }
    }
}
