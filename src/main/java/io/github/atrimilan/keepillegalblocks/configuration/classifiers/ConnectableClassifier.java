package io.github.atrimilan.keepillegalblocks.configuration.classifiers;

import io.github.atrimilan.keepillegalblocks.configuration.types.ConnectableType;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.block.data.type.Wall;

/**
 * See more details about "connectable" blocks here: {@link ConnectableType}.
 *
 * @see FragileClassifier
 * @see InteractableClassifier
 */
public class ConnectableClassifier extends AbstractClassifier {

    public ConnectableType classify(Material mat) {
        BlockData data = mat.createBlockData();

        return switch (data) {
            case Fence ignored -> ConnectableType.FENCE; // Fences + Iron bars + Copper bars
            case GlassPane ignored -> ConnectableType.GLASS_PANE;
            case Wall ignored -> ConnectableType.WALL;

            default -> ConnectableType.NONE;
        };
    }
}
