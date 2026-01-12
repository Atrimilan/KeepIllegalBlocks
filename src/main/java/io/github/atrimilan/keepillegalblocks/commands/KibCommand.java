package io.github.atrimilan.keepillegalblocks.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.atrimilan.keepillegalblocks.services.KibService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.Set;

public class KibCommand {

    public static final String DESCRIPTION = "Reload KeepIllegalBlocks";
    public static final Set<String> ALIASES = Set.of("keepillegalblocks");

    private final KibService kibService;

    public KibCommand(KibService kibService) {
        this.kibService = kibService;
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
        return kibService.reloadKib();
    }
}
