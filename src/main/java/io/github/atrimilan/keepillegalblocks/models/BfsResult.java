package io.github.atrimilan.keepillegalblocks.models;

import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public record BfsResult(
        @NotNull InteractableBlockWrapper interactableBlock,
        @NotNull Set<ReactiveBlockWrapper> reactiveBlocks,
        @NotNull BoundingBox boundingBox) {

    public World getWorld() {
        return interactableBlock.blockState().getWorld();
    }

    public boolean hasBlocksToRestore() {
        return interactableBlock.isAlsoReactive() || !reactiveBlocks.isEmpty();
    }

    public Set<ReactiveBlockWrapper> getAllReactiveBlocks() {
        Set<ReactiveBlockWrapper> all = new HashSet<>(reactiveBlocks);
        if (interactableBlock.isAlsoReactive()) {
            all.add(new ReactiveBlockWrapper(interactableBlock.blockState(), false));
            // Setting isConnectable=true would force its restoration in any blockdata update, even though it is not broken
            // However, the interactable block is expected to update (e.g. a door opens/closes), therefore isConnectable=false
        }
        return all;
    }
}
