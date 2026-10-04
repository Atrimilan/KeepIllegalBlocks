package io.github.atrimilan.keepillegalblocks.commands.kib;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.atrimilan.keepillegalblocks.config.ConfigManager;
import io.github.atrimilan.keepillegalblocks.core.RegistryLoader;
import io.github.atrimilan.keepillegalblocks.models.MaterialGroup;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.block.BlockState;

public class KibBlacklistCommandNode extends AbstractKibCommandNode {

    private final ConfigManager configManager;
    private final RegistryLoader registryLoader;

    private static final String MATERIAL_ARG = "material";

    public KibBlacklistCommandNode(ConfigManager configManager, RegistryLoader registryLoader) {
        this.configManager = configManager;
        this.registryLoader = registryLoader;
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
                .then(this.buildBlacklistSubNode(MaterialGroup.INTERACTABLE))
                .then(this.buildBlacklistSubNode(MaterialGroup.REACTIVE));
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildBlacklistSubNode(MaterialGroup materialGroup) {
        return Commands.literal(materialGroup.getName()) //
                .then(Commands.literal("add") //
                              .then(Commands.argument(MATERIAL_ARG, ArgumentTypes.blockState()) //
                                            .executes(ctx -> this.addToBlacklist(ctx, materialGroup))))
                .then(Commands.literal("remove") //
                              .then(Commands.argument(MATERIAL_ARG, ArgumentTypes.blockState()) //
                                            .executes(ctx -> this.removeFromBlacklist(ctx, materialGroup))));
    }

    private int addToBlacklist(CommandContext<CommandSourceStack> ctx, MaterialGroup group) {
        String material = ctx.getArgument(MATERIAL_ARG, BlockState.class).getBlockData().getMaterial().name();

        if (configManager.addToBlacklist(group, material)) {
            registryLoader.loadMaterialRegistry(configManager.getConfig()); // Reload material registry
            sendMessage(ctx, material + "<red> is now in the " + group.name().toLowerCase() + " blacklist");
        } else {
            sendMessage(ctx, material + "<yellow> is already in the " + group.name().toLowerCase() + " blacklist");
        }
        return Command.SINGLE_SUCCESS;
    }

    private int removeFromBlacklist(CommandContext<CommandSourceStack> ctx, MaterialGroup group) {
        String material = ctx.getArgument(MATERIAL_ARG, BlockState.class).getBlockData().getMaterial().name();

        if (configManager.removeFromBlacklist(group, material)) {
            registryLoader.loadMaterialRegistry(configManager.getConfig()); // Reload material registry
            sendMessage(ctx, material + "<green> is no longer in the " + group.name().toLowerCase() + " blacklist");
        } else {
            sendMessage(ctx, material + "<yellow> is not in the " + group.name().toLowerCase() + " blacklist");
        }
        return Command.SINGLE_SUCCESS;
    }
}
