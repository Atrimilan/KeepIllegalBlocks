package io.github.atrimilan.keepillegalblocks.services;

import io.github.atrimilan.keepillegalblocks.utils.blocks.FragileBlockUtils;
import io.github.atrimilan.keepillegalblocks.utils.DebugUtils;
import io.github.atrimilan.keepillegalblocks.utils.blocks.InteractableBlockUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.INFO;

public class BlockDependencyService {

    private final JavaPlugin plugin;

    private static final int MAX_BLOCKS = 1024;

    private static final BlockFace[] FACES = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
                                              BlockFace.EAST, BlockFace.WEST};

    public BlockDependencyService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Perform a Breadth-First Search (BFS) to record all fragile blocks states.
     *
     * @param sourceBlock The interactable block that the player interacted with
     * @return List of states of all fragile blocks connected to the source block
     */
    public List<BlockState> recordFragileBlockStates(Block sourceBlock) {
        if (sourceBlock == null) return Collections.emptyList();

        Queue<Block> queue = new ArrayDeque<>(MAX_BLOCKS);
        Set<Location> visited = HashSet.newHashSet(MAX_BLOCKS);
        List<BlockState> statesToSave = new ArrayList<>(MAX_BLOCKS);

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
                if (!visited.contains(relativeLoc) && visited.size() < MAX_BLOCKS && FragileBlockUtils.isFragile(relative)) {
                    visited.add(relativeLoc); // Mark as visited
                    queue.add(relative); // Add to queue for next BFS iteration
                }
            }
        }

        DebugUtils.sendChat(() -> "Fragile blocks count: <white>" + statesToSave.size() + "<grey>/" + MAX_BLOCKS, INFO);
        return statesToSave;
    }

    // TODO - Schedule the following task in 1 tick (not 2).
    //   At the moment, we need to wait an additional tick because some blocks are destroyed in cascade (tick by tick).
    //   Known blocks: CACTUS, CAVE_VINES, CAVE_VINES_PLANT, CHORUS_FLOWER, CHORUS_PLANT, POINTED_DRIPSTONE, SCAFFOLDING,
    //   SUGAR_CANE, TWISTING_VINES, TWISTING_VINES_PLANT, WEEPING_VINES, WEEPING_VINES_PLANT.

    public void scheduleRestoration(List<BlockState> fragileBlockStates) {
        if (fragileBlockStates.isEmpty()) return; // Return if there's nothing to restore

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {

            // Restore any fragile block that have been replaced by air
            for (BlockState oldState : fragileBlockStates) {
                Block currentBlock = oldState.getBlock();
                if (currentBlock.getType().isAir() && !oldState.getType().isAir())
                    oldState.update(true, false); // Force restore without physic
            }

            // Restore the first block if it causes an additional update (e.g. a button) and if there are adjacent fragile blocks
            BlockState firstState = fragileBlockStates.getFirst();
            if (InteractableBlockUtils.willTriggerAdditionalUpdate(firstState) && fragileBlockStates.size() > 1)
                firstState.update(true, false); // Force restore without physic

        }, 2L); // Schedule restoration in 2 ticks
    }
}
