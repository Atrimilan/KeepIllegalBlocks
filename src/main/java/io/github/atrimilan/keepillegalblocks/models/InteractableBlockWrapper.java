package io.github.atrimilan.keepillegalblocks.models;

import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper for the BlockState of an interactable block, specifying whether it is also reactive or not.
 *
 * @param blockState     The interactable {@link BlockState}
 * @param isAlsoReactive Whether the interactable block is also reactive
 */
public record InteractableBlockWrapper(@NotNull BlockState blockState, boolean isAlsoReactive) {

}
