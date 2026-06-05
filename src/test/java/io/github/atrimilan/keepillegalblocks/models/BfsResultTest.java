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
    private BlockState reactiveBS;

    @Mock
    private BlockState interactableBS;

    @Mock
    private BoundingBox boundingBox;

    @Test
    void shouldGetWorld() {
        InteractableBlockWrapper ibw = new InteractableBlockWrapper(interactableBS, false);

        BfsResult result = new BfsResult(ibw, Collections.emptySet(), boundingBox);

        assertEquals(interactableBS.getWorld(), result.getWorld());
    }

    @Test
    void shouldHaveBlocksToRestoreWhenReactiveBlocksNotIsEmpty() {
        InteractableBlockWrapper ibw = new InteractableBlockWrapper(interactableBS, false);
        ReactiveBlockWrapper rbw = new ReactiveBlockWrapper(reactiveBS, false);

        BfsResult result = new BfsResult(ibw, Set.of(rbw), boundingBox);

        assertTrue(result.hasBlocksToRestore());
    }

    @Test
    void shouldHaveBlocksToRestoreWhenInteractableBlockIsAlsoReactive() {
        InteractableBlockWrapper ibw = new InteractableBlockWrapper(interactableBS, true);

        BfsResult result = new BfsResult(ibw, Collections.emptySet(), boundingBox);

        assertTrue(result.hasBlocksToRestore());
    }

    @Test
    void shouldGetAllReactiveBlocks() {
        InteractableBlockWrapper ibw = new InteractableBlockWrapper(interactableBS, true);
        ReactiveBlockWrapper rbw = new ReactiveBlockWrapper(reactiveBS, false);

        BfsResult result = new BfsResult(ibw, Set.of(rbw), boundingBox);

        assertTrue(result.hasBlocksToRestore());
        assertEquals(Set.of(new ReactiveBlockWrapper(interactableBS, false), rbw), result.getAllReactiveBlocks());
    }

    @Test
    void shouldNotHaveReactiveBlocks() {
        InteractableBlockWrapper ibw = new InteractableBlockWrapper(interactableBS, false);

        BfsResult result = new BfsResult(ibw, Collections.emptySet(), boundingBox);

        assertFalse(result.hasBlocksToRestore());
        assertEquals(Collections.emptySet(), result.getAllReactiveBlocks());
    }
}
