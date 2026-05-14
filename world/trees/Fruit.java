package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.ScheduledTask;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.world.avatar.Avatar;
import java.awt.Color;
import java.util.function.Consumer;

public class Fruit extends GameObject {
    /** The tag used to identify fruit objects in the game. */
    public static final String FRUIT_TAG = "fruit";
    private static final Color FRUIT_COLOR = Color.RED;

    private static final float INVISIBLE = 0f;
    private static final float VISIBLE = 1f;

    private static final float RESPAWN_TIME = 30f;
    private static final float ENERGY_BOOST = 10f;

    private final Consumer<Float> energyAdder;

    /**
     * Constructs a new Fruit instance.
     * @param topLeftCorner The position of the fruit.
     * @param dimensions The size of the fruit.
     * @param energyAdder A callback function to increase the Avatar's energy.
     */
    public Fruit(Vector2 topLeftCorner, Vector2 dimensions, Consumer<Float> energyAdder) {
        super(topLeftCorner, dimensions, new OvalRenderable(FRUIT_COLOR));
        this.energyAdder = energyAdder;
        setTag(FRUIT_TAG);
        // Ensure the fruit does not cause physical displacement during collisions
        this.physics().preventIntersectionsFromDirection(null);
    }

    /**
     * Detects collision with the Avatar. If a collision occurs and the fruit is currently
     * visible (available), the fruit is consumed.
     * @param other The GameObject collided with.
     * @param collision Collision data.
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        // Check if the collider is the Avatar and if the fruit is currently available
        if (other.getTag().equals(Avatar.AVATAR_TAG) && renderer().getOpaqueness() > 0) {
            eat();
        }
    }

    /**
     * Handles the consumption logic: provides energy, hides the fruit,
     * and schedules its reappearance.
     */
    private void eat() {
        energyAdder.accept(ENERGY_BOOST);
        renderer().setOpaqueness(INVISIBLE);
        new ScheduledTask(this, RESPAWN_TIME, false, () -> {
            renderer().setOpaqueness(VISIBLE);
        });
    }
}