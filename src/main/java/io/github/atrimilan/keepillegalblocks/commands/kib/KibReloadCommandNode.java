package io.github.atrimilan.keepillegalblocks.commands.kib;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.atrimilan.keepillegalblocks.config.ConfigManager;
import io.github.atrimilan.keepillegalblocks.core.RegistryLoader;
import io.github.atrimilan.keepillegalblocks.data.LoadResult;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Logger;

public class KibReloadCommandNode extends AbstractKibCommandNode {

    private final ConfigManager configManager;
    private final RegistryLoader registryLoader;
    private final Logger logger;

    public KibReloadCommandNode(ConfigManager configManager, RegistryLoader registryLoader, Logger logger) {
        this.configManager = configManager;
        this.registryLoader = registryLoader;
        this.logger = logger;
    }

    /**
     * Build {@code /kib reload}
     *
     * @return A {@link LiteralCommandNode} of the command
     */
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("reload") //
                .requires(ctx -> this.hasPermission(ctx, "kib.reload")) //
                .executes(this::reloadKib);
    }

    private int reloadKib(CommandContext<CommandSourceStack> ctx) {
        configManager.reloadConfig();
        List<LoadResult> results = registryLoader.loadMaterialRegistry(configManager.getConfig());

        for (LoadResult result : results) {
            if (ctx.getSource().getExecutor() instanceof Player) {
                ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(result.chatFormat()));
            }
            logger.info(result::consoleFormat);
        }
        return Command.SINGLE_SUCCESS;
    }
}
