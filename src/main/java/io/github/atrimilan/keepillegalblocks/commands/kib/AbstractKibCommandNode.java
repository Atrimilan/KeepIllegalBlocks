package io.github.atrimilan.keepillegalblocks.commands.kib;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;

public abstract class AbstractKibCommandNode {

    /**
     * Build {@code /kib <command_node>}
     *
     * @return A {@link LiteralCommandNode} of the command
     */
    public abstract LiteralArgumentBuilder<CommandSourceStack> build();

    /**
     * Checks if the sender has the required permission
     *
     * @param ctx        Command context
     * @param permission Expected permission
     * @return Whether the sender has the expected permission
     */
    protected boolean hasPermission(CommandSourceStack ctx, String permission) {
        return ctx.getSender().hasPermission(permission);
    }

    /**
     * Sends a message to the sender, formatted with {@link MiniMessage}
     *
     * @param ctx     Command context
     * @param message Message to send
     */
    protected void sendMessage(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(message));
    }
}
