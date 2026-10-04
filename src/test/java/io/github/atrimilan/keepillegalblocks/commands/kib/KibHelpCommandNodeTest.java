package io.github.atrimilan.keepillegalblocks.commands.kib;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KibHelpCommandNodeTest {

    @InjectMocks
    private KibHelpCommandNode kibHelpCommandNode;

    @Mock
    private CommandContext<CommandSourceStack> ctx;

    @Mock
    private CommandSourceStack source;

    @Mock
    private CommandSender sender;

    @BeforeEach
    void setUp() {
        lenient().when(ctx.getSource()).thenReturn(source);
        lenient().when(source.getSender()).thenReturn(sender);
    }

    @Test
    void shouldBuild() {
        LiteralArgumentBuilder<CommandSourceStack> node = kibHelpCommandNode.build();

        assertEquals("help", node.getLiteral());
        assertNotNull(node.getCommand());
    }

    @Test
    void shouldSendHelpMessage() throws Exception {
        LiteralArgumentBuilder<CommandSourceStack> node = kibHelpCommandNode.build();
        int result = node.build().getCommand().run(ctx);

        assertEquals(Command.SINGLE_SUCCESS, result);
        verify(sender, times(1)).sendMessage(any(Component.class));
    }
}
