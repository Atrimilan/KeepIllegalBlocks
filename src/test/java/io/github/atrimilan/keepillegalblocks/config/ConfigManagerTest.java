package io.github.atrimilan.keepillegalblocks.config;

import com.tchristofferson.configupdater.ConfigUpdater;
import io.github.atrimilan.keepillegalblocks.models.MaterialGroup;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigManagerTest {

    @InjectMocks
    private ConfigManager configManager;

    @Mock
    private JavaPlugin plugin;

    @Mock
    private Server server;

    @Mock
    private PluginManager pluginManager;

    @Mock
    private Logger logger;

    @Mock
    private FileConfiguration fileConfig;

    @Mock
    private FileConfigurationOptions fileConfigOptions;

    private MockedStatic<ConfigUpdater> mockedConfigUpdater;

    @BeforeEach
    void setUp() {
        lenient().when(plugin.getLogger()).thenReturn(logger);
        lenient().when(plugin.getServer()).thenReturn(server);
        lenient().when(server.getPluginManager()).thenReturn(pluginManager);
        lenient().when(plugin.getConfig()).thenReturn(fileConfig);
        lenient().when(fileConfig.getStringList(anyString())).thenReturn(Collections.emptyList());
        mockedConfigUpdater = mockStatic(ConfigUpdater.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedConfigUpdater != null) mockedConfigUpdater.close();
    }

    @Test
    void shouldInitConfig() {
        // Given
        when(plugin.getDataFolder()).thenReturn(new File("KeepIllegalBlocks", "config.yml"));
        when(pluginManager.isPluginEnabled("packetevents")).thenReturn(true);

        when(fileConfig.getInt(Rule.MAX_BLOCKS.getName())).thenReturn(500);
        when(fileConfig.getBoolean(Rule.ONLY_USE_KIB_IN_CREATIVE_MODE.getName())).thenReturn(true);
        when(fileConfig.getBoolean(Rule.USE_PACKET_EVENTS_IF_DETECTED.getName())).thenReturn(true);

        // When
        configManager.initConfig();

        // Then
        verify(plugin).saveDefaultConfig();

        mockedConfigUpdater.verify(() -> ConfigUpdater.update(eq(plugin), eq("config.yml"), any(File.class)));
        verify(fileConfigOptions, never()).copyDefaults(anyBoolean());
        verify(plugin, never()).saveConfig();
        verify(logger, never()).severe(contains("ConfigUpdater"));
        verify(logger).info(contains("PacketEvents"));

        assertEquals(500, configManager.getConfig().maxBlocks());
        assertTrue(configManager.getConfig().onlyEnabledInCreativeMode());
        assertTrue(configManager.getConfig().isPacketEventsEnabled());
    }


    @Test
    void shouldInitConfigWithConfigUpdaterException() {
        // Given
        when(plugin.getDataFolder()).thenReturn(new File("KeepIllegalBlocks", "config.yml"));
        when(fileConfig.options()).thenReturn(fileConfigOptions); // Required in caught exception

        when(fileConfig.getInt(Rule.MAX_BLOCKS.getName())).thenReturn(500);
        when(fileConfig.getBoolean(Rule.ONLY_USE_KIB_IN_CREATIVE_MODE.getName())).thenReturn(true);
        when(fileConfig.getBoolean(Rule.USE_PACKET_EVENTS_IF_DETECTED.getName())).thenReturn(true);

        when(pluginManager.isPluginEnabled("packetevents")).thenReturn(true);

        mockedConfigUpdater.when(() -> ConfigUpdater.update(any(), anyString(), any(File.class)))
                .thenThrow(new IOException());

        // When
        configManager.initConfig();

        // Then
        verify(plugin).saveDefaultConfig();

        mockedConfigUpdater.verify(() -> ConfigUpdater.update(eq(plugin), eq("config.yml"), any(File.class)));
        verify(fileConfigOptions).copyDefaults(true);
        verify(plugin).saveConfig();
        verify(logger).severe(contains("Failed to update config.yml"));
        verify(logger).info(contains("PacketEvents"));

        assertEquals(500, configManager.getConfig().maxBlocks());
        assertTrue(configManager.getConfig().onlyEnabledInCreativeMode());
        assertTrue(configManager.getConfig().isPacketEventsEnabled());
    }

    @Test
    void shouldLoadEnabledCategories() { // On initConfig or reloadConfig
        // Given
        ConfigurationSection interactableSection = mock(ConfigurationSection.class);
        when(fileConfig.getConfigurationSection(MaterialGroup.INTERACTABLE.getCategoriesSectionKey())).thenReturn(
                interactableSection);

        when(interactableSection.getKeys(false)).thenReturn(Set.of("doors", "gates"));
        when(interactableSection.getBoolean("doors", true)).thenReturn(true);
        when(interactableSection.getBoolean("gates", true)).thenReturn(false);

        // When
        configManager.reloadConfig();

        // Then
        Set<String> enabledCategories = configManager.getConfig()
                .getEnabledCategoriesForGroup(MaterialGroup.INTERACTABLE);
        assertTrue(enabledCategories.contains("doors"));
        assertFalse(enabledCategories.contains("gates"));
    }

    @Test
    void shouldReloadConfig() {
        configManager.reloadConfig();

        verify(plugin).reloadConfig();

        verify(plugin, never()).saveDefaultConfig();
        verify(plugin, never()).saveConfig();
    }

    @ParameterizedTest
    @EnumSource(value = Rule.class)
    void shouldSetRule(Rule rule) {
        Object newValue = switch (rule) {
            case MAX_BLOCKS -> 42;
            case ONLY_USE_KIB_IN_CREATIVE_MODE -> true;
            case USE_PACKET_EVENTS_IF_DETECTED -> false;
        };

        switch (rule) {
            case MAX_BLOCKS -> lenient().when(fileConfig.getInt(rule.getName())).thenReturn((Integer) newValue);
            case ONLY_USE_KIB_IN_CREATIVE_MODE, USE_PACKET_EVENTS_IF_DETECTED ->
                    lenient().when(fileConfig.getBoolean(rule.getName())).thenReturn((Boolean) newValue);
        }

        configManager.setRule(rule, newValue);

        assertEquals(newValue, configManager.getConfig().getRule(rule));

        verify(fileConfig).set(rule.getName(), newValue);
        verify(plugin).saveConfig();
        verify(plugin).reloadConfig();
    }

    @ParameterizedTest
    @EnumSource(MaterialGroup.class)
    void shouldAddToBlacklist(MaterialGroup group) {
        String materialToAdd = Material.OAK_DOOR.name();

        configManager.initConfig(); // Initialize config first
        boolean added = configManager.addToBlacklist(group, materialToAdd);

        assertTrue(added);
        verify(fileConfig).set(eq(group.getBlacklistSectionKey()),
                               argThat((List<?> list) -> list.contains(materialToAdd)));
        verify(plugin).saveConfig();
        verify(plugin).reloadConfig();
    }

    @ParameterizedTest
    @EnumSource(MaterialGroup.class)
    void shouldNotAddToBlacklistWhenAlreadyPresent(MaterialGroup group) {
        String existingMaterial = Material.OAK_DOOR.name();
        when(fileConfig.getStringList(group.getBlacklistSectionKey())).thenReturn(List.of(existingMaterial));

        configManager.initConfig(); // Initialize config first
        boolean added = configManager.addToBlacklist(group, existingMaterial);

        assertFalse(added);
        verify(fileConfig, never()).set(eq(group.getBlacklistSectionKey()), any());
        verify(plugin, never()).saveConfig();
        verify(plugin, never()).reloadConfig();
    }
}
