package pepse.world.avatar;

import danogl.GameObject;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.ImageRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import danogl.gui.ImageReader;

import java.awt.event.KeyEvent;


/**
 * Represents the main player character (Avatar) in the PEPSE game.
 * Handles movement, jumping (including double jumps), energy management, and animations.
 */
public class Avatar extends GameObject {

    /** The maximum amount of energy the avatar can have. */
    public static final float MAX_ENERGY = 100f;
    /** Energy cost per second while the avatar is running. */
    public static final float RUN_ENERGY_COST_PER_SECOND = 10f;
    /** Energy cost for a single jump from the ground. */
    public static final float JUMP_ENERGY_COST = 20f;
    /** Energy cost for a double jump performed in mid-air. */
    public static final float DOUBLE_JUMP_ENERGY_COST_MID_AIR = 50f;
    /** Default visual size of the avatar. */
    public static final String AVATAR_TAG = "avatar";

    /** Internal states for the Avatar, ensuring type safety. */
    private enum State { IDLE, RUN, JUMP, DOUBLE_JUMP }

    private static final float ENERGY_REPLENISH_RATE = 0.2f;
    private static final float ENERGY_INCREMENT = 1.0f;
    private static final float VELOCITY_X = 400f;
    private static final float VELOCITY_Y = -650f;
    private static final float GRAVITY = 1000f;
    private static final float DOUBLE_JUMP_VELOCITY_FACTOR = 1.25f;
    private static final float ON_GROUND_VELOCITY_THRESHOLD = 0.5f;
    private static final float ZERO_VELOCITY = 0f;
    private static final float TERMINAL_VELOCITY = 500f;
    public static final Vector2 AVATAR_SIZE = new Vector2(50, 50);

    private static final float RUN_ANIM_TIME = 0.1f;
    private static final float JUMP_ANIM_TIME = 0.2f;
    private static final float IDLE_ANIM_TIME = 0.5f;


    private final UserInputListener inputListener;
    private State currentState;
    private boolean doubleJumped;
    private float energy;
    private float energyCooldown = 0f;
    private boolean wasSpacePressed = false;

    private AnimationRenderable sideToSideAnimation;
    private AnimationRenderable upAndDownAnimation;
    private AnimationRenderable standingStillAnimation;

    /**
     * Initializes the Avatar with physics, starting energy, and animations.
     * @param topLeftCorner Initial position.
     * @param inputListener Listener for user keyboard input.
     * @param imageReader Reader to load animation assets.
     */
    public Avatar(Vector2 topLeftCorner, UserInputListener inputListener,
                  ImageReader imageReader) {
        super(topLeftCorner, AVATAR_SIZE,
                new ImageRenderable(imageReader.readImage("assets/idle_0.png", true).getImage())
        );
        this.energy = MAX_ENERGY;
        this.doubleJumped = false;
        this.inputListener = inputListener;
        this.setTag(AVATAR_TAG);
        this.currentState = State.IDLE;

        // Set physics properties
        transform().setAccelerationY(GRAVITY);
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        // Load all character animations
        animation(imageReader);
    }

    /**
     * Loads image sequences and creates AnimationRenderables for different states.
     */
    private void animation(ImageReader imageReader) {
        // RUN
        Renderable[] sideToSide = new Renderable[]{
                imageReader.readImage("assets/run_0.png", true),
                imageReader.readImage("assets/run_1.png", true),
                imageReader.readImage("assets/run_2.png", true),
                imageReader.readImage("assets/run_3.png", true),
                imageReader.readImage("assets/run_4.png", true),
                imageReader.readImage("assets/run_5.png", true)
        };
        sideToSideAnimation = new AnimationRenderable(sideToSide, RUN_ANIM_TIME);
        // JUMP
        Renderable[] upAndDown = new Renderable[]{
                imageReader.readImage("assets/jump_0.png", true),
                imageReader.readImage("assets/jump_1.png", true),
                imageReader.readImage("assets/jump_2.png", true),
                imageReader.readImage("assets/jump_3.png", true)
        };
        upAndDownAnimation = new AnimationRenderable(upAndDown, JUMP_ANIM_TIME);
        // IDLE
        Renderable[] standingStill = new Renderable[]{
                imageReader.readImage("assets/idle_0.png", true),
                imageReader.readImage("assets/idle_1.png", true),
                imageReader.readImage("assets/idle_2.png", true),
                imageReader.readImage("assets/idle_3.png", true)
        };
        standingStillAnimation = new AnimationRenderable(standingStill, IDLE_ANIM_TIME);
    }

