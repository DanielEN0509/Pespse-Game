package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Responsible for creating the Sun object and defining its elliptical movement
 * across the sky relative to the camera.
 */
public class Sun {
    private static final float SUN_DIAMETER = 80f;
    private static final Color SUN_COLOR = Color.YELLOW;
    private static final String SUN_TAG = "sun";
    private static final float ORBIT_RADIUS = 150f;
    private static final float ORBIT_CENTER_Y_FAC = 2f / 3f;
    private static final float HALF_FACTOR = 0.5f;
    private static final float START_ANGLE = 0f;
    private static final float END_ANGLE = 360f;

    /**
     * Creates a Sun GameObject that orbits around a central point in the sky.
     * * @param windowDimensions The dimensions of the game window.
     * @param cycleLength      The time in seconds for a full 360-degree orbit.
     * @return A GameObject representing the orbiting sun.
     */
    public static GameObject create(Vector2 windowDimensions,
                                    float cycleLength){
        // Calculate the center point of the sun's circular path
        Vector2 cycleCenter = new Vector2(
                windowDimensions.x() * HALF_FACTOR,
                windowDimensions.y() * ORBIT_CENTER_Y_FAC
        );
        // Calculate the sun's starting position (directly above the orbit center)
        Vector2 initialSunCenter = new Vector2(
                cycleCenter.x(),
                cycleCenter.y() - ORBIT_RADIUS
        );

        Vector2 sunSize = new Vector2(SUN_DIAMETER, SUN_DIAMETER);
        // Initialize the sun object at its top-left corner relative to the center
        GameObject sun = new GameObject(
                initialSunCenter.subtract(sunSize.mult(HALF_FACTOR)),
                sunSize,
                new OvalRenderable(SUN_COLOR)
        );
        // By using CAMERA_COORDINATES, the sun follows the player across the infinite world.
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag(SUN_TAG);
        // Create the orbital movement transition
        new Transition<>(
                sun, (Float angle) -> sun.setCenter(
                            initialSunCenter.subtract(cycleCenter)
                                    .rotated(angle)
                                    .add(cycleCenter)),
                START_ANGLE,
                END_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
        return sun;
    }
}
