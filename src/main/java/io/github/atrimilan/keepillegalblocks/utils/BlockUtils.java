package io.github.atrimilan.keepillegalblocks.utils;

import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.*;

import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.*;

public class BlockUtils {

    enum FragileType {
        AMETHYST_CLUSTER, BANNER, BED, BELL, BUTTON, CACTUS, CAKE, CARPET, CAVE_VINES, CHORUS, COCOA, COMPARATOR, CORAL,
        CROP, DEAD_BUSH, DOOR, DRIPLEAF, FERN, FLOWER, FROGSPAWN, FUNGUS, GLOW_LICHEN, GRASS, HANGING_ROOTS,
        HANGING_SIGN, LADDER, LANTERN, LEVER, LILY_PAD, MANGROVE_PROPAGULE, MUSHROOM, NETHER_ROOTS, NETHER_SPROUTS,
        NETHER_WART, POINTED_DRIPSTONE, PRESSURE_PLATE, RAIL, REDSTONE_WIRE, REPEATER, SAPLING, SCAFFOLDING, SCULK_VEIN,
        SEA_PICKLE, SIGN, SNOW, SUGAR_CANE, SWEET_BERRY_BUSH, TORCH, TRIPWIRE_HOOK, TWISTING_VINES, VINE, WEEPING_VINES
    }

    static FragileType getFragileType(BlockData data) {
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
            case TripwireHook ignored -> FragileType.TRIPWIRE_HOOK;

            default -> switch (data.getMaterial()) {
                case Material m when Tag.BANNERS.isTagged(m) -> FragileType.BANNER; // Normal + Wall
                case Material m when Tag.BUTTONS.isTagged(m) -> FragileType.BUTTON;
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
                case CHORUS_FLOWER, CHORUS_PLANT -> FragileType.CHORUS;
                case ATTACHED_MELON_STEM, ATTACHED_PUMPKIN_STEM -> FragileType.CROP;
                case DEAD_BUSH -> FragileType.DEAD_BUSH;
                case BIG_DRIPLEAF_STEM -> FragileType.DRIPLEAF;
                case FERN, LARGE_FERN -> FragileType.FERN;
                case FROGSPAWN -> FragileType.FROGSPAWN;
                case CRIMSON_FUNGUS, WARPED_FUNGUS -> FragileType.FUNGUS;
                case GLOW_LICHEN -> FragileType.GLOW_LICHEN;
                case SHORT_GRASS, TALL_GRASS -> FragileType.GRASS;
                case HANGING_ROOTS -> FragileType.HANGING_ROOTS;
                case LEVER -> FragileType.LEVER;
                case LILY_PAD -> FragileType.LILY_PAD;
                case CRIMSON_ROOTS, WARPED_ROOTS -> FragileType.NETHER_ROOTS;
                case NETHER_SPROUTS -> FragileType.NETHER_SPROUTS;
                case NETHER_WART -> FragileType.NETHER_WART;
                case SUGAR_CANE -> FragileType.SUGAR_CANE;
                case SWEET_BERRY_BUSH -> FragileType.SWEET_BERRY_BUSH;
                case TWISTING_VINES, TWISTING_VINES_PLANT -> FragileType.TWISTING_VINES;
                case VINE -> FragileType.VINE;
                case WEEPING_VINES, WEEPING_VINES_PLANT -> FragileType.WEEPING_VINES;

                default -> null;
            };
        };
    }

    enum InteractableType {
        OPENABLE, POWERABLE, LIGHTABLE, DISPENSER
    }

    static InteractableType getInteractableType(BlockData data) {
        return switch (data) {
            case Openable ignored -> InteractableType.OPENABLE;
            case Powerable ignored -> InteractableType.POWERABLE;
            case Lightable ignored -> InteractableType.LIGHTABLE;
            case Dispenser ignored -> InteractableType.DISPENSER;
            default -> null;
        };
    }

    public static boolean isFragile(Block block) {
        if (block == null || block.getType().isAir()) return false;

        FragileType type = getFragileType(block.getBlockData());
        boolean isFragile = type != null;

        if (isFragile)
            DebugUtils.sendChat(() -> "Block <white>" + block.getType() + "</white> is fragile: <white>" + type, WARN);

        return isFragile;
    }

    public static boolean isInteractable(Block block) {
        if (block == null || block.getType().isAir()) return false;

        InteractableType type = getInteractableType(block.getBlockData());
        boolean isInteractable = type != null;

        DebugUtils.sendChat(() -> "Block <white>" + block.getType() + "</white> " +
                                  (isInteractable ? ("is interactable: <white>" + type) : "is not interactable"),
                            isInteractable ? OK : ERROR);

        return isInteractable;
    }

    public static boolean willTriggerAdditionalUpdate(BlockState state) {
        return Tag.BUTTONS.isTagged(state.getType());
    }
}
