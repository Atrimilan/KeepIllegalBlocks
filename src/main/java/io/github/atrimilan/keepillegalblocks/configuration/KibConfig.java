package io.github.atrimilan.keepillegalblocks.configuration;

import io.github.atrimilan.keepillegalblocks.configuration.classifiers.FragileClassifier;
import io.github.atrimilan.keepillegalblocks.configuration.classifiers.InteractableClassifier;
import io.github.atrimilan.keepillegalblocks.configuration.types.BlockType;
import io.github.atrimilan.keepillegalblocks.configuration.types.FragileType;
import io.github.atrimilan.keepillegalblocks.configuration.types.InteractableType;
import io.github.atrimilan.keepillegalblocks.models.LoadResult;
import io.github.atrimilan.keepillegalblocks.utils.DebugUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Function;

import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.ERROR;
import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.OK;

public class KibConfig {

    private final JavaPlugin plugin;
    private final FragileClassifier fragileClassifier = new FragileClassifier();
    private final InteractableClassifier interactableClassifier = new InteractableClassifier();

    private final Map<Material, FragileType> fragileBlocks = new EnumMap<>(Material.class);
    private final Map<Material, InteractableType> interactableBlocks = new EnumMap<>(Material.class);

    private boolean isPacketEventsPresent;

    private int maxBlocks;
    private boolean isOnlyEnabledInCreativeMode;
    private boolean isPacketEventsEnabled;

    public KibConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Load configuration from config.yml, and load fragile and interactable registries.
     */
    public void init() {
        plugin.saveDefaultConfig(); // Save a full copy of the default config.yml file
        plugin.getConfig().options().copyDefaults(true); // For any missing value, copy them from the default config.yml
        plugin.saveConfig();

        this.isPacketEventsPresent = plugin.getServer().getPluginManager().isPluginEnabled("packetevents");

        plugin.getLogger().info(isPacketEventsPresent ?
                                "PacketEvents is present, it will be used if \"use-packet-events-if-detected\" is enabled in the config" :
                                "PacketEvents is not present, it is recommended to improve rendering and performance on the client-side");

        List<LoadResult> results = loadConfig();

        for (LoadResult result : results)
            plugin.getLogger().info(result::consoleFormat);
    }

    /**
     * Reload configuration from config.yml, and reload fragile and interactable registries.
     */
    public List<LoadResult> reload() {
        plugin.reloadConfig();

        fragileBlocks.clear();
        interactableBlocks.clear();

        return loadConfig();
    }

    /**
     * Get the maximum number of blocks to record.
     */
    public int getMaxBlocks() {
        return maxBlocks;
    }

    public boolean isOnlyEnabledInCreativeMode() {
        return isOnlyEnabledInCreativeMode;
    }

    public boolean isPacketEventsPresent() {
        return isPacketEventsPresent && isPacketEventsEnabled;
    }

    /**
     * Load config, including plugin's behavior, and fragile and interactable registries.
     */
    List<LoadResult> loadConfig() {
        FileConfiguration configFile = plugin.getConfig();

        maxBlocks = configFile.getInt("max-blocks");
        isOnlyEnabledInCreativeMode = configFile.getBoolean("only-use-kib-in-creative-mode");
        isPacketEventsEnabled = configFile.getBoolean("use-packet-events-if-detected");

        int blacklistedFragile = loadRegistry(configFile, "fragile-blocks.", //
                                              fragileBlocks, fragileClassifier::classify);
        int blacklistedInteractable = loadRegistry(configFile, "interactable-blocks.", //
                                                   interactableBlocks, interactableClassifier::classify);

        return List.of(new LoadResult("Fragile", fragileBlocks.size(), blacklistedFragile),
                       new LoadResult("Interactable", interactableBlocks.size(), blacklistedInteractable));
    }

    public boolean isFragile(Material mat) {
        if (mat == null || fragileBlocks.isEmpty()) return false;

        FragileType type = fragileBlocks.getOrDefault(mat, FragileType.NONE);
        return type != FragileType.NONE;
    }

    public boolean isInteractable(Material mat) {
        if (mat == null || interactableBlocks.isEmpty()) return false;

        InteractableType type = interactableBlocks.getOrDefault(mat, InteractableType.NONE);
        boolean isInteractable = type != InteractableType.NONE;

        DebugUtils.sendChat(() -> "Block <white>" + mat + "</white> " + //
                                  (isInteractable ? ("is interactable: <white>" + mat) : //
                                   "is not interactable"), isInteractable ? OK : ERROR);
        return isInteractable;
    }

    /**
     * Load the given block map based on the given classifier methode. Materials and categories blacklisted in the given
     * config.yml section will not be included in the map.
     *
     * @param config           The config.yml file
     * @param sectionKey       The config.yml section to check
     * @param blockMap         The block map to initialize
     * @param classifierMethod The classifier method to execute
     * @param <T>              An implementation of {@link BlockType}
     * @return The count of blacklisted blocks
     */
    <T extends BlockType> int loadRegistry(FileConfiguration config, String sectionKey, Map<Material, T> blockMap,
                                           Function<Material, T> classifierMethod) {
        Set<String> blacklist = getBlacklist(config, sectionKey);
        Map<String, Boolean> enabledCategories = getEnabledCategories(config, sectionKey);

        int blacklisted = 0;
        for (Material mat : Material.values()) {
            if (!mat.isBlock() || mat.isAir() || mat.isLegacy()) continue;

            if (classifyMaterial(mat, blockMap, classifierMethod, blacklist, enabledCategories)) {
                blacklisted++;
            }
        }
        return blacklisted;
    }

    private Set<String> getBlacklist(FileConfiguration config, String sectionKey) {
        return new HashSet<>(config.getStringList(sectionKey + "blacklist"));
    }

    private Map<String, Boolean> getEnabledCategories(FileConfiguration config, String sectionKey) {
        ConfigurationSection section = config.getConfigurationSection(sectionKey + "categories");
        Map<String, Boolean> map = new HashMap<>();
        if (section != null) {
            for (String key : section.getKeys(false))
                map.put(key, section.getBoolean(key, true));
        }
        return map;
    }

    private <T extends BlockType> boolean classifyMaterial(Material mat, Map<Material, T> blockMap,
                                                           Function<Material, T> classifierMethod,
                                                           Set<String> blacklist,
                                                           Map<String, Boolean> enabledCategories) {
        if (blacklist.contains(mat.name())) {
            return true; // Material is blacklisted
        }
        T type = classifierMethod.apply(mat); // Apply the classifier method
        if (type == type.getNone()) {
            return false;
        }
        String configKey = type.getConfigKey();
        if (configKey != null && !enabledCategories.getOrDefault(configKey, true)) {
            return true; // Category is blacklisted
        }
        blockMap.put(mat, type);
        return false;
    }
}
