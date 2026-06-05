package io.github.atrimilan.keepillegalblocks.core;

import io.github.atrimilan.keepillegalblocks.core.types.KibGroup;
import io.github.atrimilan.keepillegalblocks.core.types.ReactiveType;
import io.github.atrimilan.keepillegalblocks.models.LoadResult;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Cocoa;
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
        Material cocoa = mock(Material.class);
        BlockData cocoaBD = mock(Cocoa.class);
        when(cocoa.isBlock()).thenReturn(true);
        when(cocoa.ordinal()).thenReturn(1); // Specify ordinal because "classifiedMaterials" is an EnumMap
        when(cocoa.createBlockData()).thenReturn(cocoaBD);
        doReturn(new Material[]{cocoa}).when(registryLoader).getAllMaterials();

        Material cocoaBeans = mock(Material.class);
        when(cocoaBeans.ordinal()).thenReturn(2); // Force different ordinal for placementMaterial classification
        when(cocoaBD.getPlacementMaterial()).thenReturn(cocoaBeans);

        doReturn(cocoaBD).when(registryLoader).getBlockData(any(Material.class));

        // Mock settings for REACTIVE
        when(settings.getBlacklistedMaterialsForGroup(KibGroup.REACTIVE)).thenReturn(Collections.emptySet());
        when(settings.getEnabledCategoriesForGroup(KibGroup.REACTIVE)).thenReturn(Set.of("cocoa"));
        when(materialRegistry.getReactiveCount()).thenReturn(10);

        // Stub loadRegistry for INTERACTABLE
        lenient().doReturn(5).when(registryLoader)
                .loadRegistry(eq(settings), eq(KibGroup.INTERACTABLE), any(), any(), eq(false));
        when(materialRegistry.getInteractableCount()).thenReturn(20);

        // When
        List<LoadResult> results = registryLoader.fillMaterialRegistry(settings);

        // Then
        verify(materialRegistry).clearAll();
        verify(materialRegistry).registerReactive(cocoa, ReactiveType.COCOA);
        verify(materialRegistry).registerReactive(cocoaBeans, ReactiveType.COCOA); // Placement material
        // INTERACTABLE was not registered because it is stubbed

        assertEquals(2, results.size());
        assertEquals(new LoadResult("Reactive", 10, 0), results.get(0));
        assertEquals(new LoadResult("Interactable", 20, 5), results.get(1));
    }
}
