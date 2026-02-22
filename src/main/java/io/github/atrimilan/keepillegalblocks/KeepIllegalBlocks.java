package io.github.atrimilan.keepillegalblocks;

import io.github.atrimilan.keepillegalblocks.commands.KibCommand;
import io.github.atrimilan.keepillegalblocks.configuration.KibConfig;
import io.github.atrimilan.keepillegalblocks.listeners.BlockInteractionListener;
import io.github.atrimilan.keepillegalblocks.services.BlockRestorationService;
import io.github.atrimilan.keepillegalblocks.utils.DebugUtils;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class KeepIllegalBlocks extends JavaPlugin {

    private final KibConfig kibConfig = new KibConfig(this);

    @Override
    public void onEnable() {
        kibConfig.init();
        DebugUtils.setServer(getServer());

        new Metrics(this, 28933); // bStats

        registerPluginEvents();
        registerPluginCommands();
    }

    private void registerPluginEvents() {
        getServer().getPluginManager().registerEvents( //
                new BlockInteractionListener(new BlockRestorationService(kibConfig), kibConfig), this);
    }

    private void registerPluginCommands() {
        KibCommand kibCommand = new KibCommand(kibConfig, this.getLogger());

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(kibCommand.create(), KibCommand.DESCRIPTION, KibCommand.ALIASES);
        });
    }
}
