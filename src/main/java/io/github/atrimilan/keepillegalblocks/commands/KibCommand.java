package io.github.atrimilan.keepillegalblocks.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.atrimilan.keepillegalblocks.core.RegistryLoader;
import io.github.atrimilan.keepillegalblocks.core.Settings;
import io.github.atrimilan.keepillegalblocks.models.LoadResult;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class KibCommand {

    public static final String DESCRIPTION = "Reload KeepIllegalBlocks";
    public static final Set<String> ALIASES = Set.of("keepillegalblocks");

    private final Settings settings;
    private final RegistryLoader registryLoader;
    private final Logger logger;

    public KibCommand(Settings settings, RegistryLoader registryLoader, Logger logger) {
        this.settings = settings;
        this.registryLoader = registryLoader;
        this.logger = logger;
    }

    /**
     * @return A LiteralCommandNode of the "/kib" command
     */
    public LiteralCommandNode<CommandSourceStack> create() {
        // Usage: /kib reload
        return Commands.literal("kib") //
                .requires(ctx -> ctx.getSender().hasPermission("kib.reload"))//
                .then(Commands.literal("reload").executes(this::reloadKib)) //
                .build();
    }

    private int reloadKib(CommandContext<CommandSourceStack> ctx) {
        settings.reloadConfig();
        List<LoadResult> results = registryLoader.fillMaterialRegistry(settings);

        for (LoadResult result : results) {
            if (ctx.getSource().getExecutor() instanceof Player)
                ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(result.chatFormat()));

            logger.info(result::consoleFormat);
        }
        return Command.SINGLE_SUCCESS;
    }
}
