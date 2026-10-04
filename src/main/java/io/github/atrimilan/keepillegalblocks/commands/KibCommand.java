package io.github.atrimilan.keepillegalblocks.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.atrimilan.keepillegalblocks.commands.kib.KibBlacklistCommandNode;
import io.github.atrimilan.keepillegalblocks.commands.kib.KibHelpCommandNode;
import io.github.atrimilan.keepillegalblocks.commands.kib.KibReloadCommandNode;
import io.github.atrimilan.keepillegalblocks.commands.kib.KibRuleCommandNode;
import io.github.atrimilan.keepillegalblocks.config.ConfigManager;
import io.github.atrimilan.keepillegalblocks.core.RegistryLoader;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.Set;
import java.util.logging.Logger;

public class KibCommand {

    public static final String DESCRIPTION = "Manage KIB configuration";
    public static final Set<String> ALIASES = Set.of("keepillegalblocks");

    private final KibBlacklistCommandNode blacklistCommand;
    private final KibHelpCommandNode helpCommand;
    private final KibReloadCommandNode reloadCommand;
    private final KibRuleCommandNode ruleCommand;

    public KibCommand(ConfigManager configManager, RegistryLoader registryLoader, Logger logger) {
        this.blacklistCommand = new KibBlacklistCommandNode(configManager, registryLoader);
        this.helpCommand = new KibHelpCommandNode();
        this.reloadCommand = new KibReloadCommandNode(configManager, registryLoader, logger);
        this.ruleCommand = new KibRuleCommandNode(configManager);
    }

    /**
     * @return A {@link LiteralCommandNode} of the full {@code /kib} command
     */
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("kib") //
                .requires(ctx -> ctx.getSender().hasPermission("kib"))
                .then(blacklistCommand.build()) //
                .then(helpCommand.build()) //
                .then(reloadCommand.build()) //
                .then(ruleCommand.build()) //
                .build();
    }
}
