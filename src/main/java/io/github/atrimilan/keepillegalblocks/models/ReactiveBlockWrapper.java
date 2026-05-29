package io.github.atrimilan.keepillegalblocks.models;

import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper for the BlockState of a reactive block, specifying whether it is connectable or not.
 *
 * @param state         The reactive {@link BlockState}
 * @param isConnectable Whether the reactive block is connectable
 */
public record ReactiveBlockWrapper(@NotNull BlockState state, boolean isConnectable) {

}
