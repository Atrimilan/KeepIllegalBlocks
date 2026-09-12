package io.github.atrimilan.keepillegalblocks.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.atrimilan.keepillegalblocks.commands.kib.KibBlacklistCommandNode;
import io.github.atrimilan.keepillegalblocks.commands.kib.KibHelpCommandNode;
import io.github.atrimilan.keepillegalblocks.commands.kib.KibReloadCommandNode;
import io.github.atrimilan.keepillegalblocks.commands.kib.KibRuleCommandNode;
import io.github.atrimilan.keepillegalblocks.core.RegistryLoader;
import io.github.atrimilan.keepillegalblocks.core.Settings;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.Set;
import java.util.logging.Logger;

public class KibCommand {

    public static final String DESCRIPTION = "Manage KIB configuration";
    public static final Set<String> ALIASES = Set.of("keepillegalblocks");

    private final KibReloadCommandNode reloadCommand;
    private final KibHelpCommandNode helpCommand;
    private final KibBlacklistCommandNode blacklistCommand;
    private final KibRuleCommandNode ruleCommand;

    public KibCommand(Settings settings, RegistryLoader registryLoader, Logger logger) {
        this.reloadCommand = new KibReloadCommandNode(settings, registryLoader, logger);
        this.helpCommand = new KibHelpCommandNode();
        this.blacklistCommand = new KibBlacklistCommandNode(settings);
        this.ruleCommand = new KibRuleCommandNode(settings);
    }

    /**
     * @return A {@link LiteralCommandNode} of the full {@code /kib} command
     */
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("kib") //
                .requires(ctx -> ctx.getSender().hasPermission("kib.*")) //
                .then(helpCommand.build()) //
                .then(reloadCommand.build()) //
                .then(ruleCommand.build()) //
                .then(blacklistCommand.build()) //
                .build();
    }
}
