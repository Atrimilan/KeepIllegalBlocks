package io.github.atrimilan.keepillegalblocks.core;

import io.github.atrimilan.keepillegalblocks.core.types.ConnectableType;
import io.github.atrimilan.keepillegalblocks.core.types.FragileType;
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
        materialRegistry.registerFragile(Material.RED_BED, FragileType.BED);
        materialRegistry.registerConnectable(Material.ACACIA_FENCE, ConnectableType.FENCE);
        materialRegistry.registerInteractable(Material.STONE_BUTTON, InteractableType.STONE_BUTTON);

        assertEquals(1, materialRegistry.getFragileCount());
        assertEquals(1, materialRegistry.getConnectableCount());
        assertEquals(1, materialRegistry.getInteractableCount());

        assertTrue(materialRegistry.isFragile(Material.RED_BED));
        assertTrue(materialRegistry.isConnectable(Material.ACACIA_FENCE));

        materialRegistry.clearAll();

        assertEquals(0, materialRegistry.getFragileCount());
        assertEquals(0, materialRegistry.getConnectableCount());
        assertEquals(0, materialRegistry.getInteractableCount());

        assertFalse(materialRegistry.isFragile(Material.RED_BED));
        assertFalse(materialRegistry.isConnectable(Material.ACACIA_FENCE));
    }

    @Test
    void shouldBeFragile() {
        materialRegistry.registerFragile(Material.RED_BED, FragileType.BED);
        assertTrue(materialRegistry.isFragile(Material.RED_BED));

        materialRegistry.registerFragile(Material.QUARTZ_BLOCK, FragileType.NONE);
        assertFalse(materialRegistry.isFragile(Material.QUARTZ_BLOCK));

        assertFalse(materialRegistry.isFragile(Material.CRAFTING_TABLE));
    }

    @Test
    void shouldBeConnectable() {
        materialRegistry.registerConnectable(Material.ACACIA_FENCE, ConnectableType.FENCE);
        assertTrue(materialRegistry.isConnectable(Material.ACACIA_FENCE));

        materialRegistry.registerConnectable(Material.QUARTZ_BLOCK, ConnectableType.NONE);
        assertFalse(materialRegistry.isConnectable(Material.QUARTZ_BLOCK));

        assertFalse(materialRegistry.isConnectable(Material.CRAFTING_TABLE));
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
