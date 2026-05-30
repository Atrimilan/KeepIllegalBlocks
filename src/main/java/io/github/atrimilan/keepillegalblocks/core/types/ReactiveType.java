package io.github.atrimilan.keepillegalblocks.core.types;

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
 * {@code POINTED_DRIPSTONE}, {@code SCAFFOLDING}, {@code SUGAR_CANE}, {@code TWISTING_VINES} and
 * {@code TWISTING_VINES_PLANT}, {@code WEEPING_VINES} and {@code WEEPING_VINES_PLANT}.
 * <p>
 * A reactive block is "connectable" when it automatically connects to adjacent blocks, such as fences or walls. These
 * connections can also be modified illegally, and they will return to their normal connection state when a physics
 * update is triggered on an adjacent block.
 *
 * @see InteractableType
 */
public enum ReactiveType implements KibBlockType {
    AMETHYST_CLUSTER("amethyst-clusters"),
    BAMBOO("bamboos"),
    BANNER("banners"),
    BED("beds"),
    BELL("bells"),
    CACTUS("cactus"),
    CAKE("cakes"),
    CARPET("carpets"),
    CAVE_VINES("cave-vines", true),
    CHORUS_PLANT("chorus-plants"), // FIXME: Add support for CHORUS_FLOWER
    COCOA("cocoa"),
    COMPARATOR("comparators"),
    CORAL("corals"),
    CROPS("crops"),
    DEAD_BUSH("dead-bushes"),
    DOOR("doors"),
    DRIPLEAF("dripleaves"),
    FENCE("fences", true),
    FERN("ferns"),
    FLOWER("flowers"),
    FROGSPAWN("frogspawn"),
    FUNGUS("fungus"),
    GLASS_PANE("glass-panes", true),
    GLOW_LICHEN("glow-lichens"),
    GRASS("grass"),
    HANGING_ROOTS("hanging-roots"),
    HANGING_SIGN("hanging-signs"),
    LADDER("ladders"),
    LANTERN("lanterns"),
    LEAF_LITTER("leaf-litters"),
    LILY_PAD("lily-pads"),
    MANGROVE_PROPAGULE("mangrove-propagules"),
    MUSHROOM("mushrooms"),
    NETHER_ROOTS("nether-roots"),
    NETHER_SPROUTS("nether-sprouts"),
    NETHER_WART("nether-warts"),
    NONE(null),
    POINTED_DRIPSTONE("pointed-dripstones", true),
    PRESSURE_PLATE("pressure-plates"),
    RAIL("rails"),
    REDSTONE_WIRE("redstone-wires"),
    REPEATER("repeaters"),
    SAPLING("saplings"),
    SCAFFOLDING("scaffolding"),
    SCULK_VEIN("sculk-veins"),
    SEA_PICKLE("sea-pickles"),
    SIGN("signs"),
    SNOW("snow"),
    SUGAR_CANE("sugar-canes"),
    SWEET_BERRY_BUSH("sweet-berry-bushes"),
    SWITCH("switches"),
    TORCH("torches"),
    TRIPWIRE_HOOK("tripwire-hooks"),
    TWISTING_VINES("twisting-vines", true),
    VINE("vines"),
    WALL("walls", true),
    WEEPING_VINES("weeping-vines", true);

    private final String configKey;

    private final boolean isConnectable;

    ReactiveType(String configKey) {
        this.configKey = configKey;
        this.isConnectable = false;
    }

    ReactiveType(String configKey, boolean isConnectable) {
        this.configKey = configKey;
        this.isConnectable = isConnectable;
    }

    @Override
    public String getConfigKey() {
        return configKey;
    }

    @Override
    public ReactiveType getNone() {
        return NONE;
    }

    public boolean isConnectable() {
        return isConnectable;
    }
}
