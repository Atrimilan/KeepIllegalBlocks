package io.github.atrimilan.keepillegalblocks.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import io.github.atrimilan.keepillegalblocks.services.FlySpeedService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlySpeedCommandTest {

    @Mock
    FlySpeedService flySpeedService;

    @Mock
    CommandSourceStack commandSourceStack;

    @Mock
    CommandContext<CommandSourceStack> ctx;

    @InjectMocks
    FlySpeedCommand flySpeedCommand;

    @Test
    void shouldSetSelfFlySpeed() throws Exception {
        // Given
        Player player = mock(Player.class);
        CommandNode<CommandSourceStack> node = flySpeedCommand.create().getChild("speed");

        when(ctx.getSource()).thenReturn(commandSourceStack);
        when(commandSourceStack.getExecutor()).thenReturn(player);
        when(ctx.getArgument("speed", Float.class)).thenReturn(1f); // Mock FloatArgumentType.getFloat()

        // When
        node.getCommand().run(ctx);

        // Then
        verify(flySpeedService).setFlySpeed(commandSourceStack, player, 1f);
    }

    @Test
    void shouldSetSelfFlySpeedFailWhenExecutorIsNotPlayer() {
        // Given
        CommandNode<CommandSourceStack> node = flySpeedCommand.create().getChild("speed");

        when(ctx.getSource()).thenReturn(commandSourceStack);
        when(commandSourceStack.getExecutor()).thenReturn(null); // Anything but a player

        // When
        CommandSyntaxException ex = assertThrows(CommandSyntaxException.class, () -> node.getCommand().run(ctx));

        // Then
        assertEquals("Only players can fly!", ex.getRawMessage().getString());
        verifyNoInteractions(flySpeedService);
    }

    @Test
    void shouldSetTargetFlySpeed() throws Exception {
        // Given
        Player target = mock(Player.class);
        CommandNode<CommandSourceStack> node = flySpeedCommand.create().getChild("player").getChild("speed");

        var playerSelector = mock(PlayerSelectorArgumentResolver.class);
        when(ctx.getArgument("player", PlayerSelectorArgumentResolver.class)).thenReturn(playerSelector);
        when(playerSelector.resolve(commandSourceStack)).thenReturn(List.of(target));
        when(ctx.getSource()).thenReturn(commandSourceStack);
        when(ctx.getArgument("speed", Float.class)).thenReturn(1f); // Mock FloatArgumentType.getFloat()

        // When
        node.getCommand().run(ctx);

        // Then
        verify(flySpeedService).setFlySpeed(commandSourceStack, target, 1f);
    }
}
