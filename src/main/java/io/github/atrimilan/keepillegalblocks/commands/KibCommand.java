package io.github.atrimilan.keepillegalblocks.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.atrimilan.keepillegalblocks.configuration.KibConfig;
import io.github.atrimilan.keepillegalblocks.models.LoadResult;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class KibCommand {

    public static final String DESCRIPTION = "Reload KeepIllegalBlocks";
    public static final Set<String> ALIASES = Set.of("keepillegalblocks");

    private final Logger logger;
    private final KibConfig kibConfig;

    public KibCommand(KibConfig kibConfig, Logger logger) {
        this.kibConfig = kibConfig;
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
        List<LoadResult> results = kibConfig.reload();
        CommandSender sender = ctx.getSource().getSender();

        for (LoadResult result : results) {
            if (ctx.getSource().getExecutor() instanceof Player)
                sender.sendMessage(MiniMessage.miniMessage().deserialize(result.chatFormat()));

            else logger.info(result::consoleFormat);
        }
        return Command.SINGLE_SUCCESS;
    }
}
