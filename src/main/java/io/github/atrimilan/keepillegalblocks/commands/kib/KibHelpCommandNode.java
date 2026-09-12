package io.github.atrimilan.keepillegalblocks.commands.kib;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class KibHelpCommandNode extends AbstractKibCommandNode {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Build {@code /kib help}
     *
     * @return A {@link LiteralCommandNode} of the command
     */
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("help") //
                .executes(this::sendHelpMessage);
    }

    private int sendHelpMessage(CommandContext<CommandSourceStack> ctx) {
        String helpMessage = """
                <shadow:black><yellow>
                
                <gold><st>                    </st> [</gold> <white>Keep Illegal Blocks</white> <gold>] <st>                    </st></gold>
                
                <click:open_url:'https://modrinth.com/plugin/keep-illegal-blocks'><green>>></green> <gray>See plugin on Modrinth</gray></click>
                <click:open_url:'https://github.com/Atrimilan/KeepIllegalBlocks'><black>>></black> <gray>See source code on GitHub</gray></click>
                
                <white>/kib reload</white>
                 Reloads the plugin configuration (<b>required</b> if you manually edit config.yml).
                
                <white>/kib rule <ruleName> [<value>]</white>
                 Displays or edits a rule value (automatically updating config.yml).
                 <gray><italic><hover:show_text:'<green>Click to use this example'>\
                <click:suggest_command:'/kib rule max_blocks 200'>\
                Example: /kib rule max_blocks 200\
                </click></hover></italic></gray>
                
                <white>/kib blacklist reactive <add|remove> <material></white>
                 Manages the reactive material blacklist. Blacklisted reactive materials are <b>not</b> restored when broken or updated.
                 <gray><italic><hover:show_text:'<green>Click to use this example'>\
                <click:suggest_command:'/kib blacklist reactive add minecraft:oak_sign'>\
                Example: /kib blacklist reactive add minecraft:oak_sign\
                </click></hover></italic></gray>
                
                <white>/kib blacklist interactable <add|remove> <material></white>
                 Manages the interactable blacklist. Blacklisted interactable materials <b>prevent</b> adjacent reactive blocks from being restored.
                 <gray><italic><hover:show_text:'<green>Click to use this example'>\
                <click:suggest_command:'/kib blacklist interactable add minecraft:oak_door'>\
                Example: /kib blacklist interactable add minecraft:oak_door\
                </click></hover></italic></gray>
                """;

        ctx.getSource().getSender().sendMessage(miniMessage.deserialize(helpMessage));

        return Command.SINGLE_SUCCESS;
    }
}
