package io.github.atrimilan.keepillegalblocks.core;

import io.github.atrimilan.keepillegalblocks.core.types.ConnectableType;
import io.github.atrimilan.keepillegalblocks.core.types.KibGroup;
import io.github.atrimilan.keepillegalblocks.models.LoadResult;
import org.bukkit.Material;
import org.bukkit.block.data.type.Fence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistryLoaderTest {

    @Spy
    @InjectMocks
    private RegistryLoader registryLoader;

    @Mock
    private MaterialRegistry materialRegistry;

    @Mock
    private Settings settings;

    @Test
    void shouldFillMaterialRegistry() {
        // Given
        Material mockAir = mock(Material.class);
        Material mockFence = mock(Material.class);
        when(mockFence.isBlock()).thenReturn(true);
        when(mockFence.createBlockData()).thenReturn(mock(Fence.class));
        doReturn(new Material[]{mockAir, mockFence}).when(registryLoader).getAllMaterials();

        // Mock settings for CONNECTABLE
        when(settings.getBlacklistedMaterialsForGroup(KibGroup.CONNECTABLE)).thenReturn(Collections.emptySet());
        when(settings.getEnabledCategoriesForGroup(KibGroup.CONNECTABLE)).thenReturn(Set.of("fences"));
        when(materialRegistry.getConnectableCount()).thenReturn(1);

        // Stub loadRegistry for FRAGILE and INTERACTABLE
        lenient().doReturn(5).when(registryLoader).loadRegistry(eq(settings), eq(KibGroup.FRAGILE), any(), any());
        lenient().doReturn(1).when(registryLoader).loadRegistry(eq(settings), eq(KibGroup.INTERACTABLE), any(), any());
        when(materialRegistry.getFragileCount()).thenReturn(10);
        when(materialRegistry.getInteractableCount()).thenReturn(20);

        // When
        List<LoadResult> results = registryLoader.fillMaterialRegistry(settings);

        // Then
        verify(materialRegistry).registerConnectable(mockFence, ConnectableType.FENCE);
        // FRAGILE and INTERACTABLE were not registered because they are stubbed

        assertEquals(3, results.size());
        assertEquals(new LoadResult("Fragile", 10, 5), results.get(0));
        assertEquals(new LoadResult("Connectable", 1, 0), results.get(1));
        assertEquals(new LoadResult("Interactable", 20, 1), results.get(2));
    }
}