    /**
     * Called once per frame. Updates physics, input handling, energy, and animations.
     * * @param deltaTime The time elapsed since the last frame.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        // Limit falling speed
        if (getVelocity().y() > TERMINAL_VELOCITY) {
            setVelocity(new Vector2(getVelocity().x(), TERMINAL_VELOCITY));
        }
        handleMovement(deltaTime);
        handleJump();
        replenishEnergy(deltaTime);
        updateStateAndAnimation();
    }

    /**
     * Handles horizontal movement and energy consumption while running.
     */
    private void handleMovement(float deltaTime) {
        float xVel = ZERO_VELOCITY;
        boolean leftPressed = inputListener.isKeyPressed(KeyEvent.VK_LEFT);
        boolean rightPressed = inputListener.isKeyPressed(KeyEvent.VK_RIGHT);
        // Determine direction and flip sprite accordingly
        if (leftPressed && !rightPressed) {
            xVel -= VELOCITY_X;
            renderer().setIsFlippedHorizontally(true);
        } else if (rightPressed && !leftPressed) {
            xVel += VELOCITY_X;
            renderer().setIsFlippedHorizontally(false);
        }
        // Apply running energy cost only if on ground and moving
        if (isOnGround() && xVel != ZERO_VELOCITY) {
            if (canMove()) {
                consumeEnergy(RUN_ENERGY_COST_PER_SECOND * deltaTime);
            } else {
                xVel = ZERO_VELOCITY; // Stop if out of energy
            }
        }
        transform().setVelocityX(xVel);
    }

    /**
     * Handles jump logic including single jump from ground and mid-air double jump.
     */
    private void handleJump() {
        boolean isSpacePressedNow = inputListener.isKeyPressed(KeyEvent.VK_SPACE);
        // Trigger jump only on the frame the key is first pressed
        if (isSpacePressedNow && !wasSpacePressed) {
            if (isOnGround() && energy >= JUMP_ENERGY_COST) {
                jump();
                consumeEnergy(JUMP_ENERGY_COST);
            }
            else if (!doubleJumped && !isOnGround() && energy >= DOUBLE_JUMP_ENERGY_COST_MID_AIR) {
                doubleJump(); // הפונקציה שמשתמשת ב-DOUBLE_JUMP_VELOCITY_FACTOR
                consumeEnergy(DOUBLE_JUMP_ENERGY_COST_MID_AIR);
                doubleJumped = true;
            }
        }
        wasSpacePressed = isSpacePressedNow;
        // Reset double jump flag when landing
        if (isOnGround()) {
            doubleJumped = false;
        }
    }

    /** Updates logical state and assigns corresponding animation. */
    private void updateStateAndAnimation() {
        boolean movingHorizontally = Math.abs(getVelocity().x()) > ZERO_VELOCITY;

        // Transition Logic
        if(isOnGround()) {
            currentState = movingHorizontally ? State.RUN : State.IDLE;
        } else {
            currentState = doubleJumped ? State.DOUBLE_JUMP : State.JUMP;
        }

        // Apply Animation
        switch (currentState) {
            case IDLE:
                renderer().setRenderable(standingStillAnimation);
                break;
            case RUN:
                renderer().setRenderable(sideToSideAnimation);
                break;
            case JUMP:
            case DOUBLE_JUMP:
                renderer().setRenderable(upAndDownAnimation);
                break;
        }
    }

    /**
     * Gets the avatar's current energy level.
     * * @return Current energy as a float.
     */
    public float getEnergy() {
        return energy;
    }
    /**
     * Reduces the avatar's energy by a specified amount, clamped to zero.
     * * @param amount The amount of energy to remove.
     */
    public void consumeEnergy(float amount) {
        energy = Math.max(ZERO_VELOCITY, energy - amount);
    }
    /**
     * Checks if the avatar is currently touching the ground based on its vertical velocity.
     * * @return true if the avatar is stationary vertically, false otherwise.
     */
    public boolean isOnGround() {
        return Math.abs(getVelocity().y()) < ON_GROUND_VELOCITY_THRESHOLD;
    }
    /**
     * Applies a vertical impulse to make the avatar jump.
     */
    public void jump() {
        transform().setVelocityY(VELOCITY_Y);
    }
    /**
     * Applies a vertical impulse to make the avatar jump higher while in mid-air.
     */
    public void doubleJump() {
        transform().setVelocityY(VELOCITY_Y * DOUBLE_JUMP_VELOCITY_FACTOR);
    }
    /**
     * Determines if the avatar has enough energy to perform actions.
     * * @return true if energy is greater than zero, false otherwise.
     */
    private boolean canMove() {
        return energy > ZERO_VELOCITY;
    }
    /**
     * Restores energy over time if the avatar is standing still on the ground.
     * * @param deltaTime The time elapsed since the last frame.
     */
    private void replenishEnergy(float deltaTime) {
        if (isOnGround() && getVelocity().x() == ZERO_VELOCITY) {
            energyCooldown += deltaTime;
            if (energyCooldown >= ENERGY_REPLENISH_RATE) {
                energy = Math.min(MAX_ENERGY, energy + ENERGY_INCREMENT);
                energyCooldown = ZERO_VELOCITY;
            }
        }
    }
    /**
     * Adds a specific amount of energy to the avatar's pool, clamped to the maximum energy.
     * * @param amount The amount of energy to add.
     */
    public void addEnergy(float amount) {
        this.energy = Math.min(MAX_ENERGY, energy + amount);
    }
}
