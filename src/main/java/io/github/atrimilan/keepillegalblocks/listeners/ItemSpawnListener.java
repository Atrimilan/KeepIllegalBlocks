package io.github.atrimilan.keepillegalblocks.listeners;

import io.github.atrimilan.keepillegalblocks.core.MaterialRegistry;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemSpawnListener implements Listener {

    private final BfsResult result;
    private final MaterialRegistry materialRegistry;

    public ItemSpawnListener(JavaPlugin plugin, BfsResult result, MaterialRegistry materialRegistry) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.result = result;
        this.materialRegistry = materialRegistry;
    }

    /**
     * Listen to {@link ItemSpawnEvent} and cancel them if they are dropped from broken fragile blocks that will be
     * restored. <b>This is important to prevent item duplication.<b/>
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (result.boundingBox().contains(event.getLocation().toVector()) && event.getEntity().getThrower() == null &&
            materialRegistry.isFragile(event.getEntity().getItemStack().getType())) {
            event.setCancelled(true);
        }
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }
}
