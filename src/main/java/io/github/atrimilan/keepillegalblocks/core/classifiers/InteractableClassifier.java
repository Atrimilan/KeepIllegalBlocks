package io.github.atrimilan.keepillegalblocks.core.classifiers;

import io.github.atrimilan.keepillegalblocks.core.types.InteractableType;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

/**
 * See more details about "interactable" blocks here: {@link InteractableType}.
 *
 * @see ConnectableClassifier
 * @see FragileClassifier
 */
public class InteractableClassifier extends AbstractClassifier<InteractableType> {

    @Override
    protected InteractableType classifyBlockData(BlockData blockData) {
        return switch (blockData) {
            case Campfire ignored -> InteractableType.CAMPFIRE;
            case Candle ignored -> InteractableType.CANDLE;
            case CaveVines ignored -> InteractableType.CAVE_VINES;
            case CaveVinesPlant ignored -> InteractableType.CAVE_VINES;
            case Comparator ignored -> InteractableType.COMPARATOR;
            case DaylightDetector ignored -> InteractableType.DAYLIGHT_DETECTOR;
            case Door ignored -> InteractableType.DOOR;
            case EndPortalFrame ignored -> InteractableType.END_PORTAL_FRAME;
            case Gate ignored -> InteractableType.GATE;
            case Lectern ignored -> InteractableType.LECTERN;
            case Repeater ignored -> InteractableType.REPEATER;
            case TrapDoor ignored -> InteractableType.TRAP_DOOR;

            default -> InteractableType.NONE;
        };
    }

    @Override
    protected InteractableType classifyMaterial(Material material) {
        return switch (material) {
            case Material m when isCauldron(m) -> InteractableType.CAULDRON;
            case Material m when isNonPlainCopperBlock(m) -> InteractableType.COPPER_BLOCK;
            case Material m when isStoneButton(m) -> InteractableType.STONE_BUTTON;
            case Material m when isWoodenButton(m) -> InteractableType.WOODEN_BUTTON;
            case COMPOSTER -> InteractableType.COMPOSTER;
            case LEVER -> InteractableType.LEVER;
            case SWEET_BERRY_BUSH -> InteractableType.SWEET_BERRY_BUSH;

            default -> InteractableType.NONE;
        };
    }
}
