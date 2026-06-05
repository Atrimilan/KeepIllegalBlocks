package io.github.atrimilan.keepillegalblocks.services;

import io.github.atrimilan.keepillegalblocks.core.MaterialRegistry;
import io.github.atrimilan.keepillegalblocks.core.Settings;
import io.github.atrimilan.keepillegalblocks.core.types.InteractableType;
import io.github.atrimilan.keepillegalblocks.core.types.ReactiveType;
import io.github.atrimilan.keepillegalblocks.listeners.ItemSpawnListener;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import io.github.atrimilan.keepillegalblocks.models.InteractableBlockWrapper;
import io.github.atrimilan.keepillegalblocks.models.ReactiveBlockWrapper;
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
    private final MaterialRegistry materialRegistry;
    private final Settings settings;

    private static final BlockFace[] FACES = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
                                              BlockFace.EAST, BlockFace.WEST};

    public BlockRestorationService(JavaPlugin plugin, MaterialRegistry materialRegistry, Settings settings) {
        this.plugin = plugin;
        this.materialRegistry = materialRegistry;
        this.settings = settings;
    }

    /**
     * Perform a Breadth-First Search (BFS) to record all reactive blocks states, and calculate their bounding box.
     *
     * @param sourceBlock The interactable block that the player interacted with
     * @param maxBlocks   The maximum number of blocks to record
     * @return All block states and their bounding box.
     */
    public BfsResult recordBlockStates(Block sourceBlock, int maxBlocks) {
        if (sourceBlock == null || maxBlocks <= 0) return null;

        // Initialize bounding box boundaries
        int minX = sourceBlock.getX(), maxX = sourceBlock.getX();
        int minY = sourceBlock.getY(), maxY = sourceBlock.getY();
        int minZ = sourceBlock.getZ(), maxZ = sourceBlock.getZ();

        // Initialize BFS variables
        Queue<Block> queue = new ArrayDeque<>();
        Set<Location> visited = new HashSet<>();
        Set<ReactiveBlockWrapper> reactiveBlocks = new HashSet<>();

        Location sourceBlockLoc = sourceBlock.getLocation();
        visited.add(sourceBlockLoc);
        queue.add(sourceBlock);

        int nbBlocks = 0;

        // BFS (stop when queue is empty, or maxBlocks is reached)
        while (!queue.isEmpty() && nbBlocks < maxBlocks) {
            Block currentBlock = queue.poll();

            if (currentBlock != sourceBlock) { // Skip interactable source block
                ReactiveType reactiveType = materialRegistry.getReactiveType(currentBlock.getType());
                if (reactiveType == ReactiveType.NONE) continue;

                reactiveBlocks.add(new ReactiveBlockWrapper(currentBlock.getState(), reactiveType.isConnectable()));
                nbBlocks++;

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

                if (!visited.contains(relativeLoc)) {
                    visited.add(relativeLoc); // Mark location as visited
                    queue.add(relative); // Add to queue for next BFS iteration
                }
            }
        }

        DebugUtils.sendChat(
                () -> "Recorded <white>" + reactiveBlocks.size() + "</white> reactive blocks <gray>(max: " + maxBlocks +
                      ")", INFO);

        boolean isInteractableAlsoReactive = materialRegistry.isReactive(sourceBlock.getType());
        var interactable = new InteractableBlockWrapper(sourceBlock.getState(), isInteractableAlsoReactive);
        var boundingBox = new BoundingBox(minX, minY, minZ, maxX + 1D, maxY + 1D, maxZ + 1D);

        return new BfsResult(interactable, reactiveBlocks, boundingBox);
    }

    /**
     * Schedule restoration of reactive blocks that might have been broken or updated.
     * <li>Tick 1 - Connectable reactive blocks are restored if they have been updated.</li>
     * <li>Tick 2 - Reactive blocks (connectable or not) are restored if they have been broken. This restoration is not
     * scheduled for Tick 1, because the reactive blocks that break in cascade only start breaking starting from Tick 2.
     * See which blocks are involved in {@link ReactiveType}.</li>
     * <li>If the interactable block triggers a second update after a delay (such as a button), an additional
     * restoration is scheduled after that delay. See which blocks are involved in {@link InteractableType}.</li>
     *
     * @param bfsResult All block states and their bounding box.
     */
    public void scheduleRestoration(BfsResult bfsResult, InteractableType interactableType) {
        if (bfsResult == null || !bfsResult.hasBlocksToRestore()) return; // Return if there's nothing to restore

        /* Prepare block sets */

        Set<ReactiveBlockWrapper> reactiveBlocks = bfsResult.getAllReactiveBlocks();
        Set<ReactiveBlockWrapper> connectableReactiveBlocks = new HashSet<>();

        for (ReactiveBlockWrapper reactiveBlock : reactiveBlocks) {
            if (reactiveBlock.isConnectable()) {
                connectableReactiveBlocks.add(reactiveBlock);
            }
        }

        /* Register listeners */

        ItemSpawnListener itemSpawnListener = new ItemSpawnListener(plugin, bfsResult, materialRegistry);
        Object packetListener = settings.isPacketEventsEnabled() ? //
                                PacketEventsAdapter.registerReactiveBlockUpdateListener(bfsResult) : null;

        /* Schedule restorations */

        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        scheduler.runTaskLater(plugin, () -> {
            restoreUpdatedConnectableBlocks(connectableReactiveBlocks); // Restore blocks that may have been updated

            if (interactableType.hasSecondUpdate()) {
                scheduler.runTaskLater(plugin, () -> restoreUpdatedConnectableBlocks(connectableReactiveBlocks),
                                       interactableType.getDelayBeforeSecondUpdate());
            }
        }, 1L);

        scheduler.runTaskLater(plugin, () -> {
            restoreBrokenBlocks(reactiveBlocks); // Restore blocks that may have been broken

            if (interactableType.hasSecondUpdate()) {
                scheduler.runTaskLater(plugin, () -> {
                    restoreBrokenBlocks(reactiveBlocks);
                    unregisterListeners(packetListener, itemSpawnListener);
                }, interactableType.getDelayBeforeSecondUpdate());
            } else {
                unregisterListeners(packetListener, itemSpawnListener);
            }
        }, 2L);
    }

    /**
     * Restore the reactive blocks that have been broken (including the interactable block if it's also reactive).
     *
     * @param reactiveBlockWrappers The set of reactive blocks to check and restore
     */
    private void restoreBrokenBlocks(Set<ReactiveBlockWrapper> reactiveBlockWrappers) {
        for (ReactiveBlockWrapper reactiveBlock : reactiveBlockWrappers) {
            if (wasReplacedByAir(reactiveBlock.state())) {
                reactiveBlock.state().update(true, false); // Force restore without physic
            }
        }
    }

    /**
     * Restore the reactive connectable blocks that have been updated.
     *
     * @param reactiveBlockWrappers The set of reactive blocks to check and restore
     */
    private void restoreUpdatedConnectableBlocks(Set<ReactiveBlockWrapper> reactiveBlockWrappers) {
        for (ReactiveBlockWrapper reactiveBlock : reactiveBlockWrappers) {
            if (wasUpdated(reactiveBlock.state())) {
                reactiveBlock.state().update(true, false); // Force restore without physic
            }
        }
    }

    /**
     * @param state The block state to check
     * @return Whether the block has been replaced by air.
     */
    boolean wasReplacedByAir(BlockState state) {
        Block currentBlock = state.getBlock();
        return currentBlock.getType() == Material.AIR && state.getType() != Material.AIR;
    }

    /**
     * @param state The block state to check
     * @return Whether the block's data has changed.
     */
    boolean wasUpdated(BlockState state) {
        Block currentBlock = state.getBlock();
        return !currentBlock.getBlockData().equals(state.getBlockData());
    }

    /**
     * Unregister event listeners.
     *
     * @param packetListener    The PacketEvents listener to unregister (if the plugin is present)
     * @param itemSpawnListener The ItemSpawnListener to unregister
     */
    private void unregisterListeners(Object packetListener, ItemSpawnListener itemSpawnListener) {
        if (packetListener != null) PacketEventsAdapter.unregisterListener(packetListener);
        if (itemSpawnListener != null) itemSpawnListener.unregister();
    }
}
