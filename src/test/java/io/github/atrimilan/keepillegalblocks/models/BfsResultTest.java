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
    private BlockState fragileBS;

    @Mock
    private BlockState connectableBS;

    @Mock
    private BlockState interactableBS;

    @Mock
    private BoundingBox boundingBox;

    @Test
    void shouldGetWorld() {
        InteractableWrapper wrapper = new InteractableWrapper(interactableBS, false);
        BfsResult result = new BfsResult(wrapper, Collections.emptySet(), Collections.emptySet(), boundingBox);

        assertEquals(interactableBS.getWorld(), result.getWorld());
    }

    @Test
    void shouldHaveBlocksToRestoreWhenFragileBlocksNotIsEmpty() {
        InteractableWrapper wrapper = new InteractableWrapper(interactableBS, false);
        BfsResult result = new BfsResult(wrapper, Set.of(fragileBS), Collections.emptySet(), boundingBox);

        assertTrue(result.hasBlocksToRestore());
    }

    @Test
    void shouldHaveBlocksToRestoreWhenConnectableBlocksIsNotEmpty() {
        InteractableWrapper wrapper = new InteractableWrapper(interactableBS, false);
        BfsResult result = new BfsResult(wrapper, Collections.emptySet(), Set.of(connectableBS), boundingBox);

        assertTrue(result.hasBlocksToRestore());
    }

    @Test
    void shouldHaveBlocksToRestoreWhenInteractableBlockIsAlsoFragile() {
        InteractableWrapper wrapper = new InteractableWrapper(interactableBS, true);
        BfsResult result = new BfsResult(wrapper, Collections.emptySet(), Collections.emptySet(), boundingBox);

        assertTrue(result.hasBlocksToRestore());
    }

    @Test
    void shouldGetAllFragileBlocks() {
        InteractableWrapper wrapper = new InteractableWrapper(interactableBS, true);
        BfsResult result = new BfsResult(wrapper, Set.of(fragileBS), Set.of(connectableBS), boundingBox);

        assertTrue(result.hasBlocksToRestore());
        assertEquals(Set.of(interactableBS, fragileBS), result.getAllFragileBlocks()); // Connectable is not fragile
    }

    @Test
    void shouldNotHaveFragileBlocks() {
        InteractableWrapper wrapper = new InteractableWrapper(interactableBS, false);
        BfsResult result = new BfsResult(wrapper, Collections.emptySet(), Collections.emptySet(), boundingBox);

        assertFalse(result.hasBlocksToRestore());
        assertEquals(Collections.emptySet(), result.getAllFragileBlocks());
    }
}
