package demo3d;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

public class MoleculeSampleApp1 extends Application {

    Group root = new Group();
    XformBox cameraXform = new XformBox();
    XformBox ballXForm = new XformBox();
    Shape3D ball;
    PhongMaterial redMaterial, greenMaterial, blueMaterial;

    PerspectiveCamera camera = new PerspectiveCamera(true);

    private static double CAMERA_INITIAL_DISTANCE = -450;
    private static double CAMERA_INITIAL_X_ANGLE = -10.0;
    private static double CAMERA_INITIAL_Y_ANGLE = 0.0;
    private static double CAMERA_NEAR_CLIP = 0.1;
    private static double CAMERA_FAR_CLIP = 10000.0;
    private static double AXIS_LENGTH = 250.0;
    private static double MOUSE_SPEED = 0.1;
    private static double ROTATION_SPEED = 2.0;

    double mouseStartPosX, mouseStartPosY;
    double mousePosX, mousePosY;
    double mouseOldX, mouseOldY;
    double mouseDeltaX, mouseDeltaY;

    private void handleMouse(Scene scene) {
        System.out.printf("handleMouse%n");

        scene.setOnMousePressed(me -> {
            mouseStartPosX = me.getSceneX();
            mouseStartPosY = me.getSceneY();
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });

        scene.setOnMouseDragged(me -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            if (me.isPrimaryButtonDown()) {
                ballXForm.addRotation(-mouseDeltaX * MOUSE_SPEED * ROTATION_SPEED, Rotate.Y_AXIS);
                ballXForm.addRotation(mouseDeltaY * MOUSE_SPEED * ROTATION_SPEED, Rotate.X_AXIS);
            }
        });
    }

    private void handleKeyboard(Scene scene) {
        scene.setOnKeyPressed(event -> ballXForm.reset());
    }

    PhongMaterial createMaterial(Color diffuseColor, Color specularColor) {
        PhongMaterial material = new PhongMaterial(diffuseColor);
        material.setSpecularColor(specularColor);
        return material;
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.printf("start%n");
        root.setDepthTest(DepthTest.ENABLE);

        // Create materials
        redMaterial = createMaterial(Color.DARKRED, Color.RED);
        greenMaterial = createMaterial(Color.DARKGREEN, Color.GREEN);
        blueMaterial = createMaterial(Color.DARKBLUE, Color.BLUE);

        // Build Camera
        root.getChildren().add(camera);
        cameraXform.getChildren().add(camera);
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.addRotation(CAMERA_INITIAL_X_ANGLE, Rotate.X_AXIS);
        cameraXform.addRotation(CAMERA_INITIAL_Y_ANGLE, Rotate.Y_AXIS);

        // Build Axes
        Box xAxis = new Box(AXIS_LENGTH, 1, 1);
        Box yAxis = new Box(1, AXIS_LENGTH, 1);
        Box zAxis = new Box(1, 1, AXIS_LENGTH);
        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
        root.getChildren().addAll(xAxis, yAxis, zAxis);

        // Build shiney red ball
        ball = new Sphere(50);
        ball.setDrawMode(DrawMode.LINE); // draw mesh so we can watch how it rotates
        ballXForm.getChildren().add(ball);
        root.getChildren().addAll(ballXForm);

        Scene scene = new Scene(root, 1024, 768, true);
        scene.setFill(Color.GREY);
        handleKeyboard(scene);
        handleMouse(scene);

        primaryStage.setTitle("TrackBall");
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setCamera(camera);
    }

    public static void main(String[] args) {
        launch(args);
    }

}

class XformBox extends Group {

    XformBox() {
        super();
        getTransforms().add(new Affine());
    }

    /**
     * Accumulate rotation about specified axis
     *
     * @param angle
     * @param axis
     */
    public void addRotation(double angle, Point3D axis) {
        Rotate r = new Rotate(angle, axis);
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