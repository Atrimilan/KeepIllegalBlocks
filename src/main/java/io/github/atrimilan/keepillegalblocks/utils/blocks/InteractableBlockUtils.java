package io.github.atrimilan.keepillegalblocks.utils.blocks;

import com.destroystokyo.paper.MaterialTags;
import io.github.atrimilan.keepillegalblocks.utils.DebugUtils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

import java.util.EnumMap;
import java.util.Map;

import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.ERROR;
import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.OK;

/**
 * An “interactable” is a block that a player can interact with directly (with right-clicking), and which physically
 * changes. In addition, only blocks that can trigger an update of “fragile” blocks are considered “interactable”.
 * <p>
 * For example, blocks such as doors, levers, candles, etc. are considered interactable, whereas blocks such as chests,
 * grindstones, enchanting tables, etc. are not.
 *
 * @see FragileBlockUtils
 */
public class InteractableBlockUtils {

    private static final Map<Material, InteractableType> CACHE = new EnumMap<>(Material.class);

    private enum InteractableType {
        CAMPFIRE, CANDLE, CAULDRON, COMPARATOR, COMPOSTER, COPPER_BLOCK, DAYLIGHT_DETECTOR, DOOR, END_PORTAL_FRAME,
        GATE, LECTERN, NONE, REPEATER, SWITCH, TRAP_DOOR,
    }

    private static InteractableType compute(BlockData data) {
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
            case Switch ignored -> InteractableType.SWITCH;
            case TrapDoor ignored -> InteractableType.TRAP_DOOR;

            default -> switch (data.getMaterial()) {
                case Material m when Tag.CAULDRONS.isTagged(m) -> InteractableType.CAULDRON;
                case Material m when MaterialTags.CUT_COPPER_STAIRS.isTagged(m) -> InteractableType.COPPER_BLOCK;
                case Material m when MaterialTags.CUT_COPPER_SLABS.isTagged(m) -> InteractableType.COPPER_BLOCK;
                case COMPOSTER -> InteractableType.COMPOSTER;

                default -> InteractableType.NONE;
            };
        };

    }

    /**
     * Check if a block is interactable and can trigger an update to any adjacent fragile blocks.
     *
     * @param block The block to check
     * @return True if the block is interactable, false otherwise
     */
    public static boolean isInteractable(Block block) {
        if (block == null || block.getType().isAir()) return false;

        InteractableType type = CACHE.computeIfAbsent(block.getType(), mat -> compute(mat.createBlockData()));
        boolean isInteractable = type != InteractableType.NONE;

        DebugUtils.sendChat(() -> "Block <white>" + block.getType() + "</white> " +
                                  (isInteractable ? ("is interactable: <white>" + type) : "is not interactable"),
                            isInteractable ? OK : ERROR);

        return isInteractable;
    }

    /**
     * Check if the interactable block will trigger an additional update. For example, a button resets to its initial
     * state after being pressed.
     *
     * @param state The block state to check
     * @return True if the block will trigger an additional update, false otherwise
     */
    public static boolean willTriggerAdditionalUpdate(BlockState state) {
        return Tag.BUTTONS.isTagged(state.getType());
    }
}
