package io.github.atrimilan.keepillegalblocks.services;

import com.mojang.brigadier.Command;
import io.github.atrimilan.keepillegalblocks.enums.FragileType;
import io.github.atrimilan.keepillegalblocks.enums.InteractableType;
import io.github.atrimilan.keepillegalblocks.utils.blocks.FragileBlockUtils;
import io.github.atrimilan.keepillegalblocks.utils.blocks.InteractableBlockUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class KibService {

    private final JavaPlugin plugin;

    public KibService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public int reloadKib() {
        plugin.reloadConfig(); // Reload the config.yml file

        long oldInteractableSize = InteractableBlockUtils.INTERACTABLE_BLOCKS.values().stream() //
                .filter(type -> type != InteractableType.NONE).count();
        long oldFragileSize = FragileBlockUtils.FRAGILE_BLOCKS.values().stream() //
                .filter(type -> type != FragileType.NONE).count();

        int interactableBlacklist = InteractableBlockUtils.reload(plugin);
        int fragileBlacklist = FragileBlockUtils.reload(plugin);

        long newInteractableSize = InteractableBlockUtils.INTERACTABLE_BLOCKS.values().stream() //
                .filter(type -> type != InteractableType.NONE).count();
        long newFragileSize = FragileBlockUtils.FRAGILE_BLOCKS.values().stream() //
                .filter(type -> type != FragileType.NONE).count();

        if (oldInteractableSize != newInteractableSize) {
            plugin.getLogger().info(() -> "Interactable blocks reloaded: " + newInteractableSize + //
                                          (interactableBlacklist > 0 ?
                                           " (" + interactableBlacklist + " blacklisted in config.yml)" : ""));
        }
        if (oldFragileSize != newFragileSize) {
            plugin.getLogger().info(() -> "Fragile blocks reloaded: " + newFragileSize + //
                                          (fragileBlacklist > 0 ?
                                           " (" + fragileBlacklist + " blacklisted in config.yml)" : ""));
        }

        return Command.SINGLE_SUCCESS;
    }
}
