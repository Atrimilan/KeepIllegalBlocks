package io.github.atrimilan.keepillegalblocks.restoration;

import io.github.atrimilan.keepillegalblocks.configuration.KibConfig;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class BlockInteractionListener implements Listener {

    private final BlockRestorationService service;
    private final KibConfig config;

    public BlockInteractionListener(BlockRestorationService service, KibConfig config) {
        this.service = service;
        this.config = config;
    }

    /**
     * Listen to players' interactions with interactable blocks, and restore any adjacent fragile blocks that break as a
     * result of the interaction.
     * <p>
     * The event will be ignored if:
     * <li>KIB is only enabled in Creative mode and player is not in Creative mode</li>
     * <li>The action is not a right click</li>
     * <li>The hand is not the right hand</li>
     * <li>The player is sneaking and holding an item</li>
     * <li>The block is not interactable</li>
     *
     * @param event The player's interaction event
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (config.isOnlyEnabledInCreativeMode() && !GameMode.CREATIVE.equals(event.getPlayer().getGameMode())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getPlayer().isSneaking() && event.getItem() != null) return;

        Block sourceBlock = event.getClickedBlock();
        if (sourceBlock == null) return;

        // Check if the block is interactable
        if (!config.isInteractable(sourceBlock.getType())) return;

        // Perform a BFS to scan and save all fragile blocks that will break as a result of the player interaction
        BfsResult result = service.recordFragileBlockStates(sourceBlock, config.getMaxBlocks());

        if (result.fragileBlocks().isEmpty()) return;

        // Schedule fragile block restoration
        service.scheduleRestoration(result);
    }
}
