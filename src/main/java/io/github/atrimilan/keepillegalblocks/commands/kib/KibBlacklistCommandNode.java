package io.github.atrimilan.keepillegalblocks.commands.kib;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.atrimilan.keepillegalblocks.core.Settings;
import io.github.atrimilan.keepillegalblocks.core.types.KibGroup;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.BlockState;

public class KibBlacklistCommandNode extends AbstractKibCommandNode {

    private final Settings settings;

    private static final String MATERIAL_ARG = "material";

    public KibBlacklistCommandNode(Settings settings) {
        this.settings = settings;
    }

    /**
     * Build {@code /kib blacklist <interactable|reactive> <add|remove> <material>}
     *
     * @return A {@link LiteralCommandNode} of the command
     */
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("blacklist") //
                .requires(ctx -> hasPermission(ctx, "kib.blacklist"))
                .then(this.buildBlacklistSubNode(KibGroup.INTERACTABLE))
                .then(this.buildBlacklistSubNode(KibGroup.REACTIVE));
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildBlacklistSubNode(KibGroup kibGroup) {
        return Commands.literal(kibGroup.getGroupName()) //
                .then(Commands.literal("add") //
                              .then(Commands.argument(MATERIAL_ARG, ArgumentTypes.blockState()) //
                                            .executes(ctx -> this.addToBlacklist(ctx, kibGroup))))
                .then(Commands.literal("remove") //
                              .then(Commands.argument(MATERIAL_ARG, ArgumentTypes.blockState()) //
                                            .executes(ctx -> this.removeFromBlacklist(ctx, kibGroup))));
    }

    private int addToBlacklist(CommandContext<CommandSourceStack> ctx, KibGroup group) {
        String material = ctx.getArgument(MATERIAL_ARG, BlockState.class).getBlockData().getMaterial().name();

        settings.addToBlacklist(group, material);
        ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                "<white>" + material + "<red> is now in the " + group.name().toLowerCase() + " blacklist"));

        return Command.SINGLE_SUCCESS;
    }

    private int removeFromBlacklist(CommandContext<CommandSourceStack> ctx, KibGroup group) {
        String material = ctx.getArgument(MATERIAL_ARG, BlockState.class).getBlockData().getMaterial().name();

        settings.removeFromBlacklist(group, material);
        ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                "<white>" + material + "<green> is no longer in the " + group.name().toLowerCase() + " blacklist"));

        return Command.SINGLE_SUCCESS;
    }
}
