package io.github.atrimilan.lockblockstate.utils;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Cake;

public class BlockUtils {

    private BlockUtils() {
    }

    public static boolean isInteractive(Block block) {
        if (block == null) return false;

        BlockData data = block.getBlockData();
        return data instanceof Openable || //
                data instanceof Powerable || //
                data instanceof Lightable || //
                data instanceof Cake;
    }
}
