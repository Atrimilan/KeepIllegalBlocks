package io.github.atrimilan.keepillegalblocks.configuration.types;

public enum FragileType implements BlockType {
    AMETHYST_CLUSTER("amethyst-clusters"),
    BANNER("banners"),
    BED("beds"),
    BELL("bells"),
    CACTUS("cactus"),
    CAKE("cakes"),
    CARPET("carpets"),
    CAVE_VINES("cave-vines"),
    CHORUS_PLANT("chorus-plants"),
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
