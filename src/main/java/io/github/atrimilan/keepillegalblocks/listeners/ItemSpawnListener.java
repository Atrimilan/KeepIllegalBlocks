package io.github.atrimilan.keepillegalblocks.listeners;

import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemSpawnListener implements Listener {

    private final BfsResult result;

    public ItemSpawnListener(BfsResult result, JavaPlugin plugin) {
        this.result = result;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (result.boundingBox().contains(event.getLocation().toVector()) && event.getEntity().getThrower() == null) {
            event.setCancelled(true);
        }
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }
}
