package io.github.atrimilan.keepillegalblocks.core;

import io.github.atrimilan.keepillegalblocks.core.types.ReactiveType;
import io.github.atrimilan.keepillegalblocks.core.types.InteractableType;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MaterialRegistryTest {

    @InjectMocks
    private MaterialRegistry materialRegistry;

    @Test
    void shouldGetCountsAndClearAll() {
        materialRegistry.registerReactive(Material.RED_BED, ReactiveType.BED);
        materialRegistry.registerInteractable(Material.STONE_BUTTON, InteractableType.STONE_BUTTON);

        assertEquals(1, materialRegistry.getReactiveCount());
        assertEquals(1, materialRegistry.getInteractableCount());

        assertTrue(materialRegistry.isReactive(Material.RED_BED));

        materialRegistry.clearAll();

        assertEquals(0, materialRegistry.getReactiveCount());
        assertEquals(0, materialRegistry.getInteractableCount());

        assertFalse(materialRegistry.isReactive(Material.RED_BED));
    }

    @Test
    void shouldBeReactive() {
        materialRegistry.registerReactive(Material.RED_BED, ReactiveType.BED);
        assertTrue(materialRegistry.isReactive(Material.RED_BED));
        assertEquals(ReactiveType.BED, materialRegistry.getReactiveType(Material.RED_BED));

        materialRegistry.registerReactive(Material.QUARTZ_BLOCK, ReactiveType.NONE);
        assertFalse(materialRegistry.isReactive(Material.QUARTZ_BLOCK));
        assertEquals(ReactiveType.NONE, materialRegistry.getReactiveType(Material.QUARTZ_BLOCK));

        assertFalse(materialRegistry.isReactive(Material.CRAFTING_TABLE));
        assertEquals(ReactiveType.NONE, materialRegistry.getReactiveType(Material.CRAFTING_TABLE));
    }

    @Test
    void shouldGetInteractableType() {
        materialRegistry.registerInteractable(Material.STONE_BUTTON, InteractableType.STONE_BUTTON);
        assertEquals(InteractableType.STONE_BUTTON, materialRegistry.getInteractableType(Material.STONE_BUTTON));

        materialRegistry.registerInteractable(Material.QUARTZ_BLOCK, InteractableType.NONE);
        assertEquals(InteractableType.NONE, materialRegistry.getInteractableType(Material.QUARTZ_BLOCK));

        assertEquals(InteractableType.NONE, materialRegistry.getInteractableType(Material.CRAFTING_TABLE));
    }
}
