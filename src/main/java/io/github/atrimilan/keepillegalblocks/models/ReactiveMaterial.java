package io.github.atrimilan.keepillegalblocks.models;

/**
 * A reactive block is one that reacts to a physics update triggered on one of its adjacent blocks and may therefore
 * break or update its block data.
 * <p>
 * These blocks can be placed illegally in a way that the game's physics would not normally allow, by using a Debug
 * Stick or plugins like Axiom or WorldEdit. For example, a torch can be placed on a levitating button, and interacting
 * with the button will destroy both blocks.
 * <p>
 * Most reactive blocks break instantly on the first tick, except for the following cascade-breaking blocks, which break
 * progressively starting on the second tick after interaction: {@code BAMBOO} (not {@code BAMBOO_SAPLING}),
 * {@code CACTUS}, {@code CAVE_VINES} and {@code CAVE_VINES_PLANT}, {@code CHORUS_PLANT} and {@code CHORUS_FLOWER},
 * {@code POINTED_DRIPSTONE}, {@code SCAFFOLDING}, {@code SUGAR_CANE}, {@code SULFUR_SPIKE}, {@code TWISTING_VINES} and
 * {@code TWISTING_VINES_PLANT}, {@code WEEPING_VINES} and {@code WEEPING_VINES_PLANT}.
 * <p>
 * A reactive block is "connectable" when it automatically connects to adjacent blocks, such as fences or walls. These
 * connections can also be modified illegally, and they will return to their normal connection state when a physics
 * update is triggered on an adjacent block.
 *
 * @see InteractableMaterial
 */
public enum ReactiveMaterial implements MaterialType {
    AMETHYST_CLUSTER("amethyst_clusters"),
    BAMBOO("bamboos"),
    BANNER("banners"),
    BED("beds"),
    BELL("bells"),
    CACTUS("cactus"),
    CAKE("cakes"),
    CARPET("carpets"),
    CAVE_VINES("cave_vines", true),
    CHORUS_PLANT("chorus_plants"), // FIXME: Add support for CHORUS_FLOWER
    COCOA("cocoa"),
    COMPARATOR("comparators"),
    CORAL("corals"),
    CROP("crops"),
    DEAD_BUSH("dead_bushes"),
    DOOR("doors"),
    DRIPLEAF("dripleaves"),
    FENCE("fences", true),
    FERN("ferns"),
    FLOWER("flowers"),
    FROGSPAWN("frogspawn"),
    FUNGUS("fungus"),
    GLASS_PANE("glass_panes", true),
    GLOW_LICHEN("glow_lichens"),
    GRASS("grass"),
    HANGING_ROOTS("hanging_roots"),
    HANGING_SIGN("hanging_signs"),
    LADDER("ladders"),
    LANTERN("lanterns"),
    LEAF_LITTER("leaf_litters"),
    LILY_PAD("lily_pads"),
    MANGROVE_PROPAGULE("mangrove_propagules"),
    MUSHROOM("mushrooms"),
    NETHER_ROOTS("nether_roots"),
    NETHER_SPROUTS("nether_sprouts"),
    NETHER_WART("nether_warts"),
    NONE(null),
    PRESSURE_PLATE("pressure_plates"),
    RAIL("rails"),
    REDSTONE_WIRE("redstone_wires"),
    REPEATER("repeaters"),
    SAPLING("saplings"),
    SCAFFOLDING("scaffolding"),
    SCULK_VEIN("sculk_veins"),
    SEA_PICKLE("sea_pickles"),
    SIGN("signs"),
    SNOW("snow"),
    SPELEOTHEM("speleothems", true),
    SUGAR_CANE("sugar_canes"),
    SWEET_BERRY_BUSH("sweet_berry_bushes"),
    SWITCH("switches"),
    TORCH("torches"),
    TRIPWIRE_HOOK("tripwire_hooks"),
    TWISTING_VINES("twisting_vines", true),
    VINE("vines"),
    WALL("walls", true),
    WEEPING_VINES("weeping_vines", true);

    private final String configKey;

    private final boolean isConnectable;

    ReactiveMaterial(String configKey) {
        this.configKey = configKey;
        this.isConnectable = false;
    }

    ReactiveMaterial(String configKey, boolean isConnectable) {
        this.configKey = configKey;
        this.isConnectable = isConnectable;
    }

    @Override
    public String getConfigKey() {
        return configKey;
    }

    @Override
    public ReactiveMaterial getNone() {
        return NONE;
    }

    public boolean isConnectable() {
        return isConnectable;
    }
}
