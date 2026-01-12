package io.github.atrimilan.keepillegalblocks;

import io.github.atrimilan.keepillegalblocks.commands.KibCommand;
import io.github.atrimilan.keepillegalblocks.eventlisteners.BlockInteractionListener;
import io.github.atrimilan.keepillegalblocks.services.BlockRestorerService;
import io.github.atrimilan.keepillegalblocks.services.KibService;
import io.github.atrimilan.keepillegalblocks.utils.blocks.FragileBlockUtils;
import io.github.atrimilan.keepillegalblocks.utils.blocks.InteractableBlockUtils;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public class KeepIllegalBlocks extends JavaPlugin {

    @Override
    public void onLoad() {
        initConfigFile();
        InteractableBlockUtils.init(this);
        FragileBlockUtils.init(this);
    }

    private void initConfigFile() {
        this.saveDefaultConfig(); // Save a full copy of the default config.yml file
        this.getConfig().options().copyDefaults(true); // For any missing value, copy them from the default config.yml
        this.saveConfig();
    }

    @Override
    public void onEnable() {
        registerPluginCommands();
        registerPluginEvents();
    }

    private void registerPluginCommands() {
        KibCommand kibCommand = new KibCommand(new KibService(this));

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(kibCommand.create(), KibCommand.DESCRIPTION, KibCommand.ALIASES);
        });
    }

    private void registerPluginEvents() {
        BlockRestorerService blockRestorerService = new BlockRestorerService(this);
        getServer().getPluginManager().registerEvents(new BlockInteractionListener(blockRestorerService), this);
    }
}
