package io.github.atrimilan.keepillegalblocks.models;

import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper for the BlockState of an interactable, specifying whether it is also fragile or not
 */
public record InteractableWrapper(@NotNull BlockState blockState, boolean isAlsoFragile) {

}
