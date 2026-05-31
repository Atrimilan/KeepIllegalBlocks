package io.github.atrimilan.keepillegalblocks.core.classifiers;

import io.github.atrimilan.keepillegalblocks.core.types.ReactiveType;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.*;

/**
 * See more details about "reactive" blocks here: {@link ReactiveType}.
 *
 * @see InteractableClassifier
 */
public class ReactiveClassifier extends AbstractClassifier<ReactiveType> {

    @Override
    protected ReactiveType classifyBlockData(BlockData blockData) {
        return switch (blockData) {
            case AmethystCluster ignored -> ReactiveType.AMETHYST_CLUSTER;
            case Bamboo ignored -> ReactiveType.BAMBOO;
            case Bed ignored -> ReactiveType.BED;
            case Bell ignored -> ReactiveType.BELL;
            case Cake ignored -> ReactiveType.CAKE;
            case CaveVines ignored -> ReactiveType.CAVE_VINES;
            case CaveVinesPlant ignored -> ReactiveType.CAVE_VINES;
            case Cocoa ignored -> ReactiveType.COCOA;
            case Comparator ignored -> ReactiveType.COMPARATOR;
            case CoralWallFan ignored -> ReactiveType.CORAL;
            case Door ignored -> ReactiveType.DOOR;
            case BigDripleaf ignored -> ReactiveType.DRIPLEAF;
            case SmallDripleaf ignored -> ReactiveType.DRIPLEAF;
            case Fence ignored -> ReactiveType.FENCE; // Fences + Iron bars + Copper bars
            case GlassPane ignored -> ReactiveType.GLASS_PANE;
            case HangingSign ignored -> ReactiveType.HANGING_SIGN;
            case Ladder ignored -> ReactiveType.LADDER;
            case Lantern ignored -> ReactiveType.LANTERN;
            case LeafLitter ignored -> ReactiveType.LEAF_LITTER;
            case MangrovePropagule ignored -> ReactiveType.MANGROVE_PROPAGULE;
            case PointedDripstone ignored -> ReactiveType.POINTED_DRIPSTONE;
            case Rail ignored -> ReactiveType.RAIL;
            case RedstoneWire ignored -> ReactiveType.REDSTONE_WIRE;
            case Repeater ignored -> ReactiveType.REPEATER;
            case Scaffolding ignored -> ReactiveType.SCAFFOLDING;
            case SculkVein ignored -> ReactiveType.SCULK_VEIN;
            case SeaPickle ignored -> ReactiveType.SEA_PICKLE;
            case Snow ignored -> ReactiveType.SNOW;
            case Switch ignored -> ReactiveType.SWITCH; // Lever + Button
            case TripwireHook ignored -> ReactiveType.TRIPWIRE_HOOK;
            case Wall ignored -> ReactiveType.WALL;

            default -> ReactiveType.NONE;
        };
    }

    @Override
    protected ReactiveType classifyMaterial(Material material) {
        return switch (material) {
            case Material m when isBanner(m) -> ReactiveType.BANNER; // Normal + Wall
            case Material m when isCarpet(m) -> ReactiveType.CARPET;
            case Material m when isCoral(m) -> ReactiveType.CORAL; // Normal + Wall
            case Material m when isCrops(m) -> ReactiveType.CROP;
            case Material m when isFlower(m) -> ReactiveType.FLOWER;
            case Material m when isMushroom(m) -> ReactiveType.MUSHROOM;
            case Material m when isPressurePlate(m) -> ReactiveType.PRESSURE_PLATE;
            case Material m when isSapling(m) -> ReactiveType.SAPLING;
            case Material m when isSign(m) -> ReactiveType.SIGN; // Normal + Wall
            case Material m when isTorch(m) -> ReactiveType.TORCH; // Normal + Redstone + Soul

            case AMETHYST_SHARD -> ReactiveType.AMETHYST_CLUSTER; // Prevent shards duplication (on restore)
            case BAMBOO_SAPLING -> ReactiveType.BAMBOO;
            case CACTUS -> ReactiveType.CACTUS;
            case CHORUS_PLANT, CHORUS_FRUIT -> ReactiveType.CHORUS_PLANT; // FIXME: Add support for CHORUS_FLOWER
            case DEAD_BUSH -> ReactiveType.DEAD_BUSH;
            case BIG_DRIPLEAF_STEM -> ReactiveType.DRIPLEAF;
            case FERN, LARGE_FERN -> ReactiveType.FERN;
            case FROGSPAWN -> ReactiveType.FROGSPAWN;
            case CRIMSON_FUNGUS, WARPED_FUNGUS -> ReactiveType.FUNGUS;
            case GLOW_LICHEN -> ReactiveType.GLOW_LICHEN;
            case SHORT_GRASS, TALL_GRASS -> ReactiveType.GRASS;
            case HANGING_ROOTS -> ReactiveType.HANGING_ROOTS;
            case LILY_PAD -> ReactiveType.LILY_PAD;
            case CRIMSON_ROOTS, WARPED_ROOTS -> ReactiveType.NETHER_ROOTS;
            case NETHER_SPROUTS -> ReactiveType.NETHER_SPROUTS;
            case NETHER_WART -> ReactiveType.NETHER_WART;
            case SUGAR_CANE -> ReactiveType.SUGAR_CANE;
            case SWEET_BERRY_BUSH -> ReactiveType.SWEET_BERRY_BUSH;
            case TWISTING_VINES, TWISTING_VINES_PLANT -> ReactiveType.TWISTING_VINES;
            case VINE -> ReactiveType.VINE;
            case WEEPING_VINES, WEEPING_VINES_PLANT -> ReactiveType.WEEPING_VINES;

            default -> ReactiveType.NONE;
        };
    }
}
