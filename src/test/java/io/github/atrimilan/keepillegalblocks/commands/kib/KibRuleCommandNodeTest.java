package io.github.atrimilan.keepillegalblocks.commands.kib;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.atrimilan.keepillegalblocks.config.Config;
import io.github.atrimilan.keepillegalblocks.config.ConfigManager;
import io.github.atrimilan.keepillegalblocks.config.Rule;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KibRuleCommandNodeTest {

    @InjectMocks
    private KibRuleCommandNode kibRuleCommandNode;

    @Mock
    private CommandContext<CommandSourceStack> ctx;

    @Mock
    private CommandSourceStack source;

    @Mock
    private CommandSender sender;

    @Mock
    private ConfigManager configManager;

    @Mock
    private Config config;

    @BeforeEach
    void setUp() {
        lenient().when(ctx.getSource()).thenReturn(source);
        lenient().when(source.getSender()).thenReturn(sender);
        lenient().when(configManager.getConfig()).thenReturn(config);
    }

    @Test
    void shouldBuild() {
        LiteralArgumentBuilder<CommandSourceStack> node = kibRuleCommandNode.build();

        assertEquals("rule", node.getLiteral());
        assertNull(node.getCommand()); // Cannot be executed without specifying a rule

        // Check if all rules are registered as executable
        for (Rule rule : Rule.values()) {
            assertNotNull(node.build().getChild(rule.getName()));
            assertNotNull(node.build().getChild(rule.getName()).getCommand());
        }

        when(sender.hasPermission("kib.rule")).thenReturn(false);
        assertFalse(node.getRequirement().test(source));

        when(sender.hasPermission("kib.rule")).thenReturn(true);
        assertTrue(node.getRequirement().test(source));
    }

    @ParameterizedTest
    @EnumSource(Rule.class)
    void shouldGetRule(Rule rule) throws Exception {
        Object mockCurrentValue = new Object();
        when(config.getRule(rule)).thenReturn(mockCurrentValue); // Return non-null value

        var node = kibRuleCommandNode.build().build().getChild(rule.getName());
        int result = node.getCommand().run(ctx);

        assertEquals(Command.SINGLE_SUCCESS, result);
        verify(sender, times(1)).sendMessage(any(Component.class));
    }

    @Test
    void shouldSetIntegerRule() throws Exception {
        Rule rule = Rule.MAX_BLOCKS;
        int newValue = 42;

        when(ctx.getArgument(eq("value"), any())).thenReturn(newValue);

        var node = kibRuleCommandNode.build().build().getChild(rule.getName()).getChild("value");
        int result = node.getCommand().run(ctx);

        assertEquals(Command.SINGLE_SUCCESS, result);
        verify(configManager, times(1)).setRule(rule, newValue);
        verify(sender, times(1)).sendMessage(any(Component.class));
    }

    @Test
    void shouldSetBooleanRule() throws Exception {
        Rule rule = Rule.ONLY_USE_KIB_IN_CREATIVE_MODE;
        boolean newValue = true;

        when(ctx.getArgument(eq("value"), any())).thenReturn(newValue);

        var node = kibRuleCommandNode.build().build().getChild(rule.getName()).getChild("value");
        int result = node.getCommand().run(ctx);

        assertEquals(Command.SINGLE_SUCCESS, result);
        verify(configManager, times(1)).setRule(rule, newValue);
        verify(sender, times(1)).sendMessage(any(Component.class));
    }
}
