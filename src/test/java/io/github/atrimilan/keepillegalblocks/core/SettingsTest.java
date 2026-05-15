package io.github.atrimilan.keepillegalblocks.core;

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
import org.mockito.junit.jupiter.MockitoExtension;

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
        when(fileConfig.options()).thenReturn(fileConfigOptions);
        when(pluginManager.isPluginEnabled("packetevents")).thenReturn(true);
        when(server.getPluginManager()).thenReturn(pluginManager);

        when(fileConfig.getInt("max-blocks")).thenReturn(500);
        when(fileConfig.getBoolean("only-use-kib-in-creative-mode")).thenReturn(true);
        when(fileConfig.getBoolean("use-packet-events-if-detected")).thenReturn(true);

        // When
        settings.initConfig();

        // Then
        verify(plugin).saveDefaultConfig();
        verify(fileConfigOptions).copyDefaults(true);
        verify(plugin).saveConfig();
        verify(logger).info(anyString());

        assertEquals(500, settings.getMaxBlocks());
        assertTrue(settings.isOnlyEnabledInCreativeMode());
        assertTrue(settings.isPacketEventsEnabled());
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
        KibGroup kibGroup = KibGroup.FRAGILE;
        String sectionKey = kibGroup.getSectionKey();
        when(fileConfig.getStringList(sectionKey + "blacklist")).thenReturn(List.of("DIRT", "STONE"));
        when(fileConfig.getConfigurationSection(sectionKey + "categories")).thenReturn(configurationSection);

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

        KibGroup kibGroup = KibGroup.FRAGILE;
        Set<String> blacklist = settings.getBlacklistedMaterialsForGroup(kibGroup);
        Set<String> enabledCategories = settings.getEnabledCategoriesForGroup(kibGroup);

        assertEquals(0, blacklist.size());
        assertEquals(0, enabledCategories.size());
    }
}
