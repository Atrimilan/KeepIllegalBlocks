package io.github.atrimilan.keepillegalblocks.commands.kib;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.atrimilan.keepillegalblocks.core.Settings;
import io.github.atrimilan.keepillegalblocks.core.types.KibRule;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class KibRuleCommandNode extends AbstractKibCommandNode {

    private final Settings settings;

    public KibRuleCommandNode(Settings settings) {
        this.settings = settings;
    }

    /**
     * Build {@code /kib rule <ruleName> [<value>]}
     *
     * @return A {@link LiteralCommandNode} of the command
     */
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> ruleNode = Commands.literal("rule") //
                .requires(ctx -> hasPermission(ctx, "kib.rule"));
        for (KibRule rule : KibRule.values()) {
            ruleNode.then(Commands.literal(rule.getName()) //
                                  .executes(ctx -> this.getRule(ctx, rule)) //
                                  .then(Commands.argument("value", rule.getArgumentType()) //
                                                .executes(ctx -> this.setRule(ctx, rule))));
        }
        return ruleNode;
    }

    private int setRule(CommandContext<CommandSourceStack> ctx, KibRule rule) {
        Object newValue = rule.getValue(ctx);

        settings.setRule(rule, newValue);
        ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                "<green>Rule <white>" + rule.getName() + "<green> has been set to: <white>" + newValue));

        return Command.SINGLE_SUCCESS;
    }

    private int getRule(CommandContext<CommandSourceStack> ctx, KibRule rule) {
        Object currentValue = settings.getRule(rule);
        if (currentValue == null) return 0;

        ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                "<yellow>Rule <white>" + rule.getName() + "<yellow> is currently set to: <white>" + currentValue));

        return Command.SINGLE_SUCCESS;
    }
}
