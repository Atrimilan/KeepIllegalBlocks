package io.github.atrimilan.keepillegalblocks.core;

import com.tchristofferson.configupdater.ConfigUpdater;
import io.github.atrimilan.keepillegalblocks.core.types.MaterialGroup;
import io.github.atrimilan.keepillegalblocks.core.types.KibRule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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
    private final Map<MaterialGroup, Set<String>> blacklists = new EnumMap<>(MaterialGroup.class);
    private final Map<MaterialGroup, Set<String>> enabledCategories = new EnumMap<>(MaterialGroup.class);

    public Settings(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize config.yml by saving the default config, then load the settings.
     */
    public void initConfig() {
        plugin.saveDefaultConfig(); // Save a full copy of the default config.yml file

        try {
            ConfigUpdater.update(plugin, "config.yml", new File(plugin.getDataFolder(), "config.yml"));
        } catch (Exception ignored) {
            plugin.getLogger().severe("Failed to update config.yml with ConfigUpdater, copying defaults instead.");
            plugin.getConfig().options().copyDefaults(true); // Copy any missing value from the default config.yml
            plugin.saveConfig();
        }

        packetEventsPresent = plugin.getServer().getPluginManager().isPluginEnabled("packetevents");
        plugin.getLogger().info(packetEventsPresent ?
                                "PacketEvents is present, it will be used if \"use-packet-events-if-detected\" is enabled in the config." :
                                "PacketEvents is not present, it is recommended for a smoother client-side experience.");
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

        for (MaterialGroup group : MaterialGroup.values()) {
            loadGroupSettings(group);
        }
    }

    /**
     * Load blacklist and enabled categories of the specified group, from config.yml.
     *
     * @param materialGroup The {@link MaterialGroup} to load blacklist and enabled categories for.
     */
    private void loadGroupSettings(MaterialGroup materialGroup) {
        FileConfiguration config = plugin.getConfig();

        // Group's blacklist
        blacklists.put(materialGroup, new HashSet<>(config.getStringList(materialGroup.getBlacklistSectionKey())));

        // Group's enabled categories
        Set<String> enabledSet = new HashSet<>();
        ConfigurationSection section = config.getConfigurationSection(materialGroup.getCategoriesSectionKey());
        if (section != null) {
            for (String sKey : section.getKeys(false)) {
                if (section.getBoolean(sKey, true)) {
                    enabledSet.add(sKey);
                }
            }
        }
        enabledCategories.put(materialGroup, enabledSet);
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
     * @param key The {@link MaterialGroup}
     * @return A set of blacklisted material names for the specified group.
     */
    public Set<String> getBlacklistedMaterialsForGroup(MaterialGroup key) {
        return blacklists.getOrDefault(key, Collections.emptySet());
    }

    /**
     * @param key The {@link MaterialGroup}
     * @return A set of enabled categories for the specified group.
     */
    public Set<String> getEnabledCategoriesForGroup(MaterialGroup key) {
        return enabledCategories.getOrDefault(key, Collections.emptySet());
    }

    public Object getRule(KibRule rule) {
        return switch (rule) {
            case KibRule.MAX_BLOCKS -> maxBlocks;
            case KibRule.ONLY_USE_KIB_IN_CREATIVE_MODE -> onlyEnabledInCreativeMode;
            case KibRule.USE_PACKET_EVENTS_IF_DETECTED -> usePacketEventsIfDetected;
        };
    }

    public void setRule(KibRule rule, Object value) {
        this.setConfig(switch (rule) {
            case KibRule.MAX_BLOCKS -> "max-blocks";
            case KibRule.ONLY_USE_KIB_IN_CREATIVE_MODE -> "only-use-kib-in-creative-mode";
            case KibRule.USE_PACKET_EVENTS_IF_DETECTED -> "use-packet-events-if-detected";
        }, value);
    }

    private <T> void setConfig(String configKey, T configValue) {
        plugin.getConfig().set(configKey, configValue);
        plugin.saveConfig();
        this.reloadConfig();
    }

    /**
     * Add a material to the blacklist of the specified group.
     *
     * @param group    The {@link MaterialGroup} to add the material to.
     * @param material The material name to add to the blacklist.
     */
    public void addToBlacklist(MaterialGroup group, String material) {
        Set<String> blacklist = blacklists.getOrDefault(group, new HashSet<>());
        blacklist.add(material);
        blacklists.put(group, blacklist);

        plugin.getConfig().set(group.getBlacklistSectionKey(), new ArrayList<>(blacklist));
        plugin.saveConfig();
    }

    /**
     * Remove a material from the blacklist of the specified group.
     *
     * @param group    The {@link MaterialGroup} to remove the material from.
     * @param material The material name to remove from the blacklist.
     */
    public void removeFromBlacklist(MaterialGroup group, String material) {
        Set<String> blacklist = blacklists.getOrDefault(group, new HashSet<>());
        blacklist.remove(material);
        blacklists.put(group, blacklist);

        plugin.getConfig().set(group.getBlacklistSectionKey(), new ArrayList<>(blacklist));
        plugin.saveConfig();
    }
}
