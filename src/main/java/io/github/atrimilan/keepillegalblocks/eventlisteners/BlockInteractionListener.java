package io.github.atrimilan.keepillegalblocks.eventlisteners;

import io.github.atrimilan.keepillegalblocks.services.BlockDependencyService;
import io.github.atrimilan.keepillegalblocks.utils.BlockUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

/**
 * Listen to the player's interaction with a block adjacent to another that could break.
 */
public class BlockInteractionListener implements Listener {

    private final BlockDependencyService service;

    public BlockInteractionListener(BlockDependencyService service) {
        this.service = service;
    }

    /**
     * Listen to players' interactions with interactable blocks, and restore any adjacent fragile blocks that break as a
     * result of the interaction.
     *
     * <p>The event will be ignored if:
     * <li>The action is not a right click
     * <li>The hand is not the right hand
     * <li>The player is sneaking and holding an item
     * <li>The block is not interactable
     *
     * @param event The player's interaction event
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getPlayer().isSneaking() && event.getItem() != null) return;

        Block sourceBlock = event.getClickedBlock();
        if (sourceBlock == null) return;

        // Check if the block is interactable
        if (!BlockUtils.isInteractable(sourceBlock)) return;

        // Perform a BFS to scan and save all fragile blocks that will break as a result of the player interaction
        List<BlockState> snapshot = service.recordFragileBlockStates(sourceBlock);

        // Schedule fragile block restoration
        service.scheduleRestoration(snapshot);
    }
}
