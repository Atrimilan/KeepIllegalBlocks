package io.github.atrimilan.keepillegalblocks.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.github.atrimilan.keepillegalblocks.configuration.KibConfig;
import io.github.atrimilan.keepillegalblocks.models.LoadResult;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KibCommandTest {

    @InjectMocks
    private KibCommand kibCommand;

    @Mock
    private KibConfig kibConfig;

    @Mock
    private CommandSourceStack commandSourceStack;

    @Mock
    private CommandContext<CommandSourceStack> ctx;

    @Mock
    private CommandSender sender;

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<Supplier<String>> captor;

    @Test
    void shouldReloadKibFromPlayer() throws Exception {
        LoadResult mockResult = mock(LoadResult.class);
        when(mockResult.chatFormat()).thenReturn("Reload message");
        when(kibConfig.reload()).thenReturn(List.of(mockResult));

        when(ctx.getSource()).thenReturn(commandSourceStack);
        when(commandSourceStack.getSender()).thenReturn(sender);
        when(commandSourceStack.getExecutor()).thenReturn(mock(Player.class));

        CommandNode<CommandSourceStack> node = kibCommand.create().getChild("reload");
        node.getCommand().run(ctx);

        verify(kibConfig).reload();
        verify(sender).sendMessage(Component.text("Reload message"));
        verifyNoInteractions(logger);
    }

    @Test
    void shouldReloadKibFromConsole() throws Exception {
        LoadResult mockResult = mock(LoadResult.class);
        when(mockResult.consoleFormat()).thenReturn("Reload message");
        when(kibConfig.reload()).thenReturn(List.of(mockResult));

        when(ctx.getSource()).thenReturn(commandSourceStack);
        when(commandSourceStack.getSender()).thenReturn(sender);
        when(commandSourceStack.getExecutor()).thenReturn(null); // Console is not an Entity

        CommandNode<CommandSourceStack> node = kibCommand.create().getChild("reload");
        node.getCommand().run(ctx);

        verify(kibConfig).reload();
        verifyNoInteractions(sender);
        verify(logger).info(captor.capture());
        assertEquals("Reload message", captor.getValue().get());
    }

    @Test
    void shouldReloadKibWithNoResult() throws Exception { // From any source
        when(kibConfig.reload()).thenReturn(Collections.emptyList());

        when(ctx.getSource()).thenReturn(commandSourceStack);
        when(commandSourceStack.getSender()).thenReturn(sender);

        CommandNode<CommandSourceStack> node = kibCommand.create().getChild("reload");
        node.getCommand().run(ctx);

        verify(kibConfig).reload();
        verifyNoInteractions(sender);
        verifyNoInteractions(logger);
    }
}
