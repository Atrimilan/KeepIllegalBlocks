package io.github.atrimilan.keepillegalblocks.core;

import io.github.atrimilan.keepillegalblocks.core.classifiers.ReactiveClassifier;
import io.github.atrimilan.keepillegalblocks.core.classifiers.InteractableClassifier;
import io.github.atrimilan.keepillegalblocks.core.types.KibBlockType;
import io.github.atrimilan.keepillegalblocks.core.types.KibGroup;
import io.github.atrimilan.keepillegalblocks.models.LoadResult;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static io.github.atrimilan.keepillegalblocks.core.types.KibGroup.*;

/**
 * Handle the loading of materials into the registry.
 */
public class RegistryLoader {

    private final MaterialRegistry registry;

    private final ReactiveClassifier reactiveClassifier = new ReactiveClassifier();
    private final InteractableClassifier interactableClassifier = new InteractableClassifier();

    public RegistryLoader(MaterialRegistry registry) {
        this.registry = registry;
    }

    /**
     * Clear the material registry and fill it again based on the given settings. <b>Therefore, settings must be
     * initialized before calling this method.</b>
     *
     * @param settings The {@link Settings} to use to load the registry
     * @return A list of {@link LoadResult}, containing the count of blacklisted materials for each {@link KibGroup}
     */
    public List<LoadResult> fillMaterialRegistry(Settings settings) {
        registry.clearAll();

        int blacklistedReactiveCount = loadRegistry(settings, REACTIVE, reactiveClassifier::classify,
                                                    registry::registerReactive, true);
        int blacklistedInteractableCount = loadRegistry(settings, INTERACTABLE, interactableClassifier::classify,
                                                        registry::registerInteractable, false);

        return List.of(new LoadResult("Reactive", registry.getReactiveCount(), blacklistedReactiveCount),
                       new LoadResult("Interactable", registry.getInteractableCount(), blacklistedInteractableCount));
    }

    /**
     * Classify material of the given group, and add it to the material registry.
     *
     * @param settings                      The {@link Settings} to use to load the registry
     * @param group                         The {@link KibGroup}
     * @param classifierMethod              The classifier method to execute
     * @param registrySetter                The registry setter to execute
     * @param propagateToPlacementMaterials Whether to propagate the classification to placement materials (see
     *                                      {@link BlockData#getPlacementMaterial})
     * @param <T>                           An implementation of {@link KibBlockType}
     * @return The count of blacklisted materials
     */
    protected <T extends KibBlockType> int loadRegistry(Settings settings, KibGroup group,
                                                        Function<Material, T> classifierMethod,
                                                        BiConsumer<Material, T> registrySetter,
                                                        boolean propagateToPlacementMaterials) {
        Set<String> blacklist = settings.getBlacklistedMaterialsForGroup(group);
        Set<String> enabledCategories = settings.getEnabledCategoriesForGroup(group);

        int blacklistedCount = 0;
        Map<Material, T> classifiedMaterials = new EnumMap<>(Material.class);

        for (Material mat : getAllMaterials()) {
            if (mat.isAir() || mat.isLegacy()) continue;

            if (blacklist.contains(mat.name())) {
                blacklistedCount++; // Material is blacklisted
                continue;
            }

            T type = classifierMethod.apply(mat); // Apply the classifier method
            if (type == type.getNone()) continue;

            String configKey = type.getConfigKey();
            if (configKey != null && !enabledCategories.contains(configKey)) {
                blacklistedCount++; // Category is disabled
                continue;
            }

            classifiedMaterials.put(mat, type);
            registrySetter.accept(mat, type); // Add the material to the material registry
        }

        if (propagateToPlacementMaterials) {
            for (Map.Entry<Material, T> entry : classifiedMaterials.entrySet()) {
                Material mat = entry.getKey();
                T type = entry.getValue();

                BlockData blockData = getBlockData(mat);
                if (blockData == null) continue; // Skip, this material is not a block

                Material placementMaterial = blockData.getPlacementMaterial();

                // If not AIR (default when there's no placement material), and if not already classified
                if (placementMaterial != Material.AIR && !classifiedMaterials.containsKey(placementMaterial)) {
                    registrySetter.accept(placementMaterial, type); // Add placement material to material registry
                }
            }
        }

        return blacklistedCount;
    }

    protected BlockData getBlockData(Material material) {
        try {
            return material.createBlockData();
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return null;
        }
    }

    /**
     * @return An array of all available {@link Material}
     */
    protected Material[] getAllMaterials() {
        return Material.values();
    }
}
