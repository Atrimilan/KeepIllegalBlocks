package io.github.atrimilan.keepillegalblocks.configuration.classifiers;

import io.github.atrimilan.keepillegalblocks.configuration.types.InteractableType;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

/**
 * See more details about "interactable" blocks here: {@link InteractableType}.
 *
 * @see FragileClassifier
 */
public class InteractableClassifier extends AbstractClassifier {

    public InteractableType classify(Material mat) {
        BlockData data = mat.createBlockData();

        return switch (data) {
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

            default -> switch (data.getMaterial()) {
                case Material m when isCauldron(m) -> InteractableType.CAULDRON;
                case Material m when isNonPlainCopperBlock(m) -> InteractableType.COPPER_BLOCK;
                case Material m when isStoneButton(m) -> InteractableType.STONE_BUTTON;
                case Material m when isWoodenButton(m) -> InteractableType.WOODEN_BUTTON;
                case COMPOSTER -> InteractableType.COMPOSTER;
                case LEVER -> InteractableType.LEVER;
                case SWEET_BERRY_BUSH -> InteractableType.SWEET_BERRY_BUSH;

                default -> InteractableType.NONE;
            };
        };
    }
}
