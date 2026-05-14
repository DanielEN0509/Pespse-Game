package pepse.world;

import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.util.NoiseGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for generating and managing the ground terrain.
 * Uses Perlin-like noise to create organic hills and manages block placement
 * within a grid system to support infinite world generation.
 */
public class Terrain {
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
    private static final int TERRAIN_DEPTH = 20;
    private static final String GROUND_TAG = "ground";

    private static final float GROUND_HEIGHT_FACTOR = 2f / 3f;
    private static final int NOISE_SMOOTHNESS_FACTOR = 7;
    private final float groundHeightAtX0;
    private final NoiseGenerator noiseGenerator;

    /**
     * Constructs a Terrain generator.
     * @param windowDimensions The dimensions of the game window.
     * @param seed The seed for deterministic noise generation.
     */
    public Terrain(Vector2 windowDimensions, int seed) {
        this.groundHeightAtX0 = windowDimensions.y() * GROUND_HEIGHT_FACTOR;
        this.noiseGenerator = new NoiseGenerator(seed, (int) groundHeightAtX0);
    }

    /**
     * Calculates the height of the terrain at a specific X coordinate.
     * @param x The horizontal coordinate.
     * @return The vertical coordinate of the ground top at the given X.
     */
    public float groundHeightAt(float x) {
        float noise = (float) noiseGenerator.noise(x, Block.SIZE *NOISE_SMOOTHNESS_FACTOR);
        return groundHeightAtX0 + noise;
    }


    /**
     * Creates a list of ground blocks within the specified horizontal range.
     * @param minX The starting X coordinate.
     * @param maxX The ending X coordinate.
     * @return A list of blocks composing the terrain.
     */
    public List<Block> createInRange(int minX, int maxX) {
        List<Block> blocks = new ArrayList<>();

        int startX = alignToGrid(minX);
        int endX = (int) (Math.ceil((float) maxX / Block.SIZE) * Block.SIZE);

        for (int x = startX; x < endX; x += Block.SIZE) {
            createColumn(x, blocks);
        }
        return blocks;
    }

    /**
     * Private helper to create a vertical column of blocks.
     * SRP Improvement: Separating column logic from range logic.
     */
    private void createColumn(int x, List<Block> blocks) {
        float rawHeight = groundHeightAt(x);
        int topBlockY = alignToGrid((int) rawHeight);

        for (int i = 0; i < TERRAIN_DEPTH; i++) {
            int y = topBlockY + (i * Block.SIZE);
            blocks.add(createGroundBlock(x, y));
        }
    }

    /**
     * Factory method for ground blocks.
     * Encapsulates block configuration.
     */
    private Block createGroundBlock(int x, int y) {
        Block block = new Block(
                new Vector2(x, y),
                new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR))
        );
        block.setTag(GROUND_TAG);
        return block;
    }

    /** Helper to align any value to the Block grid. */
    private int alignToGrid(float value) {
        return (int) (Math.floor(value / Block.SIZE) * Block.SIZE);
    }
}

