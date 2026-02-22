package io.github.atrimilan.keepillegalblocks.configuration.classifiers;

import com.destroystokyo.paper.MaterialTags;
import io.github.atrimilan.keepillegalblocks.configuration.types.BlockType;
import org.bukkit.Material;
import org.bukkit.Tag;

public abstract class AbstractClassifier {

    public abstract BlockType classify(Material m);

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
        return Tag.FLOWERS.isTagged(m);
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
