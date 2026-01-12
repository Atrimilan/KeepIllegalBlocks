package io.github.atrimilan.keepillegalblocks.utils.blocks;

import io.github.atrimilan.keepillegalblocks.enums.KibBlockType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract sealed class AbstractKibBlockUtils permits InteractableBlockUtils, FragileBlockUtils {

    /**
     * Initialize a block map based on the given method to compute. Materials and categories blacklisted in the
     * config.yml file will not be included in the map.
     *
     * @param config        The config.yml file
     * @param sectionKey    The section to get the enabled categories from
     * @param blockMap      The block map to initialize
     * @param computeMethod The method to compute
     * @param <T>           An implementation of {@link KibBlockType}
     * @return The count of blacklisted blocks
     */
    protected static <T extends KibBlockType> int loadKibBlocks(FileConfiguration config, String sectionKey,
                                                                Map<Material, T> blockMap,
                                                                Function<Material, T> computeMethod) {
        Set<String> blacklist = getBlacklist(config, sectionKey);
        Map<String, Boolean> enabledCategories = getEnabledCategories(config, sectionKey);

        int blacklistedCount = 0;
        for (Material mat : Material.values()) {
            if (isInvalidBlock(mat)) continue;

            if (blacklist.contains(mat.name())) {
                blacklistedCount++; // Material is blacklisted
            } else {
                T type = computeMethod.apply(mat); // Apply the compute method
                String configKey = type.getConfigKey();

                if (configKey != null && !enabledCategories.getOrDefault(configKey, true)) {
                    blacklistedCount++; // Category is blacklisted
                } else {
                    blockMap.put(mat, type);
                }
            }
        }
        return blacklistedCount;
    }

    private static Set<String> getBlacklist(FileConfiguration config, String sectionKey) {
        return new HashSet<>(config.getStringList(sectionKey + "blacklist"));
    }

    private static Map<String, Boolean> getEnabledCategories(FileConfiguration config, String sectionKey) {
        ConfigurationSection section = config.getConfigurationSection(sectionKey + "categories");
        if (section == null) return Map.of();

        return section.getKeys(false).stream()
                .collect(Collectors.toMap(key -> key, key -> section.getBoolean(key, true)));
    }

    private static boolean isInvalidBlock(Material mat) {
        return !mat.isBlock() || mat.isAir() || mat.isLegacy();
    }
}
