package io.github.atrimilan.paperplugintemplate.eventlisteners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerActionsListenerTest {

    @Mock
    private Player player;

    @Mock
    private PlayerJoinEvent playerJoinEvent;

    @Captor
    ArgumentCaptor<Component> componentCaptor;

    @Test
    void onPlayerJoin_ShouldSetCustomJoinMessage() {
        // Given
        String playerName = "Atrimilan";
        when(playerJoinEvent.getPlayer()).thenReturn(player);
        when(player.getName()).thenReturn(playerName);

        PlayerActionsListener listener = new PlayerActionsListener();

        // When
        listener.onPlayerJoin(playerJoinEvent);

        // Then
        verify(playerJoinEvent).joinMessage(componentCaptor.capture());
        assertEquals(
                String.format("Welcome back <gold>%s</gold>!", playerName),
                MiniMessage.miniMessage().serialize(componentCaptor.getValue()));
    }
}
