package fx.view3d;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.PointLight;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

/**
 * Custom Pane constructing LED Cube as Spheres with PointLights
 */
public class Cube3D extends Pane {
    private final ObservableList<Sphere> nodes=FXCollections.observableArrayList();
    private final ObservableList<PointLight> lights=FXCollections.observableArrayList();
    public static int RADIUS=6, MARGIN=20, OFFSET=20;

    public Cube3D(int size) {
        OFFSET=-(size - 1) * MARGIN / 2;
        PhongMaterial material=new PhongMaterial(Color.BLUE);
        material.setSpecularColor(Color.WHITE);
        /** Fill LED Cube with SIZE*SIZE*SIZE*/
        for(int y=size - 1; y >= 0; y--)
            for(int z=size - 1; z >= 0; z--)
                for(int x=0; x<size; x++) {
                    Sphere sphere=new Sphere(RADIUS);
                    PointLight light=new PointLight();
                    light.setColor(Color.BLUE);
                    light.setTranslateX(OFFSET + x * MARGIN);
                    light.setTranslateY(OFFSET + y * MARGIN);
                    light.setTranslateZ(OFFSET + z * MARGIN);
                    lights.add(light);
                    sphere.setTranslateX(OFFSET + x * MARGIN);
                    sphere.setTranslateY(OFFSET + y * MARGIN);
                    sphere.setTranslateZ(OFFSET + z * MARGIN);
                    sphere.setMaterial(material);
                    nodes.add(sphere);
                }
        getChildren().addAll(nodes);
    }
}
