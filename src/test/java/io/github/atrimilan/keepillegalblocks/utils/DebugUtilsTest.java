package io.github.atrimilan.keepillegalblocks.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Server;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebugUtilsTest {

    @Test
    void shouldSendChatWhenDebugIsEnabled() {
        DebugUtils.setDebugEnabled(true);

        Server mockServer = mock(Server.class);
        DebugUtils.setServer(mockServer);

        DebugUtils.sendChat(() -> "test", DebugUtils.MessageType.INFO);
        verify(mockServer).broadcast(MiniMessage.miniMessage().deserialize("<aqua>test"));
    }


    @Test
    void shouldNotSendChatWhenDebugIsDisabled()  {
        DebugUtils.setDebugEnabled(false);

        Server mockServer = mock(Server.class);
        DebugUtils.setServer(mockServer);

        DebugUtils.sendChat(() -> "test", DebugUtils.MessageType.INFO);
        verifyNoInteractions(mockServer);
    }


    @Test
    void shouldNotSendChatByDefault()  {
        Server mockServer = mock(Server.class);
        DebugUtils.setServer(mockServer);

        DebugUtils.sendChat(() -> "test", DebugUtils.MessageType.INFO);
        verifyNoInteractions(mockServer);
    }
}
