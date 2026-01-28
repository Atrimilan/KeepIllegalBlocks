package io.github.atrimilan.keepillegalblocks.models;

import org.bukkit.block.BlockState;

import java.util.Set;

public record BfsResult(BlockState interactableBlock, Set<BlockState> fragileBlocks) {
}
