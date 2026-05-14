package pepse.world.trees;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.RectangleRenderable;

import danogl.util.Vector2;

import java.awt.*;

/**
 * Represents the main vertical support (trunk) of a tree in the game world.
 * Trunks are static, immovable objects that provide physical support for the canopy
 * and act as obstacles for the Avatar.
 */
public class Trunk extends GameObject {
    private static final Color TRUNK_COLOR = new Color(100, 50, 20);
    /** The tag used to identify trunk objects in the game world. */
    public static final String TRUNK_TAG = "trunk";

    /**
     * Constructs a new Trunk instance.
     * @param topLeftCorner The initial position of the trunk's top-left corner.
     * @param dimensions The width and height of the trunk.
     */
    public Trunk(Vector2 topLeftCorner, Vector2 dimensions) {
        super(topLeftCorner, dimensions, new RectangleRenderable(TRUNK_COLOR));
        setTag(TRUNK_TAG);
        // Prevent other objects (like the Avatar) from overlapping with the trunk
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        // Ensure the trunk is stationary and cannot be moved by collisions
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
    }
}
