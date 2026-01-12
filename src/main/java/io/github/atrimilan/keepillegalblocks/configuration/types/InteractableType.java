package io.github.atrimilan.keepillegalblocks.configuration.types;

public enum InteractableType implements BlockType {
    CAMPFIRE("campfires"),
    CANDLE("candles"),
    CAULDRON("cauldrons"),
    COMPARATOR("comparators"),
    COMPOSTER("composters"),
    COPPER_BLOCK("copper-blocks"),
    DAYLIGHT_DETECTOR("daylight-detectors"),
    DOOR("doors"),
    END_PORTAL_FRAME("end-portal-frames"),
    GATE("gates"),
    LECTERN("lecterns"),
    NONE(null),
    REPEATER("repeaters"),
    SWITCH("switches"),
    TRAP_DOOR("trap-doors");

    private final String configKey;

    InteractableType(String configKey) {
        this.configKey = configKey;
    }

    @Override
    public String getConfigKey() {
        return configKey;
    }

    @Override
    public InteractableType getNone() {
        return NONE;
    }
}
