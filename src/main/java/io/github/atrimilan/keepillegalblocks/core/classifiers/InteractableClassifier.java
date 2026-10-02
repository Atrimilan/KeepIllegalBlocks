package io.github.atrimilan.keepillegalblocks.core.classifiers;

import io.github.atrimilan.keepillegalblocks.models.InteractableMaterial;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

/**
 * See more details about "interactable" blocks here: {@link InteractableMaterial}.
 *
 * @see ReactiveClassifier
 */
public class InteractableClassifier extends AbstractClassifier<InteractableMaterial> {

    @Override
    protected InteractableMaterial classifyBlockData(BlockData blockData) {
        return switch (blockData) {
            case Campfire ignored -> InteractableMaterial.CAMPFIRE;
            case Candle ignored -> InteractableMaterial.CANDLE;
            case CaveVines ignored -> InteractableMaterial.CAVE_VINES;
            case CaveVinesPlant ignored -> InteractableMaterial.CAVE_VINES;
            case ChiseledBookshelf ignored -> InteractableMaterial.CHISELED_BOOKSHELF;
            case Comparator ignored -> InteractableMaterial.COMPARATOR;
            case DaylightDetector ignored -> InteractableMaterial.DAYLIGHT_DETECTOR;
            case Door ignored -> InteractableMaterial.DOOR;
            case EndPortalFrame ignored -> InteractableMaterial.END_PORTAL_FRAME;
            case Gate ignored -> InteractableMaterial.GATE;
            case Lectern ignored -> InteractableMaterial.LECTERN;
            case Repeater ignored -> InteractableMaterial.REPEATER;
            case TrapDoor ignored -> InteractableMaterial.TRAP_DOOR;

            default -> InteractableMaterial.NONE;
        };
    }

    @Override
    protected InteractableMaterial classifyMaterial(Material material) {
        return switch (material) {
            case Material m when isCauldron(m) -> InteractableMaterial.CAULDRON;
            case Material m when isNonPlainCopperBlock(m) -> InteractableMaterial.COPPER_BLOCK;
            case Material m when isStoneButton(m) -> InteractableMaterial.STONE_BUTTON;
            case Material m when isWoodenButton(m) -> InteractableMaterial.WOODEN_BUTTON;
            case COMPOSTER -> InteractableMaterial.COMPOSTER;
            case LEVER -> InteractableMaterial.LEVER;
            case SWEET_BERRY_BUSH -> InteractableMaterial.SWEET_BERRY_BUSH;

            default -> InteractableMaterial.NONE;
        };
    }
}
