package io.github.atrimilan.keepillegalblocks.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.atrimilan.keepillegalblocks.services.ReadConfigService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.Set;

/**
 * Example command to read values from config.yml
 */
public class ReadConfigCommand {

    public static final String DESCRIPTION = "Read values from config.yml";
    public static final Set<String> ALIASES = Set.of("rc");

    private final ReadConfigService readConfigService;

    public ReadConfigCommand(ReadConfigService readConfigService) {
        this.readConfigService = readConfigService;
    }

    /**
     * @return A LiteralCommandNode of the "/read-config" command, ready to be registered
     */
    public LiteralCommandNode<CommandSourceStack> create() {
        // Usage: /read-config <ultimate-answer|pangram|boolean|player>
        return Commands.literal("read-config")
                .requires(ctx -> ctx.getSender().hasPermission("ppt.read-config"))
                .then(Commands.literal("ultimate-answer").executes(this::readUltimateAnswer))
                .then(Commands.literal("pangram").executes(this::readPangram))
                .then(Commands.literal("boolean").executes(this::readBoolean))
                .then(Commands.literal("player").executes(this::readPlayer))
                .build();
    }

    private int readUltimateAnswer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String path = "example.ultimate-answer";
        String template = "The answer to the Ultimate Question of Life, the Universe, and Everything is: <dark_red><bold><value>";
        return readConfigService.readSimpleValue(ctx.getSource().getSender(), path, template);
    }

    private int readPangram(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String path = "example.pangram";
        String template = "Pangram: <gold><value>";
        return readConfigService.readSimpleValue(ctx.getSource().getSender(), path, template);
    }

    private int readBoolean(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String path = "example.boolean";
        String template = "Isn't this a Boolean: <gold><value>";
        return readConfigService.readSimpleValue(ctx.getSource().getSender(), path, template);
    }

    private int readPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return readConfigService.readPlayer(ctx.getSource().getSender());
    }
}
