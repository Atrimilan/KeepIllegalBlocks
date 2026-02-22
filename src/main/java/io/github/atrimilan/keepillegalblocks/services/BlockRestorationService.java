package io.github.atrimilan.keepillegalblocks.services;

import io.github.atrimilan.keepillegalblocks.configuration.KibConfig;
import io.github.atrimilan.keepillegalblocks.configuration.types.FragileType;
import io.github.atrimilan.keepillegalblocks.configuration.types.InteractableType;
import io.github.atrimilan.keepillegalblocks.listeners.ItemSpawnListener;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import io.github.atrimilan.keepillegalblocks.models.InteractableWrapper;
import io.github.atrimilan.keepillegalblocks.packets.PacketEventsAdapter;
import io.github.atrimilan.keepillegalblocks.utils.DebugUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.BoundingBox;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.INFO;

public class BlockRestorationService {

    private final JavaPlugin plugin;
    private final KibConfig config;

    private static final BlockFace[] FACES = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
                                              BlockFace.EAST, BlockFace.WEST};

    public BlockRestorationService(KibConfig config) {
        this.config = config;
        this.plugin = config.getPlugin();
    }

    /**
     * Perform a Breadth-First Search (BFS) to record all fragile blocks states, and calculate their bounding box.
     *
     * @param sourceBlock The interactable block that the player interacted with
     * @param maxBlocks   The maximum number of blocks to record
     * @return All block states (interactable and fragile) and their bounding box.
     */
    public BfsResult recordFragileBlockStates(Block sourceBlock, int maxBlocks) {
        if (sourceBlock == null || maxBlocks <= 0) return null;

        // Initialize bounding box boundaries
        int minX = sourceBlock.getX(), maxX = sourceBlock.getX();
        int minY = sourceBlock.getY(), maxY = sourceBlock.getY();
        int minZ = sourceBlock.getZ(), maxZ = sourceBlock.getZ();

        // Initialize BFS variables
        Queue<Block> queue = new ArrayDeque<>();
        Set<Location> visited = new HashSet<>();
        Set<BlockState> fragileBlocks = new HashSet<>();

        Location sourceBlockLoc = sourceBlock.getLocation();
        visited.add(sourceBlockLoc);
        queue.add(sourceBlock);

        // BFS (stop when queue is empty, or maxBlocks is reached)
        while (!queue.isEmpty() && fragileBlocks.size() <= maxBlocks) {
            Block currentBlock = queue.poll();

            if (currentBlock != sourceBlock) { // Skip interactable source block
                fragileBlocks.add(currentBlock.getState()); // Save fragile block state

                // Update bounding box
                minX = Math.min(minX, currentBlock.getX());
                maxX = Math.max(maxX, currentBlock.getX());
                minY = Math.min(minY, currentBlock.getY());
                maxY = Math.max(maxY, currentBlock.getY());
                minZ = Math.min(minZ, currentBlock.getZ());
                maxZ = Math.max(maxZ, currentBlock.getZ());
            }

            // Scan all 6 faces
            for (BlockFace face : FACES) {
                Block relative = currentBlock.getRelative(face);
                Location relativeLoc = relative.getLocation();

                if (!visited.contains(relativeLoc) && config.isFragile(relative.getType())) {
                    visited.add(relativeLoc); // Mark location as visited
                    queue.add(relative); // Add to queue for next BFS iteration
                }
            }
        }

        DebugUtils.sendChat(() -> "Fragile blocks count: <white>" + (fragileBlocks.size()) + "<gray>/" + maxBlocks,
                            INFO);

        boolean isInteractableAlsoFragile = config.isFragile(sourceBlock.getType());
        var interactable = new InteractableWrapper(sourceBlock.getState(), isInteractableAlsoFragile);
        var boundingBox = new BoundingBox(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);

        return new BfsResult(interactable, fragileBlocks, boundingBox);
    }

    /**
     * Schedule restoration of fragile blocks that might have been broken after the update of an interactable block.
     * <li>The initial restoration is scheduled in 2 ticks, because some fragile blocks are not broken within the first
     * tick. See which blocks are involved in {@link FragileType}.</li>
     * <li>If the interactable block will trigger a second update (such as a button), an additional restoration is
     * scheduled after a delay (which depends on the {@link InteractableType}).</li>
     *
     * @param bfsResult All block states (interactable and fragile) and their bounding box.
     */
    public void scheduleRestoration(BfsResult bfsResult, InteractableType interactableType) {
        if (bfsResult == null || !bfsResult.hasFragileBlocks()) return; // Return if there's nothing to restore

        ItemSpawnListener itemSpawnListener = new ItemSpawnListener(bfsResult, plugin);
        Object packetListener = config.isPacketEventsPresent() ? //
                                PacketEventsAdapter.registerFragileBlockBreakListener(bfsResult) : null;

        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        // Schedule initial restoration in 2 ticks
        scheduler.runTaskLater(plugin, () -> {
            applyRestoration(bfsResult.getAllFragileBlocks()); // Apply restoration

            long delayBeforeSecondUpdate = interactableType.getDelayBeforeSecondUpdate();
            boolean hasSecondUpdate = delayBeforeSecondUpdate > 0;

            if (!hasSecondUpdate) {
                unregisterListeners(packetListener, itemSpawnListener);

            } else {
                // If interactable type has a second update, schedule another restoration
                scheduler.runTaskLater(plugin, () -> {
                    applyRestoration(bfsResult.getAllFragileBlocks());
                    unregisterListeners(packetListener, itemSpawnListener);
                }, delayBeforeSecondUpdate); // Delay depends on the interactable type
            }
        }, 2L);
    }

    /**
     * Restore the fragile blocks that have been broken (including the interactable block if it is also fragile).
     *
     * @param fragileBlocks All fragile block states
     */
    private void applyRestoration(Set<BlockState> fragileBlocks) {
        for (BlockState state : fragileBlocks) {
            if (wasReplacedByAir(state)) {
                state.update(true, false); // Force restore without physic
            }
        }
    }

    /**
     * Unregister fragile block break PacketEvents listener (if plugin is present)
     *
     * @param packetListener The PacketEvents listener to unregister
     */
    private void unregisterListeners(Object packetListener, ItemSpawnListener itemSpawnListener) {
        if (packetListener != null) PacketEventsAdapter.unregisterListener(packetListener);
        if (itemSpawnListener != null) itemSpawnListener.unregister();
    }

    /**
     * @param state The block state to check
     * @return Whether the block has been replaced by air.
     */
    boolean wasReplacedByAir(BlockState state) {
        Block currentBlock = state.getBlock();
        return currentBlock.getType() == Material.AIR && state.getType() != Material.AIR;
    }
}
