package io.github.atrimilan.lockblockstate.services;

import com.mojang.brigadier.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlySpeedServiceTest {

    @Mock
    CommandSourceStack source;

    @InjectMocks
    FlySpeedService flySpeedService;

    @Captor
    ArgumentCaptor<Component> componentCaptor;

    private final MiniMessage mm = MiniMessage.miniMessage();

    @ParameterizedTest
    @ValueSource(floats = {0f, 0.3f, 1f, 5.777f, 10f})
    void shouldSetSelfFlySpeed(float speed) {
        // Given
        Player target = mock(Player.class);
        when(source.getExecutor()).thenReturn(target);

        // When
        int result = flySpeedService.setFlySpeed(source, target, speed);

        // Then
        verify(target).setFlySpeed(speed * 0.1f);
        verify(target).sendMessage(componentCaptor.capture());
        assertEquals(
                String.format("<green>Successfully set your flight speed to <gold>%s", speed),
                mm.serialize(componentCaptor.getValue()));
        verifyNoMoreInteractions(target);
        assertEquals(Command.SINGLE_SUCCESS, result);
    }

    @Test
    void shouldSetOtherFlySpeedFromPlayer() {
        Player sender = mock(Player.class);
        when(source.getExecutor()).thenReturn(sender);
        when(source.getSender()).thenReturn(sender);
        when(sender.getName()).thenReturn("Notch");

        this.shouldSetOtherFlySpeed(sender);
    }

    @Test
    void shouldSetOtherFlySpeedFromConsole() {
        ConsoleCommandSender sender = mock(ConsoleCommandSender.class);
        when(source.getExecutor()).thenReturn(null);
        when(source.getSender()).thenReturn(sender);
        when(sender.getName()).thenReturn("CONSOLE");

        this.shouldSetOtherFlySpeed(sender);
    }

    private void shouldSetOtherFlySpeed(CommandSender sender) {
        // Given
        float speed = 2f;

        Player target = mock(Player.class);
        when(target.name()).thenReturn(Component.text("Atrimilan"));
        // Sender is already mocked in calling methods (either player or console)

        // When
        int result = flySpeedService.setFlySpeed(source, target, speed);

        // Then
        verify(target).setFlySpeed(speed * 0.1f);
        verify(target).sendMessage(componentCaptor.capture());
        assertEquals(
                String.format("<aqua>%s</aqua> <gray>has set your flight speed to <gold>%s", sender.getName(), speed),
                mm.serialize(componentCaptor.getValue()));
        verify(sender).sendMessage(componentCaptor.capture());
        assertEquals(
                String.format("<gray>Successfully set <aqua>%s</aqua>'s flight speed to <gold>%s",
                        mm.serialize(target.name()), speed),
                mm.serialize(componentCaptor.getValue()));
        verifyNoMoreInteractions(target);
        verifyNoMoreInteractions(sender);
        assertEquals(Command.SINGLE_SUCCESS, result);
    }
}
