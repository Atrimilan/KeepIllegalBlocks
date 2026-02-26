package io.github.atrimilan.keepillegalblocks.models;

import org.bukkit.block.BlockState;
import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BfsResultTest {

    @Mock
    private BlockState interactableBlockState;

    @Mock
    private BlockState fragileBlockState;

    @Mock
    private BoundingBox boundingBox;

    @Test
    void shouldGetWorld() {
        InteractableWrapper wrapper = new InteractableWrapper(interactableBlockState, false);
        BfsResult result = new BfsResult(wrapper, Collections.emptySet(), boundingBox);

        assertEquals(interactableBlockState.getWorld(), result.getWorld());
    }

    @Test
    void shouldHaveFragileBlocksWhenFragileBlocksNotEmpty() {
        InteractableWrapper wrapper = new InteractableWrapper(interactableBlockState, false);
        BfsResult result = new BfsResult(wrapper, Set.of(fragileBlockState), boundingBox);

        assertTrue(result.hasBlocksToRestore());
    }

    @Test
    void shouldHaveFragileBlocksWhenInteractableBlockIsAlsoFragile() {
        InteractableWrapper wrapper = new InteractableWrapper(interactableBlockState, true);
        BfsResult result = new BfsResult(wrapper, Collections.emptySet(), boundingBox);

        assertTrue(result.hasBlocksToRestore());
    }

    @Test
    void shouldGetAllFragileBlocks() {
        InteractableWrapper wrapper = new InteractableWrapper(interactableBlockState, true);
        BfsResult result = new BfsResult(wrapper, Set.of(fragileBlockState), boundingBox);

        assertTrue(result.hasBlocksToRestore());
        assertEquals(Set.of(interactableBlockState, fragileBlockState), result.getAllFragileBlocks());
    }

    @Test
    void shouldNotHaveFragileBlocks() {
        InteractableWrapper wrapper = new InteractableWrapper(interactableBlockState, false);
        BfsResult result = new BfsResult(wrapper, Collections.emptySet(), boundingBox);

        assertFalse(result.hasBlocksToRestore());
        assertEquals(Collections.emptySet(), result.getAllFragileBlocks());
    }
}
