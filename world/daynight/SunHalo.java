package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Responsible for creating a visual "halo" effect around the sun.
 * The halo is a semi-transparent circle that follows the sun's position throughout its cycle.
 */
public class SunHalo {

    private static final float HALO_DIAMETER = 200f;
    private static final Color HALO_COLOR = new Color(250, 250, 0, 20);
    private static final String HALO_TAG = "sunHalo";
    private static final float HALF_FACTOR = 0.5f;

    /**
     * Creates a SunHalo GameObject that tracks a given Sun object.
     * * @param sun The Sun GameObject that this halo will follow.
     * @return A GameObject representing the sun's halo.
     */
    public static GameObject create(GameObject sun) {

        Vector2 haloSize = new Vector2(HALO_DIAMETER, HALO_DIAMETER);
        GameObject halo = new GameObject(
                sun.getCenter().subtract(haloSize.mult(HALF_FACTOR)),
                haloSize,
                new OvalRenderable(HALO_COLOR)
        );
        // Stays fixed to the camera view, just like the sun.
        halo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        halo.setTag(HALO_TAG);
        // Add component that forces the halo's center to match the sun's center every frame.
        halo.addComponent(deltaTime -> halo.setCenter(sun.getCenter()));

        return halo;
    }
}
