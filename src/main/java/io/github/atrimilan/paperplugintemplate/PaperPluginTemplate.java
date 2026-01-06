package io.github.atrimilan.paperplugintemplate;

import io.github.atrimilan.paperplugintemplate.commands.FlySpeedCommand;
import io.github.atrimilan.paperplugintemplate.commands.ReadConfigCommand;
import io.github.atrimilan.paperplugintemplate.eventlisteners.PlayerActionsListener;
import io.github.atrimilan.paperplugintemplate.services.FlySpeedService;
import io.github.atrimilan.paperplugintemplate.services.ReadConfigService;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperPluginTemplate extends JavaPlugin {

    @Override
    public void onEnable() {
        initConfigFile();
        registerPluginCommands();
        registerPluginEvents();
    }

    private void initConfigFile() {
        this.saveDefaultConfig(); // Save a full copy of the default config.yml file
        this.getConfig().options().copyDefaults(true); // For any missing value, copy them from the default config.yml
        this.saveConfig();
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
        getServer().getPluginManager().registerEvents(new PlayerActionsListener(), this);
    }
}
