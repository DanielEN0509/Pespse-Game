package pepse.world.trees;

import danogl.GameObject;
import danogl.util.Vector2;
import pepse.world.Block;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;


public class Flora {
    private static final float TRUNK_WIDTH = 20f;
    private static final float MIN_TRUNK_HEIGHT = 100f;
    private static final float MAX_TRUNK_HEIGHT = 150f;

    private static final float TREE_PROBABILITY = 0.4f;
    private static final int TREE_GRID_INTERVAL = 4;
    private static final int SEED_SALT = 1234;

    private static final int CANOPY_SIZE = 5;
    private static final float FRUIT_PROBABILITY = 0.35f;
    private static final float LEAF_SIZE_DIVIDER = 2f;
    private static final float LEAF_SPACING_FACTOR = 1.2f;
    private static final float CENTER_DIVIDER = 2f;

    private final Function<Float, Float> groundHeightAt;
    private final Consumer<Float> addEnergyCallback;
    private final Random random = new Random();

    /**
     * Constructs a Flora generator.
     * @param groundHeightAt A function that provides terrain height for a given X coordinate.
     * @param addEnergyCallback A callback to be executed when a fruit is consumed.
     */
    public Flora(Function<Float, Float> groundHeightAt, Consumer<Float> addEnergyCallback) {
        this.groundHeightAt = groundHeightAt;
        this.addEnergyCallback = addEnergyCallback;
    }

    /**
     * Scans a range and determines where trees should be placed.
     */
    public List<GameObject> createInRange(int minX, int maxX) {
        List<GameObject> treeParts = new ArrayList<>();

        // Align boundaries to block grid
        int startX = (int) (Math.floor((float) minX / Block.SIZE) * Block.SIZE);
        int endX = (int) (Math.ceil((float) maxX / Block.SIZE) * Block.SIZE);

        for (int x = startX; x < endX; x += Block.SIZE) {
            random.setSeed(Objects.hash(x, SEED_SALT));

            // Only attempt to place trees at specific grid intervals
            if (x % (Block.SIZE * TREE_GRID_INTERVAL) == 0) {
                if (random.nextFloat() < TREE_PROBABILITY) {
                    float yGround = groundHeightAt.apply((float) x);
                    treeParts.addAll(buildTree(x, yGround));
                }
            }
        }
        return treeParts;
    }

    /**
     * Orchestrates the building of a single tree.
     * Acts as a mini-factory for tree parts.
     */
    private List<GameObject> buildTree(float x, float yGround) {
        List<GameObject> parts = new ArrayList<>();
        Random treeRandom = new Random(Objects.hash(x, SEED_SALT));

        // 1. Determine dimensions
        float trunkHeight = MIN_TRUNK_HEIGHT + treeRandom.nextFloat() * (MAX_TRUNK_HEIGHT - MIN_TRUNK_HEIGHT);
        float alignedY = (float) Math.floor(yGround / Block.SIZE) * Block.SIZE;
        Vector2 trunkPos = new Vector2(x, alignedY - trunkHeight);

        // 2. Create Trunk
        parts.add(new Trunk(trunkPos, new Vector2(TRUNK_WIDTH, trunkHeight)));

        // 3. Create Canopy
        parts.addAll(buildCanopy(x, alignedY - trunkHeight, treeRandom));

        return parts;
    }

    /**
     * Handles the specific logic of creating the leaf/fruit canopy.
     */
    private List<GameObject> buildCanopy(float centerX, float trunkTopY, Random treeRandom) {
        List<GameObject> canopyParts = new ArrayList<>();

        float leafSize = Block.SIZE / LEAF_SIZE_DIVIDER;
        float stepSize = leafSize * LEAF_SPACING_FACTOR;
        float totalSize = CANOPY_SIZE * stepSize;

        float startX = (centerX + TRUNK_WIDTH / CENTER_DIVIDER) - (totalSize / CENTER_DIVIDER);
        float startY = trunkTopY - (totalSize / CENTER_DIVIDER);

        for (int i = 0; i < CANOPY_SIZE; i++) {
            for (int j = 0; j < CANOPY_SIZE; j++) {
                Vector2 pos = new Vector2(startX + (i * stepSize), startY + (j * stepSize));

                canopyParts.add(new Leaf(pos, new Vector2(leafSize, leafSize)));

                if (treeRandom.nextFloat() < FRUIT_PROBABILITY) {
                    canopyParts.add(new Fruit(pos, new Vector2(leafSize, leafSize), addEnergyCallback));
                }
            }
        }
        return canopyParts;
    }
}



