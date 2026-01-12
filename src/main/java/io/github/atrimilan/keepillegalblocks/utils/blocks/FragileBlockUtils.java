package io.github.atrimilan.keepillegalblocks.utils.blocks;

import com.destroystokyo.paper.MaterialTags;
import io.github.atrimilan.keepillegalblocks.enums.FragileType;
import io.github.atrimilan.keepillegalblocks.enums.InteractableType;
import io.github.atrimilan.keepillegalblocks.utils.debug.DebugUtils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;

import static io.github.atrimilan.keepillegalblocks.utils.debug.DebugUtils.MessageType.WARN;

/**
 * A "fragile" block is an illegally placed block, such as a door on another door, a torch attached to a sign, or
 * anything that the game physic would generally not allow. Fragile blocks can commonly be placed with a Debug Stick, or
 * a plugin like WorldEdit.
 *
 * @see InteractableBlockUtils
 */
public final class FragileBlockUtils extends AbstractKibBlockUtils {

    public static final Map<Material, FragileType> FRAGILE_BLOCKS = new EnumMap<>(Material.class);

    /**
     * Load all fragile blocks, ignoring the materials and categories blacklisted in the {@code "fragile-blocks"}
     * section of the config.yml file.
     *
     * @param plugin The JavaPlugin instance
     */
    public static void init(JavaPlugin plugin) {
        int blacklisted = loadFragileBlocks(plugin.getConfig());
        plugin.getLogger().info(() -> "Fragile blocks loaded: " + FRAGILE_BLOCKS.values().stream() //
                .filter(type -> type != FragileType.NONE).count() + //
                                      (blacklisted > 0 ? " (" + blacklisted + " blacklisted in config.yml)" : ""));
    }

    public static int reload(JavaPlugin plugin) {
        FRAGILE_BLOCKS.clear();
        return loadFragileBlocks(plugin.getConfig());
    }

    private static int loadFragileBlocks(FileConfiguration config) {
        return loadKibBlocks(config, "fragile-blocks.", FRAGILE_BLOCKS, mat -> compute(mat.createBlockData()));
    }

    private static FragileType compute(BlockData data) {
        return switch (data) {
            case AmethystCluster ignored -> FragileType.AMETHYST_CLUSTER;
            case Bed ignored -> FragileType.BED;
            case Bell ignored -> FragileType.BELL;
            case Cake ignored -> FragileType.CAKE;
            case CaveVines ignored -> FragileType.CAVE_VINES;
            case CaveVinesPlant ignored -> FragileType.CAVE_VINES;
            case Cocoa ignored -> FragileType.COCOA;
            case Comparator ignored -> FragileType.COMPARATOR;
            case Door ignored -> FragileType.DOOR;
            case BigDripleaf ignored -> FragileType.DRIPLEAF;
            case SmallDripleaf ignored -> FragileType.DRIPLEAF;
            case HangingSign ignored -> FragileType.HANGING_SIGN;
            case Ladder ignored -> FragileType.LADDER;
            case Lantern ignored -> FragileType.LANTERN;
            case MangrovePropagule ignored -> FragileType.MANGROVE_PROPAGULE;
            case PointedDripstone ignored -> FragileType.POINTED_DRIPSTONE;
            case Rail ignored -> FragileType.RAIL;
            case RedstoneWire ignored -> FragileType.REDSTONE_WIRE;
            case Repeater ignored -> FragileType.REPEATER;
            case Scaffolding ignored -> FragileType.SCAFFOLDING;
            case SculkVein ignored -> FragileType.SCULK_VEIN;
            case SeaPickle ignored -> FragileType.SEA_PICKLE;
            case Snow ignored -> FragileType.SNOW;
            case Switch ignored -> FragileType.SWITCH;
            case TripwireHook ignored -> FragileType.TRIPWIRE_HOOK;

            default -> switch (data.getMaterial()) {
                case Material m when Tag.BANNERS.isTagged(m) -> FragileType.BANNER; // Normal + Wall
                case Material m when Tag.WOOL_CARPETS.isTagged(m) -> FragileType.CARPET;
                case Material m when MaterialTags.CORAL.isTagged(m) -> FragileType.CORAL; // Normal + Wall
                case Material m when Tag.CROPS.isTagged(m) -> FragileType.CROP;
                case Material m when Tag.FLOWERS.isTagged(m) -> FragileType.FLOWER;
                case Material m when MaterialTags.MUSHROOMS.isTagged(m) -> FragileType.MUSHROOM;
                case Material m when Tag.PRESSURE_PLATES.isTagged(m) -> FragileType.PRESSURE_PLATE;
                case Material m when Tag.SAPLINGS.isTagged(m) -> FragileType.SAPLING;
                case Material m when MaterialTags.SIGNS.isTagged(m) -> FragileType.SIGN; // Normal + Wall
                case Material m when MaterialTags.TORCHES.isTagged(m) -> FragileType.TORCH; // Normal + Redstone + Soul

                case CACTUS -> FragileType.CACTUS;
                case MOSS_CARPET -> FragileType.CARPET;
                case CHORUS_FLOWER, CHORUS_PLANT -> FragileType.CHORUS_PLANT;
                case ATTACHED_MELON_STEM, ATTACHED_PUMPKIN_STEM -> FragileType.CROP;
                case DEAD_BUSH -> FragileType.DEAD_BUSH;
                case BIG_DRIPLEAF_STEM -> FragileType.DRIPLEAF;
                case FERN, LARGE_FERN -> FragileType.FERN;
                case FROGSPAWN -> FragileType.FROGSPAWN;
                case CRIMSON_FUNGUS, WARPED_FUNGUS -> FragileType.FUNGUS;
                case GLOW_LICHEN -> FragileType.GLOW_LICHEN;
                case SHORT_GRASS, TALL_GRASS -> FragileType.GRASS;
                case HANGING_ROOTS -> FragileType.HANGING_ROOTS;
                case LILY_PAD -> FragileType.LILY_PAD;
                case CRIMSON_ROOTS, WARPED_ROOTS -> FragileType.NETHER_ROOTS;
                case NETHER_SPROUTS -> FragileType.NETHER_SPROUTS;
                case NETHER_WART -> FragileType.NETHER_WART;
                case SUGAR_CANE -> FragileType.SUGAR_CANE;
                case SWEET_BERRY_BUSH -> FragileType.SWEET_BERRY_BUSH;
                case TWISTING_VINES, TWISTING_VINES_PLANT -> FragileType.TWISTING_VINES;
                case VINE -> FragileType.VINE;
                case WEEPING_VINES, WEEPING_VINES_PLANT -> FragileType.WEEPING_VINES;

                default -> FragileType.NONE;
            };
        };
    }

    /**
     * Check if a block is fragile.
     *
     * @param block The block to check whether it is fragile
     * @return True if the block is fragile, false otherwise
     */
    public static boolean isFragile(Block block) {
        if (block == null || block.getType().isAir()) return false;

        FragileType type = FRAGILE_BLOCKS.getOrDefault(block.getType(), FragileType.NONE);
        boolean isFragile = type != FragileType.NONE;

        if (isFragile)
            DebugUtils.sendChat(() -> "Block <white>" + block.getType() + "</white> is fragile: <white>" + type, WARN);

        return isFragile;
    }
}
