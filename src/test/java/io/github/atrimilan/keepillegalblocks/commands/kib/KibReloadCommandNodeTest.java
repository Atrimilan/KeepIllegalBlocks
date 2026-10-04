package io.github.atrimilan.keepillegalblocks.commands.kib;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.atrimilan.keepillegalblocks.config.Config;
import io.github.atrimilan.keepillegalblocks.config.ConfigManager;
import io.github.atrimilan.keepillegalblocks.core.RegistryLoader;
import io.github.atrimilan.keepillegalblocks.data.LoadResult;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KibReloadCommandNodeTest {

    @InjectMocks
    private KibReloadCommandNode kibReloadCommandNode;

    @Mock
    private CommandContext<CommandSourceStack> ctx;

    @Mock
    private CommandSourceStack source;

    @Mock
    private CommandSender sender;

    @Mock
    private RegistryLoader registryLoader;

    @Mock
    private ConfigManager configManager;

    @Mock
    private Config config;

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<Supplier<String>> captor;

    @BeforeEach
    void setUp() {
        lenient().when(ctx.getSource()).thenReturn(source);
        lenient().when(source.getSender()).thenReturn(sender);
        lenient().when(configManager.getConfig()).thenReturn(config);
    }

    @Test
    void shouldBuild() {
        LiteralArgumentBuilder<CommandSourceStack> node = kibReloadCommandNode.build();

        assertEquals("reload", node.getLiteral());
        assertNotNull(node.getCommand());

        when(sender.hasPermission("kib.reload")).thenReturn(false);
        assertFalse(node.getRequirement().test(source));

        when(sender.hasPermission("kib.reload")).thenReturn(true);
        assertTrue(node.getRequirement().test(source));
    }

    @Test
    void shouldReloadKibFromPlayer() throws Exception {
        LoadResult loadResult = mock(LoadResult.class);
        when(loadResult.consoleFormat()).thenReturn("Reload - Console message");
        when(loadResult.chatFormat()).thenReturn("Reload - Chat message");
        when(registryLoader.loadMaterialRegistry(config)).thenReturn(List.of(loadResult));

        when(source.getExecutor()).thenReturn(mock(Player.class));

        LiteralArgumentBuilder<CommandSourceStack> node = kibReloadCommandNode.build();
        int result = node.build().getCommand().run(ctx);

        assertEquals(Command.SINGLE_SUCCESS, result);
        verify(sender, times(1)).sendMessage(any(Component.class));

        verify(configManager).reloadConfig();
        verify(registryLoader).loadMaterialRegistry(config);
        verify(sender).sendMessage(Component.text("Reload - Chat message"));
        verify(logger).info(captor.capture());
        assertEquals("Reload - Console message", captor.getValue().get());
    }

    @Test
    void shouldReloadKibFromConsole() throws Exception {
        LoadResult loadResult = mock(LoadResult.class);
        when(loadResult.consoleFormat()).thenReturn("Reload - Console message");
        when(registryLoader.loadMaterialRegistry(config)).thenReturn(List.of(loadResult));

        when(source.getExecutor()).thenReturn(null); // Console is not an Entity

        LiteralArgumentBuilder<CommandSourceStack> node = kibReloadCommandNode.build();
        int result = node.build().getCommand().run(ctx);

        assertEquals(Command.SINGLE_SUCCESS, result);
        verify(sender, never()).sendMessage(any(Component.class));

        verify(configManager).reloadConfig();
        verify(registryLoader).loadMaterialRegistry(config);
        verifyNoInteractions(sender);
        verify(logger).info(captor.capture());
        assertEquals("Reload - Console message", captor.getValue().get());
    }

    @Test
    void shouldReloadKibWithNoResult() throws Exception { // From any source
        when(registryLoader.loadMaterialRegistry(config)).thenReturn(Collections.emptyList());

        LiteralArgumentBuilder<CommandSourceStack> node = kibReloadCommandNode.build();
        int result = node.build().getCommand().run(ctx);

        assertEquals(Command.SINGLE_SUCCESS, result);
        verify(sender, never()).sendMessage(any(Component.class));

        verify(configManager).reloadConfig();
        verify(registryLoader).loadMaterialRegistry(config);
        verifyNoInteractions(sender);
        verifyNoInteractions(logger);
    }
}
