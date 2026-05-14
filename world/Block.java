package pepse.world;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Represents a single building block in the game world, used primarily for terrain.
 * Blocks are static, immovable objects that interact with other physical game objects.
 */
public class Block extends GameObject {
    /** * The uniform size of each block side in pixels.
     */
    public static final int SIZE = 30;
    private static final String BLOCK_TAG = "block";

    /**
     * Constructs a new Block instance.
     * * @param topLeftCorner The initial position for the block's top-left corner.
     * @param renderable    The visual representation (image or color) of the block.
     */
    public Block(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);
        // Prevent other objects from penetrating the block from any direction.
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        // Ensure the block is static and cannot be moved by physical collisions.
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);

        setTag(BLOCK_TAG);
    }
}
