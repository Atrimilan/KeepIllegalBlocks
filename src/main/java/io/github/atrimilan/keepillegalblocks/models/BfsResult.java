package io.github.atrimilan.keepillegalblocks.models;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public record BfsResult(
        @NotNull InteractableWrapper interactableBlock,
        @NotNull Set<BlockState> fragileBlocks,
        @NotNull BoundingBox boundingBox) {

    public World getWorld() {
        return interactableBlock.blockState().getWorld();
    }

    public boolean hasFragileBlocks() {
        return interactableBlock.isAlsoFragile() || !fragileBlocks.isEmpty();
    }

    public Set<BlockState> getAllFragileBlocks() {
        Set<BlockState> all = new HashSet<>(fragileBlocks);
        if (interactableBlock.isAlsoFragile()) all.add(interactableBlock.blockState());
        return all;
    }
}
