package pepse.world.trees;

import danogl.GameObject;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;
import java.util.Random;

/**
 * Represents a single leaf in a tree's canopy.
 * Leaves have a wind-sway effect involving rotation, width scaling, and slight opacity changes.
 */
public class Leaf extends GameObject {

    private static final Color BASE_LEAF_COLOR = new Color(50, 200, 30);
    /** Tag used to identify leaf objects in the game world. */
    public static final String LEAF_TAG = "leaf";

    private static final float TRANSITION_TIME = 2.0f;
    private static final float FADEOUT_TIME = 0.5f;

    private static final float MAX_ANGLE = 10f;
    private static final float WIDTH_FACTOR = 1.2f;

    private static final float FULL_OPACITY = 1.0f;
    private static final float MIN_OPACITY = 0.8f;

    /**
     * Constructs a new Leaf instance.
     * @param topLeftCorner The initial position of the leaf.
     * @param dimensions The size of the leaf.
     */
    public Leaf(Vector2 topLeftCorner, Vector2 dimensions) {
        super(topLeftCorner, dimensions, new RectangleRenderable(BASE_LEAF_COLOR));
        setTag(LEAF_TAG);
        // Calculate a random delay so that all leaves don't start moving simultaneously
        Random random = new Random();
        float waitTime = random.nextFloat() * TRANSITION_TIME;

        new ScheduledTask(
                this,
                waitTime,
                false,
                this::startWindEffect
        );
    }

    /**
     * Initiates the wind-sway animations.
     * Includes a rotational transition, a dimensional (width) transition, and an opacity transition.
     */
    private void startWindEffect() {
        // 1. Rotational sway (Angle)
        new Transition<Float>(
                this,
                renderer()::setRenderableAngle,
                -MAX_ANGLE,
                MAX_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                TRANSITION_TIME,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
        // 2. Dimensional sway (Width scaling)
        Vector2 initialDimensions = getDimensions();
        Vector2 enlargedDimensions = new Vector2(initialDimensions.x() * WIDTH_FACTOR, initialDimensions.y());

        new Transition<Vector2>(
                this,
                this::setDimensions,
                initialDimensions,
                enlargedDimensions,
                Transition.LINEAR_INTERPOLATOR_VECTOR,
                TRANSITION_TIME,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
        // 3. Opacity fluctuation
        new Transition<Float>(
                this,
                renderer()::setOpaqueness,
                FULL_OPACITY,
                MIN_OPACITY,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                FADEOUT_TIME + (new Random().nextFloat()),
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
    }
}


