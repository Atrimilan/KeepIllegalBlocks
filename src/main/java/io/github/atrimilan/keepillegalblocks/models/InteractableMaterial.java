package io.github.atrimilan.keepillegalblocks.models;

/**
 * An interactable block is one that a player can interact with directly by right-clicking, triggering a physics update
 * on its adjacent blocks. For example, blocks such as doors, levers, candles, etc. are considered interactable, while
 * blocks such as chests, grindstones, enchanting tables, etc. are not.
 * <p>
 * Some interactable blocks schedule a second physics update after a few ticks. Currently, only
 * {@link InteractableMaterial#STONE_BUTTON} and {@link InteractableMaterial#WOODEN_BUTTON} have this behavior.
 *
 * @see ReactiveMaterial
 */
public enum InteractableMaterial implements MaterialType {
    CAMPFIRE("campfires"),
    CANDLE("candles"),
    CAULDRON("cauldrons"),
    CAVE_VINES("cave_vines"),
    CHISELED_BOOKSHELF("chiseled_bookshelves"),
    COMPARATOR("comparators"),
    COMPOSTER("composters"),
    COPPER_BLOCK("copper_blocks"),
    DAYLIGHT_DETECTOR("daylight_detectors"),
    DOOR("doors"),
    END_PORTAL_FRAME("end_portal_frames"),
    GATE("gates"),
    LECTERN("lecterns"),
    LEVER("levers"),
    NONE(null),
    REPEATER("repeaters"),
    STONE_BUTTON("stone_buttons", 20L), // Triggers a second update after 1 second
    SWEET_BERRY_BUSH("sweet_berry_bushes"),
    TRAP_DOOR("trap_doors"),
    WOODEN_BUTTON("wooden_buttons", 30L); // Triggers a second update after 1.5 seconds

    private final String configKey;

    private final long delayBeforeSecondUpdate;

    InteractableMaterial(String configKey) {
        this.configKey = configKey;
        this.delayBeforeSecondUpdate = 0L;
    }

    InteractableMaterial(String configKey, long delayBeforeSecondUpdate) {
        this.configKey = configKey;
        this.delayBeforeSecondUpdate = delayBeforeSecondUpdate;
    }

    @Override
    public String getConfigKey() {
        return configKey;
    }

    @Override
    public InteractableMaterial getNone() {
        return NONE;
    }

    public boolean hasSecondUpdate() {
        return delayBeforeSecondUpdate > 0;
    }

    public long getDelayBeforeSecondUpdate() {
        return delayBeforeSecondUpdate;
    }
}
