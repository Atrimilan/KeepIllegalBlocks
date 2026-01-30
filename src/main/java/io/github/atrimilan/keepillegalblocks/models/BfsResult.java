package io.github.atrimilan.keepillegalblocks.models;

import org.bukkit.block.BlockState;
import org.bukkit.util.BoundingBox;

import java.util.Set;

public record BfsResult(BlockState interactableBlock, Set<BlockState> fragileBlocks, BoundingBox boundingBox) {
}
