package io.github.atrimilan.keepillegalblocks.core.classifiers;

import io.github.atrimilan.keepillegalblocks.core.types.ReactiveMaterial;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.*;

/**
 * See more details about "reactive" blocks here: {@link ReactiveMaterial}.
 *
 * @see InteractableClassifier
 */
public class ReactiveClassifier extends AbstractClassifier<ReactiveMaterial> {

    @Override
    protected ReactiveMaterial classifyBlockData(BlockData blockData) {
        return switch (blockData) {
            case AmethystCluster ignored -> ReactiveMaterial.AMETHYST_CLUSTER;
            case Bamboo ignored -> ReactiveMaterial.BAMBOO;
            case Bed ignored -> ReactiveMaterial.BED;
            case Bell ignored -> ReactiveMaterial.BELL;
            case Cake ignored -> ReactiveMaterial.CAKE;
            case CaveVines ignored -> ReactiveMaterial.CAVE_VINES;
            case CaveVinesPlant ignored -> ReactiveMaterial.CAVE_VINES;
            case Cocoa ignored -> ReactiveMaterial.COCOA;
            case Comparator ignored -> ReactiveMaterial.COMPARATOR;
            case CoralWallFan ignored -> ReactiveMaterial.CORAL;
            case Door ignored -> ReactiveMaterial.DOOR;
            case BigDripleaf ignored -> ReactiveMaterial.DRIPLEAF;
            case SmallDripleaf ignored -> ReactiveMaterial.DRIPLEAF;
            case Fence ignored -> ReactiveMaterial.FENCE; // Fences + Iron bars + Copper bars
            case GlassPane ignored -> ReactiveMaterial.GLASS_PANE;
            case HangingSign ignored -> ReactiveMaterial.HANGING_SIGN;
            case Ladder ignored -> ReactiveMaterial.LADDER;
            case Lantern ignored -> ReactiveMaterial.LANTERN;
            case MangrovePropagule ignored -> ReactiveMaterial.MANGROVE_PROPAGULE;
            case Rail ignored -> ReactiveMaterial.RAIL;
            case RedstoneWire ignored -> ReactiveMaterial.REDSTONE_WIRE;
            case Repeater ignored -> ReactiveMaterial.REPEATER;
            case Scaffolding ignored -> ReactiveMaterial.SCAFFOLDING;
            case SculkVein ignored -> ReactiveMaterial.SCULK_VEIN;
            case SeaPickle ignored -> ReactiveMaterial.SEA_PICKLE;
            case Snow ignored -> ReactiveMaterial.SNOW;
            case Switch ignored -> ReactiveMaterial.SWITCH; // Lever + Button
            case TripwireHook ignored -> ReactiveMaterial.TRIPWIRE_HOOK;
            case Wall ignored -> ReactiveMaterial.WALL;
            case BlockData bd when hasAnyInterface(bd, "LeafLitter") -> ReactiveMaterial.LEAF_LITTER;
            case BlockData bd when hasAnyInterface(bd, "Speleothem", "PointedDripstone") -> ReactiveMaterial.SPELEOTHEM;

            default -> ReactiveMaterial.NONE;
        };
    }

    @Override
    protected ReactiveMaterial classifyMaterial(Material material) {
        return switch (material) {
            case Material m when isBanner(m) -> ReactiveMaterial.BANNER; // Normal + Wall
            case Material m when isCarpet(m) -> ReactiveMaterial.CARPET;
            case Material m when isCoral(m) -> ReactiveMaterial.CORAL; // Normal + Wall
            case Material m when isCrops(m) -> ReactiveMaterial.CROP;
            case Material m when isFlower(m) -> ReactiveMaterial.FLOWER;
            case Material m when isMushroom(m) -> ReactiveMaterial.MUSHROOM;
            case Material m when isPressurePlate(m) -> ReactiveMaterial.PRESSURE_PLATE;
            case Material m when isSapling(m) -> ReactiveMaterial.SAPLING;
            case Material m when isSign(m) -> ReactiveMaterial.SIGN; // Normal + Wall
            case Material m when isTorch(m) -> ReactiveMaterial.TORCH; // Normal + Redstone + Soul

            case AMETHYST_SHARD -> ReactiveMaterial.AMETHYST_CLUSTER; // Prevent shards duplication (on restore)
            case BAMBOO_SAPLING -> ReactiveMaterial.BAMBOO;
            case CACTUS -> ReactiveMaterial.CACTUS;
            case CHORUS_PLANT, CHORUS_FRUIT -> ReactiveMaterial.CHORUS_PLANT; // FIXME: Add support for CHORUS_FLOWER
            case DEAD_BUSH -> ReactiveMaterial.DEAD_BUSH;
            case BIG_DRIPLEAF_STEM -> ReactiveMaterial.DRIPLEAF;
            case FERN, LARGE_FERN -> ReactiveMaterial.FERN;
            case FROGSPAWN -> ReactiveMaterial.FROGSPAWN;
            case CRIMSON_FUNGUS, WARPED_FUNGUS -> ReactiveMaterial.FUNGUS;
            case GLOW_LICHEN -> ReactiveMaterial.GLOW_LICHEN;
            case SHORT_GRASS, TALL_GRASS -> ReactiveMaterial.GRASS;
            case HANGING_ROOTS -> ReactiveMaterial.HANGING_ROOTS;
            case LILY_PAD -> ReactiveMaterial.LILY_PAD;
            case CRIMSON_ROOTS, WARPED_ROOTS -> ReactiveMaterial.NETHER_ROOTS;
            case NETHER_SPROUTS -> ReactiveMaterial.NETHER_SPROUTS;
            case NETHER_WART -> ReactiveMaterial.NETHER_WART;
            case SUGAR_CANE -> ReactiveMaterial.SUGAR_CANE;
            case SWEET_BERRY_BUSH -> ReactiveMaterial.SWEET_BERRY_BUSH;
            case TWISTING_VINES, TWISTING_VINES_PLANT -> ReactiveMaterial.TWISTING_VINES;
            case VINE -> ReactiveMaterial.VINE;
            case WEEPING_VINES, WEEPING_VINES_PLANT -> ReactiveMaterial.WEEPING_VINES;

            default -> ReactiveMaterial.NONE;
        };
    }
}
