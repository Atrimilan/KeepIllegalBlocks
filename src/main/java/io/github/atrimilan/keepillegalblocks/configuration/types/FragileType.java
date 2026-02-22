package io.github.atrimilan.keepillegalblocks.configuration.types;

/**
 * A "fragile" block is an illegally placed block, such as a door on another door, a torch attached to a sign, or
 * anything that the game physic would generally not allow. Fragile blocks can commonly be placed with a Debug Stick, or
 * plugins like Axiom or WorldEdit.
 * <p>
 * All connected Fragile blocks break on the first tick, except for the following ones, which break in a cascade
 * starting on the second tick after interaction/ {@code BAMBOO} (not {@code BAMBOO_SAPLING}), {@code CACTUS},
 * {@code CAVE_VINES} and {@code CAVE_VINES_PLANT}, {@code CHORUS_PLANT} and {@code CHORUS_FLOWER},
 * {@code POINTED_DRIPSTONE}, {@code SCAFFOLDING}, {@code SUGAR_CANE}, {@code TWISTING_VINES} and
 * {@code TWISTING_VINES_PLANT}, {@code WEEPING_VINES} and {@code WEEPING_VINES_PLANT}
 *
 * @see InteractableType
 */
public enum FragileType implements BlockType {
    AMETHYST_CLUSTER("amethyst-clusters"),
    BAMBOO("bamboos"),
    BANNER("banners"),
    BED("beds"),
    BELL("bells"),
    CACTUS("cactus"),
    CAKE("cakes"),
    CARPET("carpets"),
    CAVE_VINES("cave-vines"),
    CHORUS_PLANT("chorus-plants"), // FIXME: CHORUS_FLOWER behavior seems different, restoration only works with button
    COCOA("cocoa"),
    COMPARATOR("comparators"),
    CORAL("corals"),
    CROP("crops"),
    DEAD_BUSH("dead-bushes"),
    DOOR("doors"),
    DRIPLEAF("dripleaves"),
    FERN("ferns"),
    FLOWER("flowers"),
    FROGSPAWN("frogspawn"),
    FUNGUS("fungus"),
    GLOW_LICHEN("glow-lichens"),
    GRASS("grass"),
    HANGING_ROOTS("hanging-roots"),
    HANGING_SIGN("hanging-signs"),
    LADDER("ladders"),
    LANTERN("lanterns"),
    LILY_PAD("lily-pads"),
    MANGROVE_PROPAGULE("mangrove-propagules"),
    MUSHROOM("mushrooms"),
    NETHER_ROOTS("nether-roots"),
    NETHER_SPROUTS("nether-sprouts"),
    NETHER_WART("nether-warts"),
    NONE(null),
    POINTED_DRIPSTONE("pointed-dripstones"),
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
    TWISTING_VINES("twisting-vines"),
    VINE("vines"),
    WEEPING_VINES("weeping-vines");

    private final String configKey;

    FragileType(String configKey) {
        this.configKey = configKey;
    }

    @Override
    public String getConfigKey() {
        return configKey;
    }

    @Override
    public FragileType getNone() {
        return NONE;
    }
}
