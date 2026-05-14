//package pepse.world.daynight;
//
//import danogl.GameObject;
//import danogl.components.CoordinateSpace;
//import danogl.components.Transition;
//import danogl.gui.rendering.RectangleRenderable;
//import danogl.util.Vector2;
//
//import java.awt.*;
//
//public class Night {
//    private static final float MIDNIGHT_OPACITY = 0.5f;
//    private static final float ZERO_OPACITY = 0f;
//    public static GameObject create(Vector2 windowDimensions,
//                                    float cycleLength){
//        GameObject night = new GameObject(
//                Vector2.ZERO,
//                windowDimensions,
//                new RectangleRenderable(Color.BLACK)
//        );
//
//        new Transition<>(
//                night,                                   // האובייקט שאליו שייך ה-Transition
//                night.renderer()::setOpaqueness,         // הפונקציה שמשנה את האטימות
//                ZERO_OPACITY,                              // ערך התחלתי
//                MIDNIGHT_OPACITY,                              // ערך סופי
//                Transition.CUBIC_INTERPOLATOR_FLOAT,      // סוג אינטרפולציה חלקה
//                cycleLength,                              // משך המחזור בשניות
//                Transition.TransitionType.TRANSITION_BACK_AND_FORTH, // חזרה אינסופית
//                null                                      // אין callback בסיום
//        );
//
//        return night;
//    }
//}
package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Handles the day-night cycle visual effect by creating a semi-transparent
 * black overlay that fluctuates in opacity over time.
 */
public class Night {
    private static final float MIDNIGHT_OPACITY = 0.5f;
    private static final float ZERO_OPACITY = 0f;
    private static final String NIGHT_TAG = "night";

    /**
     * Creates a full-screen black overlay that transitions between transparent and semi-opaque.
     * * @param windowDimensions The dimensions of the game window to cover.
     * @param cycleLength      The time in seconds it takes for the cycle to transition
     * from day to night (or vice versa).
     * @return A GameObject representing the night overlay.
     */
    public static GameObject create(Vector2 windowDimensions,
                                    float cycleLength){
        GameObject night = new GameObject(
                Vector2.ZERO,
                windowDimensions,
                new RectangleRenderable(Color.BLACK)
        );
        // By setting the coordinate space to CAMERA_COORDINATES, the overlay
        // stays fixed to the screen regardless of where the camera moves in the world.
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        night.setTag(NIGHT_TAG);
        // Initialize the cycle transition
        new Transition<>(
                night,
                night.renderer()::setOpaqueness,
                ZERO_OPACITY,
                MIDNIGHT_OPACITY,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );

        return night;
    }
}