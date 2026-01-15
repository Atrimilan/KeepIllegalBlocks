package io.github.atrimilan.keepillegalblocks.configuration.classifiers;

import io.github.atrimilan.keepillegalblocks.configuration.types.InteractableType;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

/**
 * An “interactable” is a block that a player can interact with directly (with right-clicking), and which physically
 * changes. In addition, only blocks that can trigger an update of “fragile” blocks are considered “interactable”.
 * <p>
 * For example, blocks such as doors, levers, candles, etc. are considered interactable, whereas blocks such as chests,
 * grindstones, enchanting tables, etc. are not.
 *
 * @see FragileClassifier
 */
public class InteractableClassifier extends AbstractClassifier {

    public InteractableType classify(Material mat) {
        BlockData data = mat.createBlockData();

        return switch (data) {
            case Campfire ignored -> InteractableType.CAMPFIRE;
            case Candle ignored -> InteractableType.CANDLE;
            case Comparator ignored -> InteractableType.COMPARATOR;
            case DaylightDetector ignored -> InteractableType.DAYLIGHT_DETECTOR;
            case Door ignored -> InteractableType.DOOR;
            case EndPortalFrame ignored -> InteractableType.END_PORTAL_FRAME;
            case Gate ignored -> InteractableType.GATE;
            case Lectern ignored -> InteractableType.LECTERN;
            case Repeater ignored -> InteractableType.REPEATER;
            case Switch ignored -> InteractableType.SWITCH; // Lever + Button
            case TrapDoor ignored -> InteractableType.TRAP_DOOR;

            default -> switch (data.getMaterial()) {
                case Material m when isCauldron(m) -> InteractableType.CAULDRON;
                case Material m when isNonPlainCopperBlock(m) -> InteractableType.COPPER_BLOCK;
                case COMPOSTER -> InteractableType.COMPOSTER;

                default -> InteractableType.NONE;
            };
        };
    }
}
