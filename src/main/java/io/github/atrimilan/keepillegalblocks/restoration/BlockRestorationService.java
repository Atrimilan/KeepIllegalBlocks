package io.github.atrimilan.keepillegalblocks.restoration;

import io.github.atrimilan.keepillegalblocks.configuration.KibConfig;
import io.github.atrimilan.keepillegalblocks.utils.DebugUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.INFO;

public class BlockRestorationService {

    private final JavaPlugin plugin;
    private final KibConfig config;

    private static final int MAX_BLOCKS = 1024;

    private static final BlockFace[] FACES = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
                                              BlockFace.EAST, BlockFace.WEST};

    public BlockRestorationService(JavaPlugin plugin, KibConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Perform a Breadth-First Search (BFS) to record all fragile blocks states.
     *
     * @param sourceBlock The interactable block that the player interacted with
     * @return List of states of all fragile blocks connected to the source block
     */
    public List<BlockState> recordFragileBlockStates(Block sourceBlock) {
        if (sourceBlock == null) return Collections.emptyList();

        Queue<Block> queue = new ArrayDeque<>();
        Set<Location> visited = new HashSet<>();
        List<BlockState> statesToSave = new ArrayList<>();

        visited.add(sourceBlock.getLocation());
        queue.add(sourceBlock);

        // BFS (stop when queue is empty, or MAX_BLOCKS is reached)
        while (!queue.isEmpty() && statesToSave.size() < MAX_BLOCKS) {
            Block currentBlock = queue.poll();
            statesToSave.add(currentBlock.getState()); // Save block state

            // Scan all 6 faces
            for (BlockFace face : FACES) {
                Block relative = currentBlock.getRelative(face);
                Location relativeLoc = relative.getLocation();

                if (!visited.contains(relativeLoc) && visited.size() < MAX_BLOCKS &&
                    config.isFragile(relative.getType())) {
                    visited.add(relativeLoc); // Mark as visited
                    queue.add(relative); // Add to queue for next BFS iteration
                }
            }
        }

        DebugUtils.sendChat(() -> "Fragile blocks count: <white>" + statesToSave.size() + "<gray>/" + MAX_BLOCKS, INFO);
        return statesToSave;
    }

    // TODO - Schedule restoration in 1 tick for non-cascade-destructible fragile blocks

    /**
     * Schedule restoration of fragile blocks that might have been broken after the update of an interactable block.
     * <p>
     * Note: Restoration is scheduled in 2 ticks, because some fragile blocks are not broken within the first tick.
     * Known blocks doing so are blocks breaking in cascade (tick by tick): {@code CACTUS}, {@code CAVE_VINES},
     * {@code CAVE_VINES_PLANT}, {@code CHORUS_FLOWER}, {@code CHORUS_PLANT}, {@code POINTED_DRIPSTONE},
     * {@code SCAFFOLDING}, {@code SUGAR_CANE}, {@code TWISTING_VINES}, {@code TWISTING_VINES_PLANT},
     * {@code WEEPING_VINES}, {@code WEEPING_VINES_PLANT}.
     *
     * @param fragileBlockStates List of fragile blocks to restore (if they have been broken)
     */
    public void scheduleRestoration(List<BlockState> fragileBlockStates) {
        if (fragileBlockStates.isEmpty()) return; // Return if there's nothing to restore

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {

            // Restore any fragile block that have been replaced by air
            for (BlockState oldState : fragileBlockStates) {
                Block currentBlock = oldState.getBlock();
                if (currentBlock.getType() == Material.AIR && oldState.getType() != Material.AIR)
                    oldState.update(true, false); // Force restore without physic
            }

            // Restore the first block if it causes an additional update (e.g. a button) and if there are adjacent fragile blocks
            BlockState firstState = fragileBlockStates.getFirst();
            if (willTriggerAdditionalUpdate(firstState) && fragileBlockStates.size() > 1)
                firstState.update(true, false); // Force restore without physic

        }, 2L); // Schedule restoration in 2 ticks
    }

    /**
     * Check if the interactable block will trigger an additional update. For example, a button will reset to its
     * initial state 1 second after being pressed.
     *
     * @param state The block state to check
     * @return True if the block will trigger an additional update, false otherwise
     */
    boolean willTriggerAdditionalUpdate(BlockState state) {
        return Tag.BUTTONS.isTagged(state.getType());
    }
}
