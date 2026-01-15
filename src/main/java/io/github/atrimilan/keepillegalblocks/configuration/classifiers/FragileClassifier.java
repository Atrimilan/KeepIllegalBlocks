package io.github.atrimilan.keepillegalblocks.configuration.classifiers;

import io.github.atrimilan.keepillegalblocks.configuration.types.FragileType;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.*;

/**
 * A "fragile" block is an illegally placed block, such as a door on another door, a torch attached to a sign, or
 * anything that the game physic would generally not allow. Fragile blocks can commonly be placed with a Debug Stick, or
 * plugins like WorldEdit.
 *
 * @see InteractableClassifier
 */
public class FragileClassifier extends AbstractClassifier {

    public FragileType classify(Material mat) {
        BlockData data = mat.createBlockData();

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
            case Switch ignored -> FragileType.SWITCH; // Lever + Button
            case TripwireHook ignored -> FragileType.TRIPWIRE_HOOK;

            default -> switch (data.getMaterial()) {
                case Material m when isBanner(m) -> FragileType.BANNER; // Normal + Wall
                case Material m when isCarpet(m) -> FragileType.CARPET;
                case Material m when isCoral(m) -> FragileType.CORAL; // Normal + Wall
                case Material m when isCrop(m) -> FragileType.CROP;
                case Material m when isFlower(m) -> FragileType.FLOWER;
                case Material m when isMushroom(m) -> FragileType.MUSHROOM;
                case Material m when isPressurePlate(m) -> FragileType.PRESSURE_PLATE;
                case Material m when isSapling(m) -> FragileType.SAPLING;
                case Material m when isSign(m) -> FragileType.SIGN; // Normal + Wall
                case Material m when isTorch(m) -> FragileType.TORCH; // Normal + Redstone + Soul

                case CACTUS -> FragileType.CACTUS;
                case CHORUS_FLOWER, CHORUS_PLANT -> FragileType.CHORUS_PLANT;
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
}
