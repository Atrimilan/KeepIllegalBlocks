package io.github.atrimilan.keepillegalblocks.core;

import io.github.atrimilan.keepillegalblocks.core.classifiers.ConnectableClassifier;
import io.github.atrimilan.keepillegalblocks.core.classifiers.FragileClassifier;
import io.github.atrimilan.keepillegalblocks.core.classifiers.InteractableClassifier;
import io.github.atrimilan.keepillegalblocks.core.types.KibBlockType;
import io.github.atrimilan.keepillegalblocks.core.types.KibGroup;
import io.github.atrimilan.keepillegalblocks.models.LoadResult;
import org.bukkit.Material;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static io.github.atrimilan.keepillegalblocks.core.types.KibGroup.*;

/**
 * Handle the loading of materials into the registry.
 */
public class RegistryLoader {

    private final MaterialRegistry registry;

    private final FragileClassifier fragileClassifier = new FragileClassifier();
    private final ConnectableClassifier connectableClassifier = new ConnectableClassifier();
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

        int blacklistedFragileCount = loadRegistry(settings, FRAGILE, fragileClassifier::classify,
                                                   registry::registerFragile);
        int blacklistedConnectableCount = loadRegistry(settings, CONNECTABLE, connectableClassifier::classify,
                                                       registry::registerConnectable);
        int blacklistedInteractableCount = loadRegistry(settings, INTERACTABLE, interactableClassifier::classify,
                                                        registry::registerInteractable);

        return List.of(new LoadResult("Fragile", registry.getFragileCount(), blacklistedFragileCount),
                       new LoadResult("Connectable", registry.getConnectableCount(), blacklistedConnectableCount),
                       new LoadResult("Interactable", registry.getInteractableCount(), blacklistedInteractableCount));
    }

    /**
     * Classify material of the given group, and add it to the material registry.
     *
     * @param settings         The {@link Settings} to use to load the registry
     * @param group            The {@link KibGroup}
     * @param classifierMethod The classifier method to execute
     * @param registrySetter   The registry setter to execute
     * @param <T>              An implementation of {@link KibBlockType}
     * @return The count of blacklisted materials
     */
    protected <T extends KibBlockType> int loadRegistry(Settings settings, KibGroup group,
                                                      Function<Material, T> classifierMethod,
                                                      BiConsumer<Material, T> registrySetter) {
        Set<String> blacklist = settings.getBlacklistedMaterialsForGroup(group);
        Set<String> enabledCategories = settings.getEnabledCategoriesForGroup(group);

        int blacklistedCount = 0;

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

            registrySetter.accept(mat, type); // Add the material to the material registry
        }

        return blacklistedCount;
    }

    /**
     * @return An array of all available {@link Material}
     */
    protected Material[] getAllMaterials() {
        return Material.values();
    }
}
