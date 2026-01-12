package io.github.atrimilan.keepillegalblocks;

import io.github.atrimilan.keepillegalblocks.commands.FlySpeedCommand;
import io.github.atrimilan.keepillegalblocks.commands.ReadConfigCommand;
import io.github.atrimilan.keepillegalblocks.eventlisteners.BlockInteractionListener;
import io.github.atrimilan.keepillegalblocks.services.BlockRestorerService;
import io.github.atrimilan.keepillegalblocks.services.FlySpeedService;
import io.github.atrimilan.keepillegalblocks.services.ReadConfigService;
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
//        registerPluginCommands();
        registerPluginEvents();
    }

    private void registerPluginCommands() {
        FlySpeedCommand flySpeedCommand = new FlySpeedCommand(new FlySpeedService());
        ReadConfigCommand readConfigCommand = new ReadConfigCommand(new ReadConfigService(this.getConfig()));

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(flySpeedCommand.create(), FlySpeedCommand.DESCRIPTION, FlySpeedCommand.ALIASES);
            commands.register(readConfigCommand.create(), ReadConfigCommand.DESCRIPTION, ReadConfigCommand.ALIASES);
        });
    }

    private void registerPluginEvents() {
        BlockRestorerService blockRestorerService = new BlockRestorerService(this);
        getServer().getPluginManager().registerEvents(new BlockInteractionListener(blockRestorerService), this);
    }
}
