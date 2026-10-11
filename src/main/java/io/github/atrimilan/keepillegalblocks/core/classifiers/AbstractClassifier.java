package io.github.atrimilan.keepillegalblocks.core.classifiers;

import com.destroystokyo.paper.MaterialTags;
import io.github.atrimilan.keepillegalblocks.models.MaterialType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractClassifier<T extends MaterialType> {

    /**
     * Classify a material into a {@link MaterialType}:
     * <li>First, if the material is a block, it is classified based on its block data</li>
     * <li>If the material is not a block, or the previous step returns {@code NONE}, it is classified based on the
     * material itself.</li>
     *
     * @param material The material to classify
     * @return The classified {@link MaterialType}
     */
    public T classify(Material material) {
        if (material.isBlock()) {
            BlockData blockData = getBlockData(material);

            if (blockData != null) {
                T materialType = classifyBlockData(blockData);
                if (!materialType.getNone().equals(materialType)) {
                    return materialType;
                }
            }
        }
        return classifyMaterial(material);
    }

    protected BlockData getBlockData(Material material) {
        try {
            return material.createBlockData();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    protected abstract T classifyMaterial(Material m);

    protected abstract T classifyBlockData(BlockData m);

    /**
     * Check if the block data class directly implements one of the given target interfaces.
     *
     * @param bd               The {@link BlockData} to check
     * @param targetInterfaces The simple names of the interfaces to look up
     * @return {@code true} if the block data class implements at least one of the given target interfaces,
     * {@code false} otherwise.
     */
    protected boolean hasAnyInterface(BlockData bd, String... targetInterfaces) {
        Set<String> targets = Set.of(targetInterfaces);
        return Arrays.stream(bd.getClass().getInterfaces()).map(Class::getSimpleName).anyMatch(targets::contains);
    }

    /**
     * Check if a material matches any of the provided material names.
     *
     * @param material The {@link Material} to check
     * @param names    The names of the materials to match against
     * @return {@code true} if the material matches at least one of the names, {@code false} otherwise
     */
    protected boolean matchAnyMaterial(Material material, String... names) {
        return Arrays.stream(names).map(Material::matchMaterial).anyMatch(material::equals);
    }

    /**
     * Cached tags for faster lookup.
     */
    private final Map<String, Tag<Material>> tagCache = new HashMap<>();

    /**
     * Get the {@link MaterialTags} class using reflection.
     */
    protected Class<?> getMaterialTagsClass() throws ClassNotFoundException {
        return Class.forName("com.destroystokyo.paper.MaterialTags");
    }

    /**
     * Check whether a {@link Material} belongs to a specific {@link Tag} in the {@code "items"} registry. Falls back to
     * a legacy field in Paper's {@link MaterialTags} (using reflection for backward compatibility with older Minecraft
     * versions, where the modern tag is unavailable).
     *
     * @param material              The material to check
     * @param tagName               The modern tag name
     * @param legacyMaterialTagName The corresponding field name in Paper's {@link MaterialTags}
     * @return {@code true} if the material belongs to the resolved tag, {@code false} otherwise
     */
    protected boolean isTagged(Material material, String tagName, String legacyMaterialTagName) {
        Tag<Material> tag = tagCache.computeIfAbsent(tagName, key -> {
            // Try to resolve the tag in the Bukkit "items" registry
            Tag<Material> modernTag = Bukkit.getTag(Tag.REGISTRY_ITEMS, NamespacedKey.minecraft(tagName),
                                                    Material.class);
            if (modernTag != null) return modernTag;

            // Fallback to Paper MaterialTags using reflection
            try {
                Object legacyTagObj = getMaterialTagsClass().getField(legacyMaterialTagName).get(null);
                if (legacyTagObj instanceof Tag<?> t) {
                    @SuppressWarnings("unchecked") Tag<Material> legacyTag = (Tag<Material>) t;
                    return legacyTag;
                }
            } catch (ReflectiveOperationException ignored) {
                // MaterialTags or field in MaterialTags is unavailable
            }
            return null;
        });

        return tag != null && tag.isTagged(material);
    }

    protected boolean isCauldron(Material m) {
        return Tag.CAULDRONS.isTagged(m);
    }

    protected boolean isNonPlainCopperBlock(Material m) {
        return MaterialTags.CUT_COPPER_STAIRS.isTagged(m) || MaterialTags.CUT_COPPER_SLABS.isTagged(m);
    }

    protected boolean isBanner(Material m) {
        return Tag.BANNERS.isTagged(m);
    }

    protected boolean isBush(Material m) {
        return matchAnyMaterial(m, "BUSH", "FIREFLY_BUSH");
    }

    protected boolean isWoodenButton(Material m) {
        return Tag.WOODEN_BUTTONS.isTagged(m);
    }

    protected boolean isStoneButton(Material m) {
        return Tag.STONE_BUTTONS.isTagged(m);
    }

    protected boolean isCarpet(Material m) {
        return Tag.WOOL_CARPETS.isTagged(m) || Material.MOSS_CARPET.equals(m);
    }

    protected boolean isCoral(Material m) {
        return MaterialTags.CORAL.isTagged(m);
    }

    protected boolean isCrops(Material m) {
        return Tag.CROPS.isTagged(m);
    }

    protected boolean isFlower(Material m) {
        return Tag.FLOWERS.isTagged(m);
    }

    protected boolean isMushroom(Material m) {
        return isTagged(m, "mushrooms", "MUSHROOMS");
    }

    protected boolean isPressurePlate(Material m) {
        return Tag.PRESSURE_PLATES.isTagged(m);
    }

    protected boolean isSapling(Material m) {
        return Tag.SAPLINGS.isTagged(m);
    }

    protected boolean isRedShrub(Material m) {
        return matchAnyMaterial(m, "RED_SHRUB");
    }

    protected boolean isSign(Material m) {
        return Tag.ALL_SIGNS.isTagged(m);
    }

    protected boolean isTorch(Material m) {
        return MaterialTags.TORCHES.isTagged(m);
    }
}
