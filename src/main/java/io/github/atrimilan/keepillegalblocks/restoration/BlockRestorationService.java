package io.github.atrimilan.keepillegalblocks.restoration;

import io.github.atrimilan.keepillegalblocks.configuration.KibConfig;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import io.github.atrimilan.keepillegalblocks.packets.PacketEventsAdapter;
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
     * @param maxBlocks   The maximum number of blocks to record
     * @return The interactable source block and a set of fragile blocks that may need to be restored
     */
    public BfsResult recordFragileBlockStates(Block sourceBlock, int maxBlocks) {
        if (sourceBlock == null) return null;

        Queue<Block> queue = new ArrayDeque<>();
        Set<Location> visited = new HashSet<>();
        Set<BlockState> fragileBlocks = new HashSet<>();

        Location sourceBlockLoc = sourceBlock.getLocation();
        visited.add(sourceBlockLoc);
        queue.add(sourceBlock);

        // BFS (stop when queue is empty, or maxBlocks is reached)
        while (!queue.isEmpty() && fragileBlocks.size() <= maxBlocks) {
            Block currentBlock = queue.poll();

            if (!currentBlock.getLocation().equals(sourceBlockLoc)) { // Don't save the interactable source block
                fragileBlocks.add(currentBlock.getState()); // Save fragile block state
            }

            // Scan all 6 faces
            for (BlockFace face : FACES) {
                Block relative = currentBlock.getRelative(face);
                Location relativeLoc = relative.getLocation();

                if (!visited.contains(relativeLoc) && visited.size() <= maxBlocks &&
                    config.isFragile(relative.getType())) {
                    visited.add(relativeLoc); // Mark as visited
                    queue.add(relative); // Add to queue for next BFS iteration
                }
            }
        }

        DebugUtils.sendChat(() -> "Fragile blocks count: <white>" + fragileBlocks.size() + "<gray>/" + maxBlocks, INFO);
        return new BfsResult(sourceBlock.getState(), fragileBlocks);
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
     * @param bfsResult The interactable source block and a set of fragile blocks that may need to be restored
     */
    public void scheduleRestoration(BfsResult bfsResult) {
        if (bfsResult == null) return; // Return if there's nothing to restore

        Object packetEventsListener = config.isPacketEventsPresent() ?
                                      PacketEventsAdapter.registerFragileBlockBreakListener(bfsResult.fragileBlocks()) :
                                      null;

        plugin.getServer().getScheduler() // Schedule restoration in 2 ticks
                .runTaskLater(plugin, () -> applyRestoration(bfsResult, packetEventsListener), 2L);
    }

    private void applyRestoration(BfsResult bfsResult, Object packetEventsListener) {
        // Unregister fragile block break PacketEvents listener (if plugin is present)
        if (packetEventsListener != null) {
            PacketEventsAdapter.unregisterListener(packetEventsListener);
        }

        Set<BlockState> fragileBlockStates = bfsResult.fragileBlocks();
        BlockState interactableBlock = bfsResult.interactableBlock();

        // Restore any fragile block that have been replaced by air
        for (BlockState fragileBlockState : fragileBlockStates) {
            if (wasReplacedByAir(fragileBlockState))
                fragileBlockState.update(true, false); // Force restore without physic
        }

        // Restore the interactable block if it has been replaced by air
        // Or if it will cause an additional update (e.g. a button) and there are adjacent fragile blocks
        if (wasReplacedByAir(interactableBlock) ||
            (willTriggerAdditionalUpdate(interactableBlock) && fragileBlockStates.size() > 1)) {
            interactableBlock.update(true, false); // Force restore without physic
        }
    }

    boolean wasReplacedByAir(BlockState blockState) {
        Block currentBlock = blockState.getBlock();
        return currentBlock.getType() == Material.AIR && blockState.getType() != Material.AIR;
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
