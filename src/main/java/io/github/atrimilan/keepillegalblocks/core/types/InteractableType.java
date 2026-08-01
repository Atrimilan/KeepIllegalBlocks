package io.github.atrimilan.keepillegalblocks.core.types;

/**
 * An interactable block is one that a player can interact with directly by right-clicking, triggering a physics update
 * on its adjacent blocks. For example, blocks such as doors, levers, candles, etc. are considered interactable, while
 * blocks such as chests, grindstones, enchanting tables, etc. are not.
 * <p>
 * Some interactable blocks schedule a second physics update after a few ticks. Currently, only
 * {@link InteractableType#STONE_BUTTON} and {@link InteractableType#WOODEN_BUTTON} have this behavior.
 *
 * @see ReactiveType
 */
public enum InteractableType implements KibBlockType {
    CAMPFIRE("campfires"),
    CANDLE("candles"),
    CAULDRON("cauldrons"),
    CAVE_VINES("cave-vines"),
    CHISELED_BOOKSHELF("chiseled-bookshelves"),
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

    public boolean hasSecondUpdate() {
        return delayBeforeSecondUpdate > 0;
    }

    public long getDelayBeforeSecondUpdate() {
        return delayBeforeSecondUpdate;
    }
}
