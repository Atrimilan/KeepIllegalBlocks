package io.github.atrimilan.keepillegalblocks.configuration.types;

/**
 * A "connectable" is a block that automatically connects to its adjacent blocks, such as a fence or a wall. Their
 * connections can be modified illegally with a Debug Stick, or plugins like Axiom or WorldEdit. A connectable block
 * will retrieve its normal state when a physics update is triggered on one of its adjacent blocks.
 *
 * @see FragileType
 * @see InteractableType
 */
public enum ConnectableType implements KibBlockType {
    FENCE("fences"),
    GLASS_PANE("glass-panes"),
    NONE(null),
    WALL("walls");

    private final String configKey;

    ConnectableType(String configKey) {
        this.configKey = configKey;
    }

    @Override
    public String getConfigKey() {
        return configKey;
    }

    @Override
    public ConnectableType getNone() {
        return NONE;
    }
}
