package io.github.atrimilan.keepillegalblocks.core.classifiers;

import io.github.atrimilan.keepillegalblocks.core.types.ConnectableType;
import io.github.atrimilan.keepillegalblocks.core.types.InteractableType;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectableClassifierTest {

    @Spy
    @InjectMocks
    private ConnectableClassifier classifier;

    static Stream<Arguments> provideMaterial() {
        return Stream.of( // Non-exhaustive list of materials to test
                Arguments.of(Material.CRIMSON_FENCE, Fence.class, ConnectableType.FENCE),
                Arguments.of(Material.IRON_BARS, Fence.class, ConnectableType.FENCE),
                Arguments.of(Material.OXIDIZED_COPPER_BARS, Fence.class, ConnectableType.FENCE),
                Arguments.of(Material.GLASS_PANE, GlassPane.class, ConnectableType.GLASS_PANE),
                Arguments.of(Material.CYAN_STAINED_GLASS_PANE, GlassPane.class, ConnectableType.GLASS_PANE),
                Arguments.of(Material.BRICK_WALL, Wall.class, ConnectableType.WALL));
    }

    @ParameterizedTest
    @MethodSource("provideMaterial")
    void shouldClassify(Material mat, Class<? extends BlockData> dataClass, ConnectableType expected) {
        BlockData blockData = mock(dataClass);
        Material materialMock = mock(Material.class);
        doReturn(blockData).when(materialMock).createBlockData();
        lenient().doReturn(mat).when(blockData).getMaterial();

        ConnectableType result = classifier.classify(materialMock);

        assertEquals(expected, result);
    }
}
