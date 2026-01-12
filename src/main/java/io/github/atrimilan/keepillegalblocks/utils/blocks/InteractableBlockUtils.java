package io.github.atrimilan.keepillegalblocks.utils.blocks;

import com.destroystokyo.paper.MaterialTags;
import io.github.atrimilan.keepillegalblocks.enums.InteractableType;
import io.github.atrimilan.keepillegalblocks.utils.debug.DebugUtils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;

import static io.github.atrimilan.keepillegalblocks.utils.debug.DebugUtils.MessageType.ERROR;
import static io.github.atrimilan.keepillegalblocks.utils.debug.DebugUtils.MessageType.OK;

/**
 * An “interactable” is a block that a player can interact with directly (with right-clicking), and which physically
 * changes. In addition, only blocks that can trigger an update of “fragile” blocks are considered “interactable”.
 * <p>
 * For example, blocks such as doors, levers, candles, etc. are considered interactable, whereas blocks such as chests,
 * grindstones, enchanting tables, etc. are not.
 *
 * @see FragileBlockUtils
 */
public final class InteractableBlockUtils extends AbstractKibBlockUtils {

    private static final Map<Material, InteractableType> INTERACTABLE_BLOCKS = new EnumMap<>(Material.class);

    /**
     * Load all interactable blocks, ignoring the materials and categories blacklisted in the
     * {@code "interactable-blocks"} section of the config.yml file.
     *
     * @param plugin The JavaPlugin instance
     */
    public static void init(JavaPlugin plugin) {
        int blacklisted = loadInteractableBlocks(plugin.getConfig());
        plugin.getLogger().info(() -> "Interactable blocks loaded: " + INTERACTABLE_BLOCKS.values().stream() //
                .filter(type -> type != InteractableType.NONE).count() + //
                                      (blacklisted > 0 ? " (" + blacklisted + " blacklisted in config.yml)" : ""));
    }

    private static int loadInteractableBlocks(FileConfiguration config) {
        return loadKibBlocks(config, "interactable-blocks.", INTERACTABLE_BLOCKS,
                             mat -> compute(mat.createBlockData()));
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
     * Check if a block is interactable.
     *
     * @param block The block to check
     * @return True if the block is interactable, false otherwise
     */
    public static boolean isInteractable(Block block) {
        if (block == null || INTERACTABLE_BLOCKS.isEmpty()) return false;

        InteractableType type = INTERACTABLE_BLOCKS.getOrDefault(block.getType(), InteractableType.NONE);
        boolean isInteractable = type != InteractableType.NONE;

        DebugUtils.sendChat(() -> "Block <white>" + block.getType() + "</white> " +
                                  (isInteractable ? ("is interactable: <white>" + type) : "is not interactable"),
                            isInteractable ? OK : ERROR);

        return isInteractable;
    }

    /**
     * Check if the interactable block will trigger an additional update. For example, a button will reset to its
     * initial state 1 second after being pressed.
     *
     * @param state The block state to check
     * @return True if the block will trigger an additional update, false otherwise
     */
    public static boolean willTriggerAdditionalUpdate(BlockState state) {
        return Tag.BUTTONS.isTagged(state.getType());
    }
}
