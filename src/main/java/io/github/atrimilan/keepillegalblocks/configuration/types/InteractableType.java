package io.github.atrimilan.keepillegalblocks.configuration.types;


/**
 * An “interactable” is a block that a player can interact with directly (with right-clicking), and which physically
 * changes. In addition, only blocks that can trigger an update of “fragile” blocks are considered “interactable”.
 * <p>
 * For example, blocks such as doors, levers, candles, etc. are considered interactable, whereas blocks such as chests,
 * grindstones, enchanting tables, etc. are not.
 * <p>
 * Some interactable blocks will automatically trigger asecond update after a few ticks: currently, only
 * {@link InteractableType#STONE_BUTTON} and {@link InteractableType#WOODEN_BUTTON} can do this.
 *
 * @see FragileType
 */
public enum InteractableType implements BlockType {
    CAMPFIRE("campfires"),
    CANDLE("candles"),
    CAULDRON("cauldrons"),
    CAVE_VINES("cave-vines"),
    COMPARATOR("comparators"),
    COMPOSTER("composters"),
    COPPER_BLOCK("copper-blocks"),
    DAYLIGHT_DETECTOR("daylight-detectors"),
    DOOR("doors"),
    END_PORTAL_FRAME("end-portal-frames"),
    GATE("gates"),
    LECTERN("lecterns"),
    LEVER("levers"),
    NONE(null),
    REPEATER("repeaters"),
    STONE_BUTTON("stone-buttons", 20L), // Triggers a second update after 1 second
    SWEET_BERRY_BUSH("sweet-berry-bushes"),
    TRAP_DOOR("trap-doors"),
    WOODEN_BUTTON("wooden-buttons", 30L); // Triggers a second update after 1.5 seconds

    private final String configKey;

    private final long delayBeforeSecondUpdate;

    InteractableType(String configKey) {
        this.configKey = configKey;
        this.delayBeforeSecondUpdate = 0L;
    }

    InteractableType(String configKey, long delayBeforeSecondUpdate) {
        this.configKey = configKey;
        this.delayBeforeSecondUpdate = delayBeforeSecondUpdate;
    }

    @Override
    public String getConfigKey() {
        return configKey;
    }

    @Override
    public InteractableType getNone() {
        return NONE;
    }

    public long getDelayBeforeSecondUpdate() {
        return delayBeforeSecondUpdate;
    }
}
