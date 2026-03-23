package io.github.atrimilan.keepillegalblocks.core;

import io.github.atrimilan.keepillegalblocks.core.types.KibGroup;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Handle the settings of the plugin (based on config.yml).
 */
public class Settings {

    private final JavaPlugin plugin;
    private boolean packetEventsPresent;

    private int maxBlocks;
    private boolean onlyEnabledInCreativeMode;
    private boolean usePacketEventsIfDetected;
    private final Map<KibGroup, Set<String>> blacklists = new EnumMap<>(KibGroup.class);
    private final Map<KibGroup, Set<String>> enabledCategories = new EnumMap<>(KibGroup.class);

    public Settings(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize config.yml by saving the default config, then load the settings.
     */
    public void initConfig() {
        plugin.saveDefaultConfig(); // Save a full copy of the default config.yml file
        plugin.getConfig().options().copyDefaults(true); // For any missing value, copy them from the default config.yml
        plugin.saveConfig();

        packetEventsPresent = plugin.getServer().getPluginManager().isPluginEnabled("packetevents");

        plugin.getLogger().info(packetEventsPresent ?
                                "PacketEvents is present, it will be used if \"use-packet-events-if-detected\" is enabled." :
                                "PacketEvents is not present. It is recommended for better client-side rendering.");
        loadConfig();
    }

    /**
     * Reload config.yml and load the settings.
     */
    public void reloadConfig() {
        plugin.reloadConfig();

        blacklists.clear();
        enabledCategories.clear();

        loadConfig();
    }

    /**
     * Load settings from config.yml.
     */
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        maxBlocks = config.getInt("max-blocks");
        onlyEnabledInCreativeMode = config.getBoolean("only-use-kib-in-creative-mode");
        usePacketEventsIfDetected = config.getBoolean("use-packet-events-if-detected");

        for (KibGroup group : KibGroup.values()) {
            loadGroupSettings(group);
        }
    }

    /**
     * Load blacklist and enabled categories of the specified group, from config.yml.
     *
     * @param kibGroup The {@link KibGroup} to load blacklist and enabled categories for.
     */
    private void loadGroupSettings(KibGroup kibGroup) {
        FileConfiguration config = plugin.getConfig();

        // Group's blacklist
        blacklists.put(kibGroup, new HashSet<>(config.getStringList(kibGroup.getSectionKey() + "blacklist")));

        // Group's enabled categories
        Set<String> enabledSet = new HashSet<>();
        ConfigurationSection section = config.getConfigurationSection(kibGroup.getSectionKey() + "categories");
        if (section != null) {
            for (String sKey : section.getKeys(false)) {
                if (section.getBoolean(sKey, true)) {
                    enabledSet.add(sKey);
                }
            }
        }
        enabledCategories.put(kibGroup, enabledSet);
    }

    /**
     * @return The maximum number of blocks that can be recorded.
     */
    public int getMaxBlocks() {
        return maxBlocks;
    }

    /**
     * @return Whether the plugin should only be enabled in creative mode.
     */
    public boolean isOnlyEnabledInCreativeMode() {
        return onlyEnabledInCreativeMode;
    }

    /**
     * @return Whether PacketEvents should be used to improve rendering (if it's detected and enabled on the server).
     */
    public boolean isPacketEventsEnabled() {
        return packetEventsPresent && usePacketEventsIfDetected;
    }

    /**
     * @param key The {@link KibGroup}
     * @return A set of blacklisted material names for the specified group.
     */
    public Set<String> getBlacklistedMaterialsForGroup(KibGroup key) {
        return blacklists.getOrDefault(key, Collections.emptySet());
    }

    /**
     * @param key The {@link KibGroup}
     * @return A set of enabled categories for the specified group.
     */
    public Set<String> getEnabledCategoriesForGroup(KibGroup key) {
        return enabledCategories.getOrDefault(key, Collections.emptySet());
    }
}
