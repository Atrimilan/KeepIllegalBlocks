package io.github.atrimilan.lockblockstate.eventlisteners;

import io.github.atrimilan.lockblockstate.services.BlockInteractionService;
import io.github.atrimilan.lockblockstate.utils.BlockUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

/**
 * Listen to the player's interaction with a block adjacent to another that could break.
 */
public class BlockInteractionListener implements Listener {

//    private static final Set<Tag<Material>> PROTECTED_TAGS = Set.of(Tag.ALL_SIGNS, Tag.BUTTONS, MaterialTags.TORCHES,
//                                                                    Tag.RAILS, Tag.WOOL_CARPETS, Tag.CANDLES,
//                                                                    Tag.FLOWER_POTS);
//
//    private static final Set<Material> PROTECTED_MATERIALS = Set.of(Material.LEVER, Material.REDSTONE_WIRE,
//                                                                    Material.REPEATER, Material.COMPARATOR,
//                                                                    Material.LADDER, Material.TRIPWIRE_HOOK);

    private final JavaPlugin plugin;
    private final BlockInteractionService service;

    public BlockInteractionListener(JavaPlugin plugin, BlockInteractionService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Ignore left click, ignore left hand, and ignore interaction while sneaking and holding an item
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || //
                event.getHand() == EquipmentSlot.OFF_HAND || //
                (event.getPlayer().isSneaking() && event.getItem() != null)) {
            return;
        }

        Block sourceBlock = event.getClickedBlock();

        if (!BlockUtils.isInteractive(sourceBlock)) return;

        Map<Location, BlockState> blockStatesSnapshot = service.saveSourceAndRelativesBlockStates(sourceBlock);

        blockStatesSnapshot.forEach((l, b) -> System.out.println(b.getBlock().getType()));

        plugin.getServer().getScheduler()
                .runTaskLater(plugin, () -> service.restoreSourceAndRelativesBlockStates(blockStatesSnapshot), 1L);
    }
}
