package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.gui.*;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.avatar.Avatar;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Flora;
import pepse.world.trees.Fruit;
import pepse.world.trees.Leaf;
import pepse.world.trees.Trunk;

import java.awt.*;
import java.util.function.Supplier;

public class PepseGameManager extends GameManager {
    private static final float CYCLE_LENGTH = 30f;
    private static final int WORLD_SEED = 1234;
    private static final int CHUNK_SIZE = Block.SIZE * 10;
    private static final float HALF_FACTOR = 0.5f;
    private static final float SUN_CYCLE_MODIFIER = 2f;
    private static final int TEXT_Y_OFFSET = 25;

    private static final Vector2 ENERGY_DISPLAY_POS = new Vector2(20, 20);
    private static final Vector2 ENERGY_DISPLAY_SIZE = new Vector2(200, 50);
    private static final String FONT_NAME = "Courier New";
    private static final int FONT_SIZE = 20;
    private static final String GROUND_TAG = "ground";
    private static final String BLOCK_TAG = "block";

    private Terrain terrain;
    private Flora flora;
    private int curMinX;
    private int curMaxX;
    private WindowController windowController;

    /**
     * Entry point of the application.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        new PepseGameManager().run();
    }

    /**
     * Initializes the game state, world infrastructure, and the avatar.
     */
    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader,
                               UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        this.windowController = windowController;
        Vector2 windowDimensions = windowController.getWindowDimensions();

        // Initialize World Infrastructure
        this.terrain = new Terrain(windowDimensions, WORLD_SEED);
        // Add Background Elements
        gameObjects().addGameObject(Sky.create(windowDimensions), Layer.BACKGROUND);
        gameObjects().addGameObject(Night.create(windowDimensions, CYCLE_LENGTH), Layer.BACKGROUND);
        GameObject sun = Sun.create(windowDimensions, CYCLE_LENGTH * SUN_CYCLE_MODIFIER);
        gameObjects().addGameObject(sun, Layer.BACKGROUND);
        gameObjects().addGameObject(SunHalo.create(sun), Layer.BACKGROUND);

        // Avatar Initialization (Centered on screen)
        float centerX = windowDimensions.x() * HALF_FACTOR;
        float rawGroundY = terrain.groundHeightAt(centerX);
        float blockTopY = (float) Math.floor(rawGroundY / Block.SIZE) * Block.SIZE;
        Vector2 avatarTopLeft = new Vector2(centerX - Avatar.AVATAR_SIZE.x() * HALF_FACTOR,
                blockTopY - Avatar.AVATAR_SIZE.y());

        Avatar avatar = new Avatar(avatarTopLeft, inputListener, imageReader);
        gameObjects().addGameObject(avatar, Layer.DEFAULT);

        // Camera Setup
        setCamera(new Camera(avatar, Vector2.ZERO, windowDimensions, windowDimensions));

        // Flora Initialization
        this.flora = new Flora(terrain::groundHeightAt, avatar::addEnergy);

        // Initial World Creation (Infinite chunk boundaries)
        curMinX = (int) (centerX - windowDimensions.x());
        curMaxX = (int) (centerX + windowDimensions.x());
        // Snapping boundaries to CHUNK_SIZE grid
        curMinX = (curMinX / CHUNK_SIZE) * CHUNK_SIZE;
        curMaxX = (curMaxX / CHUNK_SIZE) * CHUNK_SIZE;

        createWorldInRange(curMinX, curMaxX);
        // Collision Rules
        gameObjects().layers().shouldLayersCollide(Layer.DEFAULT, Layer.STATIC_OBJECTS, true);
        // Collision Rules
        gameObjects().addGameObject(getEnergyDisplay(avatar), Layer.BACKGROUND);
    }

    /**
     * Updates the game every frame. Checks if the camera has moved enough to trigger
     * the creation of new chunks or removal of distant ones.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        float cameraX = camera().getTopLeftCorner().x();
        float windowWidth = windowController.getWindowDimensions().x();

        // Check if we need to load a new chunk to the right
        if (cameraX + windowWidth > curMaxX) {
            createWorldInRange(curMaxX, curMaxX + CHUNK_SIZE);
            removeWorldInRange(curMinX, curMinX + CHUNK_SIZE);
            curMaxX += CHUNK_SIZE;
            curMinX += CHUNK_SIZE;
        }

        // Check if we need to load a new chunk to the left
        if (cameraX < curMinX) {
            createWorldInRange(curMinX - CHUNK_SIZE, curMinX);
            removeWorldInRange(curMaxX - CHUNK_SIZE, curMaxX);
            curMinX -= CHUNK_SIZE;
            curMaxX -= CHUNK_SIZE;
        }
    }

    /**
     * Creates terrain and flora objects within a specific X range.
     * @param minX Start of range.
     * @param maxX End of range.
     */
    private void createWorldInRange(int minX, int maxX) {
        // Create ground blocks
        for (Block block : terrain.createInRange(minX, maxX)) {
            gameObjects().addGameObject(block, Layer.STATIC_OBJECTS);
        }
        // Create trees (trunks, leaves, fruits)
        for (GameObject obj : flora.createInRange(minX, maxX)) {
            int layer = obj.getTag().equals(Fruit.FRUIT_TAG) ? Layer.DEFAULT : Layer.STATIC_OBJECTS;
            gameObjects().addGameObject(obj, layer);
        }
    }

    /**
     * Removes world objects that fall outside the active range to save memory.
     * @param minX Start of range to remove.
     * @param maxX End of range to remove.
     */
    private void removeWorldInRange(int minX, int maxX) {
        for (GameObject obj : gameObjects()) {
            float objX = obj.getTopLeftCorner().x();
            if (objX >= minX && objX < maxX) {
                String tag = obj.getTag();
                // Ensure only world objects are removed, avoiding background or UI
                if (tag.equals(BLOCK_TAG) || tag.equals(GROUND_TAG) || tag.equals(Trunk.TRUNK_TAG) ||
                        tag.equals(Leaf.LEAF_TAG) || tag.equals(Fruit.FRUIT_TAG)) {

                    int layer = tag.equals(Fruit.FRUIT_TAG) ? Layer.DEFAULT : Layer.STATIC_OBJECTS;
                    gameObjects().removeGameObject(obj, layer);
                }
            }
        }
    }

    /**
     * Creates the energy level display for the avatar.
     * @param avatar The player object.
     * @return A GameObject that renders the energy text.
     */
    private GameObject getEnergyDisplay(Avatar avatar) {
        Supplier<Integer> energySupplier = () -> (int) avatar.getEnergy();
        GameObject energy = new GameObject(ENERGY_DISPLAY_POS, ENERGY_DISPLAY_SIZE,
                (g, topLeft, dims, deg, flipH, flipV, opaque)
                        -> {
                    g.setColor(Color.BLACK);
                    g.setFont(new Font(FONT_NAME, Font.BOLD, FONT_SIZE));
                    g.drawString("Energy: " + energySupplier.get(), (int)topLeft.x(),
                            (int)topLeft.y() + TEXT_Y_OFFSET);
                });
        // Ensure UI stays fixed relative to the camera
        energy.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        return energy;
    }
}