package pepse.world;

import danogl.GameObject;
import danogl.components.CoordinateSpace;

import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Represents the sky background of the game world.
 * The sky is a static colored rectangle that remains fixed to the camera view
 * to simulate an infinite atmosphere.
 */
public class Sky {

    private static final Color BASIC_SKY_COLOR = Color.decode("#80C6E5");
    private static final String SKY_TAG = "sky";

    /**
     * Creates a sky GameObject that covers the entire window.
     * * @param windowDimensions The dimensions of the game window.
     * @return A GameObject representing the fixed sky background.
     */
    public static GameObject create(Vector2 windowDimensions){
        GameObject sky = new GameObject(
                Vector2.ZERO, windowDimensions,
                new RectangleRenderable(BASIC_SKY_COLOR));

        sky.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sky.setTag(SKY_TAG);

        return sky;
    }
}
