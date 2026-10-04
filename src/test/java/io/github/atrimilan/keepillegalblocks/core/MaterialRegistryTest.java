package io.github.atrimilan.keepillegalblocks.core;

import io.github.atrimilan.keepillegalblocks.models.ReactiveMaterial;
import io.github.atrimilan.keepillegalblocks.models.InteractableMaterial;
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
        materialRegistry.registerReactive(Material.RED_BED, ReactiveMaterial.BED);
        materialRegistry.registerInteractable(Material.STONE_BUTTON, InteractableMaterial.STONE_BUTTON);

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
        materialRegistry.registerReactive(Material.RED_BED, ReactiveMaterial.BED);
        assertTrue(materialRegistry.isReactive(Material.RED_BED));
        assertEquals(ReactiveMaterial.BED, materialRegistry.getReactiveMaterial(Material.RED_BED));

        materialRegistry.registerReactive(Material.QUARTZ_BLOCK, ReactiveMaterial.NONE);
        assertFalse(materialRegistry.isReactive(Material.QUARTZ_BLOCK));
        assertEquals(ReactiveMaterial.NONE, materialRegistry.getReactiveMaterial(Material.QUARTZ_BLOCK));

        assertFalse(materialRegistry.isReactive(Material.CRAFTING_TABLE));
        assertEquals(ReactiveMaterial.NONE, materialRegistry.getReactiveMaterial(Material.CRAFTING_TABLE));
    }

    @Test
    void shouldGetInteractableMaterial() {
        materialRegistry.registerInteractable(Material.STONE_BUTTON, InteractableMaterial.STONE_BUTTON);
        assertEquals(InteractableMaterial.STONE_BUTTON, materialRegistry.getInteractableMaterial(Material.STONE_BUTTON));

        materialRegistry.registerInteractable(Material.QUARTZ_BLOCK, InteractableMaterial.NONE);
        assertEquals(InteractableMaterial.NONE, materialRegistry.getInteractableMaterial(Material.QUARTZ_BLOCK));

        assertEquals(InteractableMaterial.NONE, materialRegistry.getInteractableMaterial(Material.CRAFTING_TABLE));
    }
}
