package io.github.atrimilan.keepillegalblocks.core;

import com.tchristofferson.configupdater.ConfigUpdater;
import io.github.atrimilan.keepillegalblocks.core.types.KibGroup;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingsTest {

    @InjectMocks
    private Settings settings;

    @Mock
    private JavaPlugin plugin;

    @Mock
    private FileConfiguration fileConfig;

    @Mock
    private FileConfigurationOptions fileConfigOptions;

    @BeforeEach
    void setUp() {
        when(plugin.getConfig()).thenReturn(fileConfig);
    }

    @Test
    void shouldInitConfig() {
        // Given
        Logger logger = mock(Logger.class);
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);

        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(pluginManager.isPluginEnabled("packetevents")).thenReturn(true);
        when(plugin.getDataFolder()).thenReturn(new File("KeepIllegalBlocks", "config.yml"));

        when(fileConfig.getInt("max-blocks")).thenReturn(500);
        when(fileConfig.getBoolean("only-use-kib-in-creative-mode")).thenReturn(true);
        when(fileConfig.getBoolean("use-packet-events-if-detected")).thenReturn(true);

        try (MockedStatic<ConfigUpdater> mockedUpdater = mockStatic(ConfigUpdater.class)) {
            // When
            settings.initConfig();

            // Then
            verify(plugin).saveDefaultConfig();

            mockedUpdater.verify(() -> ConfigUpdater.update(eq(plugin), eq("config.yml"), any(File.class)));
            verify(fileConfigOptions, never()).copyDefaults(anyBoolean());
            verify(plugin, never()).saveConfig();
            verify(logger, never()).severe(contains("ConfigUpdater"));
            verify(logger).info(contains("PacketEvents"));

            assertEquals(500, settings.getMaxBlocks());
            assertTrue(settings.isOnlyEnabledInCreativeMode());
            assertTrue(settings.isPacketEventsEnabled());
        }
    }

    @Test
    void shouldInitConfigWithConfigUpdaterException() {
        // Given
        Logger logger = mock(Logger.class);
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);

        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(pluginManager.isPluginEnabled("packetevents")).thenReturn(true);
        when(plugin.getDataFolder()).thenReturn(new File("KeepIllegalBlocks", "config.yml"));
        when(fileConfig.options()).thenReturn(fileConfigOptions); // Required in caught exception

        when(fileConfig.getInt("max-blocks")).thenReturn(500);
        when(fileConfig.getBoolean("only-use-kib-in-creative-mode")).thenReturn(true);
        when(fileConfig.getBoolean("use-packet-events-if-detected")).thenReturn(true);

        try (MockedStatic<ConfigUpdater> mockedUpdater = mockStatic(ConfigUpdater.class)) {
            mockedUpdater.when(() -> ConfigUpdater.update(any(), anyString(), any(File.class)))
                    .thenThrow(new IOException());

            // When
            settings.initConfig();

            // Then
            verify(plugin).saveDefaultConfig();

            mockedUpdater.verify(() -> ConfigUpdater.update(eq(plugin), eq("config.yml"), any(File.class)));
            verify(fileConfigOptions).copyDefaults(true);
            verify(plugin).saveConfig();
            verify(logger).severe(contains("ConfigUpdater"));
            verify(logger).info(contains("PacketEvents"));

            assertEquals(500, settings.getMaxBlocks());
            assertTrue(settings.isOnlyEnabledInCreativeMode());
            assertTrue(settings.isPacketEventsEnabled());
        }
    }

    @Test
    void shouldReloadConfig() {
        when(fileConfig.getInt("max-blocks")).thenReturn(200);

        settings.reloadConfig();

        verify(plugin).reloadConfig();
        assertEquals(200, settings.getMaxBlocks());
        verify(plugin, never()).saveDefaultConfig();
        verify(plugin, never()).saveConfig();
    }

    @Test
    void shouldLoadGroupSettings() {
        // Given
        ConfigurationSection configurationSection = mock(ConfigurationSection.class);
        KibGroup kibGroup = mock(KibGroup.class);

        when(fileConfig.getStringList(anyString())).thenReturn(List.of("DIRT", "STONE"));
        when(fileConfig.getConfigurationSection(anyString())).thenReturn(configurationSection);

        when(configurationSection.getKeys(false)).thenReturn(Set.of("signs", "grass"));
        when(configurationSection.getBoolean("signs", true)).thenReturn(true);
        when(configurationSection.getBoolean("grass", true)).thenReturn(false);

        // When
        settings.reloadConfig(); // This calls the loadGroupSettings() method

        // Then
        Set<String> blacklist = settings.getBlacklistedMaterialsForGroup(kibGroup);
        Set<String> enabledCategories = settings.getEnabledCategoriesForGroup(kibGroup);

        assertEquals(2, blacklist.size());
        assertTrue(blacklist.contains("DIRT"));

        assertEquals(1, enabledCategories.size());
        assertTrue(enabledCategories.contains("signs"));
        assertFalse(enabledCategories.contains("grass"));
    }

    @Test
    void shouldLoadGroupSettingsWhenConfigurationIsEmpty() {
        settings.reloadConfig(); // This calls the loadGroupSettings() method

        KibGroup kibGroup = mock(KibGroup.class);
        Set<String> blacklist = settings.getBlacklistedMaterialsForGroup(kibGroup);
        Set<String> enabledCategories = settings.getEnabledCategoriesForGroup(kibGroup);

        assertEquals(0, blacklist.size());
        assertEquals(0, enabledCategories.size());
    }
}
