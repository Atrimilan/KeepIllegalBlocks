package io.github.atrimilan.keepillegalblocks.restoration;

import io.github.atrimilan.keepillegalblocks.configuration.KibConfig;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import io.github.atrimilan.keepillegalblocks.models.InteractableWrapper;
import io.github.atrimilan.keepillegalblocks.packets.PacketEventsAdapter;
import io.github.atrimilan.keepillegalblocks.utils.DebugUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
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

                if (!visited.contains(relativeLoc) && visited.size() <= maxBlocks &&
                    config.isFragile(relative.getType())) {
                    visited.add(relativeLoc); // Mark as visited
                    queue.add(relative); // Add to queue for next BFS iteration
                }
            }
        }

        DebugUtils.sendChat(() -> "Fragile blocks count: <white>" + fragileBlocks.size() + "<gray>/" + maxBlocks, INFO);

        var interactable = new InteractableWrapper(sourceBlock.getState(), config.isFragile(sourceBlock.getType()));
        var boundingBox = new BoundingBox(minX - 0.5, minY - 0.5, minZ - 0.5, maxX + 1.5, maxY + 1.5, maxZ + 1.5);

        return new BfsResult(interactable, fragileBlocks, boundingBox);
    }

    /**
     * Schedule restoration of fragile blocks that might have been broken after the update of an interactable block.
     * <p>
     * Note: Restoration is scheduled in 2 ticks, because some fragile blocks are not broken within the first tick.
     * Known blocks doing so are blocks breaking in cascade (tick by tick): {@code BAMBOO}, {@code CACTUS},
     * {@code CAVE_VINES}, {@code CAVE_VINES_PLANT}, {@code CHORUS_FLOWER}, {@code CHORUS_PLANT},
     * {@code POINTED_DRIPSTONE}, {@code SCAFFOLDING}, {@code SUGAR_CANE}, {@code TWISTING_VINES},
     * {@code TWISTING_VINES_PLANT}, {@code WEEPING_VINES}, {@code WEEPING_VINES_PLANT}.
     *
     * @param bfsResult All block states (interactable and fragile) and their bounding box.
     */
    public void scheduleRestoration(BfsResult bfsResult) {
        if (bfsResult == null ||
            (bfsResult.fragileBlocks().isEmpty() && !bfsResult.interactableBlock().isAlsoFragile())) {
            return; // Return if there's nothing to restore
        }

        Object packetEventsListener = config.isPacketEventsPresent() ? //
                                      PacketEventsAdapter.registerFragileBlockBreakListener(bfsResult) : null;

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTask(plugin, () -> preventBlocksToDropItem(bfsResult)); // In 1 tick
        scheduler.runTaskLater(plugin, () -> applyRestoration(bfsResult, packetEventsListener), 2L); // In 2 ticks
    }

    /**
     * Delete items dropped by fragile blocks, by checking if they are within the fragile blocks bounding box and if
     * they have been dropped in the last tick.
     *
     * @param bfsResult All block states (interactable and fragile) and their bounding box.
     */
    private void preventBlocksToDropItem(BfsResult bfsResult) {
        World world = bfsResult.getWorld();

        world.getNearbyEntities(bfsResult.boundingBox(), e -> e instanceof Item i && i.getTicksLived() <= 1)
                .forEach(Entity::remove);
    }

    private void applyRestoration(BfsResult bfsResult, Object packetEventsListener) {
        // Unregister fragile block break PacketEvents listener (if plugin is present)
        if (packetEventsListener != null) {
            PacketEventsAdapter.unregisterListener(packetEventsListener);
        }

        // Restore the fragile blocks if needed
        for (BlockState state : bfsResult.fragileBlocks()) {
            if (wasReplacedByAir(state)) {
                state.update(true, false); // Force restore without physic
            }
        }

        BlockState interactableBlockState = bfsResult.interactableBlock().blockState();
        boolean isInteractableAlsoFragile = bfsResult.interactableBlock().isAlsoFragile();

        // Restore the interactable block if needed
        if ((isInteractableAlsoFragile && wasReplacedByAir(interactableBlockState)) ||
            willTriggerAdditionalUpdate(interactableBlockState)) {
            interactableBlockState.update(true, false); // Force restore without physic
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
     * @return Whether the block will trigger an additional update.<br/> For example, a button will reset to its initial
     * state 1 second after being pressed.
     */
    boolean willTriggerAdditionalUpdate(BlockState state) {
        return Tag.BUTTONS.isTagged(state.getType());
    }
}
