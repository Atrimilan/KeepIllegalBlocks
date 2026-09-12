package io.github.atrimilan.keepillegalblocks.commands.kib;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public abstract class AbstractKibCommandNode {

    /**
     * Build {@code /kib <command_node>}
     *
     * @return A {@link LiteralCommandNode} of the command
     */
    public abstract LiteralArgumentBuilder<CommandSourceStack> build();

    /**
     * Checks if the sender has the required permission.
     *
     * @param ctx        Command context
     * @param permission Expected permission
     * @return Whether the sender has the expected permission (or {@code kib.admin})
     */
    protected boolean hasPermission(CommandSourceStack ctx, String permission) {
        return ctx.getSender().hasPermission("kib.admin") || ctx.getSender().hasPermission(permission);
    }
}
