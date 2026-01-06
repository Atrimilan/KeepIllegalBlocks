package io.github.atrimilan.paperplugintemplate.services;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadConfigServiceTest {

    @Mock
    FileConfiguration config;

    @Mock
    CommandSender sender;

    @InjectMocks
    ReadConfigService readConfigService;

    @Captor
    ArgumentCaptor<Component> componentCaptor;

    private final MiniMessage mm = MiniMessage.miniMessage();

    @Test
    void shouldReadSimpleValue() throws Exception {
        // Given
        String path = "example.boolean";
        String template = "Isn't this a Boolean: <gold><value>";
        when(config.get(path)).thenReturn("true");

        // When
        int result = readConfigService.readSimpleValue(sender, path, template);

        // Then
        verify(sender).sendMessage(componentCaptor.capture());
        assertEquals("Isn't this a Boolean: <gold>true", mm.serialize(componentCaptor.getValue()));
        assertEquals(Command.SINGLE_SUCCESS, result);
    }

    @Test
    void shouldReadPlayer() throws Exception {
        // Given
        ConfigurationSection playerSection = mock(ConfigurationSection.class);
        when(config.getConfigurationSection("example.player")).thenReturn(playerSection);
        when(playerSection.getString("uuid")).thenReturn("00000000-0000-0000-0000-000000000000");
        when(playerSection.contains("location.x")).thenReturn(true);
        when(playerSection.contains("location.y")).thenReturn(true);
        when(playerSection.contains("location.z")).thenReturn(true);
        when(playerSection.getDouble("location.x")).thenReturn(1d);
        when(playerSection.getDouble("location.y")).thenReturn(2d);
        when(playerSection.getDouble("location.z")).thenReturn(3d);

        // When
        int result = readConfigService.readPlayer(sender);

        // Then
        verify(sender).sendMessage(componentCaptor.capture());
        assertEquals( //
                "Player <aqua>00000000-0000-0000-0000-000000000000</aqua> is at <gold>1 2 3",
                mm.serialize(componentCaptor.getValue()));
        assertEquals(Command.SINGLE_SUCCESS, result);
    }

    @Test
    void shouldReadPlayerFailWhenPlayerSectionIsMissing() {
        // Given
        when(config.getConfigurationSection("example.player")).thenReturn(null);

        // When
        CommandSyntaxException ex = assertThrows(CommandSyntaxException.class, () -> readConfigService.readPlayer(sender));

        // Then
        assertEquals("Player not found in config", ex.getRawMessage().getString());
        verify(sender, never()).sendMessage(anyString());
    }

    @Test
    void shouldReadPlayerFailWhenPlayerUuidIsMissing() {
        // Given
        ConfigurationSection playerSection = mock(ConfigurationSection.class);
        when(config.getConfigurationSection("example.player")).thenReturn(playerSection);
        when(playerSection.getString("uuid")).thenReturn(null);

        // When
        CommandSyntaxException ex = assertThrows(CommandSyntaxException.class, () -> readConfigService.readPlayer(sender));

        // Then
        assertEquals("Player UUID is missing", ex.getRawMessage().getString());
        verify(sender, never()).sendMessage(anyString());
    }

    @Test
    void shouldReadPlayerFailWhenPlayerLocationIsMissing() {
        // Given
        ConfigurationSection playerSection = mock(ConfigurationSection.class);
        when(config.getConfigurationSection("example.player")).thenReturn(playerSection);
        when(playerSection.getString("uuid")).thenReturn("00000000-0000-0000-0000-000000000000");
        when(playerSection.contains("location.x")).thenReturn(true);
        when(playerSection.contains("location.y")).thenReturn(true);
        when(playerSection.contains("location.z")).thenReturn(false); // Missing property

        // When
        CommandSyntaxException ex = assertThrows(CommandSyntaxException.class, () -> readConfigService.readPlayer(sender));

        // Then
        assertEquals("Player location is missing", ex.getRawMessage().getString());
        verify(sender, never()).sendMessage(anyString());
    }
}
