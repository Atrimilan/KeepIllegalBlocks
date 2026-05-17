package io.github.atrimilan.keepillegalblocks.core.classifiers;

import com.destroystokyo.paper.MaterialTags;
import io.github.atrimilan.keepillegalblocks.core.types.KibBlockType;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;

public abstract class AbstractClassifier<T extends KibBlockType> {

    /**
     * Classify a material into a {@link KibBlockType}:
     * <li>First, if the material is a block, it is classified based on its block data</li>
     * <li>If the material is not a block, or the previous step returns {@code NONE}, it is classified based on the
     * material itself.</li>
     *
     * @param material The material to classify
     * @return The classified {@link KibBlockType}
     */
    public T classify(Material material) {
        if (material.isBlock()) {
            BlockData blockData = getBlockData(material);

            if (blockData != null) {
                T kibBlockType = classifyBlockData(blockData);
                if (!kibBlockType.getNone().equals(kibBlockType)) {
                    return kibBlockType;
                }
            }
        }
        return classifyMaterial(material);
    }

    protected BlockData getBlockData(Material material) {
        try {
            return material.createBlockData();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected abstract T classifyMaterial(Material m);

    protected abstract T classifyBlockData(BlockData m);

    protected boolean isCauldron(Material m) {
        return Tag.CAULDRONS.isTagged(m);
    }

    protected boolean isNonPlainCopperBlock(Material m) {
        return MaterialTags.CUT_COPPER_STAIRS.isTagged(m) || MaterialTags.CUT_COPPER_SLABS.isTagged(m);
    }

    protected boolean isBanner(Material m) {
        return Tag.BANNERS.isTagged(m);
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

    protected boolean isCrop(Material m) {
        return Tag.CROPS.isTagged(m) || Material.ATTACHED_MELON_STEM.equals(m) ||
               Material.ATTACHED_PUMPKIN_STEM.equals(m);
    }

    protected boolean isFlower(Material m) {
        return Tag.FLOWERS.isTagged(m) && !Material.CHORUS_FLOWER.equals(m); // FIXME: Add support for CHORUS_FLOWER
    }

    protected boolean isMushroom(Material m) {
        return MaterialTags.MUSHROOMS.isTagged(m);
    }

    protected boolean isPressurePlate(Material m) {
        return Tag.PRESSURE_PLATES.isTagged(m);
    }

    protected boolean isSapling(Material m) {
        return Tag.SAPLINGS.isTagged(m);
    }

    protected boolean isSign(Material m) {
        return Tag.ALL_SIGNS.isTagged(m);
    }

    protected boolean isTorch(Material m) {
        return MaterialTags.TORCHES.isTagged(m);
    }
}
