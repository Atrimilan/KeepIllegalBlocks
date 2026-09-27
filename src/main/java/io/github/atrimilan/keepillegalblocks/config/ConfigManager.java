package io.github.atrimilan.keepillegalblocks.config;

import com.tchristofferson.configupdater.ConfigUpdater;
import io.github.atrimilan.keepillegalblocks.models.MaterialGroup;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

/**
 * Manage Bukkit's config.yml file
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private Config config;

    public ConfigManager(JavaPlugin plugin) {
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
            plugin.getLogger().severe("Failed to update config.yml, copying defaults instead.");
            plugin.getConfig().options().copyDefaults(true);
            plugin.saveConfig();
        }

        boolean packetEventsPresent = plugin.getServer().getPluginManager().isPluginEnabled("packetevents");
        plugin.getLogger().info(packetEventsPresent ?
                                "PacketEvents is present, it will be used if \"use-packet-events-if-detected\" is enabled in the config." :
                                "PacketEvents is not present, it is recommended for a smoother client-side experience.");
        loadConfig();
    }

    /**
     * @return The current config record
     */
    public Config getConfig() {
        return config;
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration configFile = plugin.getConfig();

        Map<MaterialGroup, Set<String>> blacklists = new EnumMap<>(MaterialGroup.class);
        Map<MaterialGroup, Set<String>> enabledCategories = new EnumMap<>(MaterialGroup.class);

        for (MaterialGroup group : MaterialGroup.values()) {
            blacklists.put(group, new HashSet<>(configFile.getStringList(group.getBlacklistSectionKey())));

            Set<String> enabledSet = new HashSet<>();
            ConfigurationSection section = configFile.getConfigurationSection(group.getCategoriesSectionKey());
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    if (section.getBoolean(key, true)) enabledSet.add(key);
                }
            }
            enabledCategories.put(group, enabledSet);
        }

        config = new Config(plugin.getServer().getPluginManager().isPluginEnabled("packetevents"), //
                            configFile.getInt("max-blocks"), //
                            configFile.getBoolean("only-use-kib-in-creative-mode"), //
                            configFile.getBoolean("use-packet-events-if-detected"),  //
                            blacklists, //
                            enabledCategories);
    }

    public void setRule(Rule rule, Object value) {
        String key = switch (rule) {
            case Rule.MAX_BLOCKS -> "max-blocks";
            case Rule.ONLY_USE_KIB_IN_CREATIVE_MODE -> "only-use-kib-in-creative-mode";
            case Rule.USE_PACKET_EVENTS_IF_DETECTED -> "use-packet-events-if-detected";
        };
        saveAndReload(key, value);
    }

    public boolean addToBlacklist(MaterialGroup group, String material) {
        Set<String> blacklist = new HashSet<>(config.getBlacklistedMaterialsForGroup(group));
        if (!blacklist.add(material)) return false;

        saveAndReload(group.getBlacklistSectionKey(), new ArrayList<>(blacklist));
        return true;
    }

    public boolean removeFromBlacklist(MaterialGroup group, String material) {
        Set<String> blacklist = new HashSet<>(config.getBlacklistedMaterialsForGroup(group));
        if (!blacklist.remove(material)) return false;

        saveAndReload(group.getBlacklistSectionKey(), new ArrayList<>(blacklist));
        return true;
    }

    private void saveAndReload(String path, Object value) {
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
        reloadConfig();
    }
}
