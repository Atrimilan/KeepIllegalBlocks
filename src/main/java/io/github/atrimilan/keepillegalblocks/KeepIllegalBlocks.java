package io.github.atrimilan.keepillegalblocks;

import io.github.atrimilan.keepillegalblocks.commands.KibCommand;
import io.github.atrimilan.keepillegalblocks.config.ConfigManager;
import io.github.atrimilan.keepillegalblocks.core.MaterialRegistry;
import io.github.atrimilan.keepillegalblocks.core.RegistryLoader;
import io.github.atrimilan.keepillegalblocks.events.bukkit.BlockInteractionListener;
import io.github.atrimilan.keepillegalblocks.data.LoadResult;
import io.github.atrimilan.keepillegalblocks.services.BlockRestorationService;
import io.github.atrimilan.keepillegalblocks.utils.DebugUtils;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class KeepIllegalBlocks extends JavaPlugin {

    private final ConfigManager configManager = new ConfigManager(this);
    private final MaterialRegistry materialRegistry = new MaterialRegistry();
    private final RegistryLoader registryLoader = new RegistryLoader(materialRegistry);

    @Override
    public void onEnable() {
        configManager.initConfig();
        List<LoadResult> results = registryLoader.loadMaterialRegistry(configManager.getConfig());

        for (LoadResult result : results) {
            getLogger().info(result::consoleFormat);
        }

        DebugUtils.setServer(getServer());

        new Metrics(this, 28933); // bStats

        registerPluginEvents();
        registerPluginCommands();
    }

    private void registerPluginEvents() {
        var blockRestorationService = new BlockRestorationService(this, materialRegistry, configManager);
        var blockInteractionListener = new BlockInteractionListener(blockRestorationService, materialRegistry, configManager);
        getServer().getPluginManager().registerEvents(blockInteractionListener, this);
    }

    private void registerPluginCommands() {
        KibCommand kibCommand = new KibCommand(configManager, registryLoader, getLogger());

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(kibCommand.build(), KibCommand.DESCRIPTION, KibCommand.ALIASES);
        });
    }
}
