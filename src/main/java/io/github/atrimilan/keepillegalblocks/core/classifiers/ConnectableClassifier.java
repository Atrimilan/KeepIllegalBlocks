package io.github.atrimilan.keepillegalblocks.core.classifiers;

import io.github.atrimilan.keepillegalblocks.core.types.ConnectableType;
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
public class ConnectableClassifier extends AbstractClassifier<ConnectableType> {

    @Override
    protected ConnectableType classifyBlockData(BlockData blockData) {
        return switch (blockData) {
            case Fence ignored -> ConnectableType.FENCE; // Fences + Iron bars + Copper bars
            case GlassPane ignored -> ConnectableType.GLASS_PANE;
            case Wall ignored -> ConnectableType.WALL;

            default -> ConnectableType.NONE;
        };
    }

    @Override
    protected ConnectableType classifyMaterial(Material material) {
        return switch (material) {
            default -> ConnectableType.NONE;
        };
    }
}
