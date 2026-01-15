package io.github.atrimilan.keepillegalblocks.configuration.classifiers;

import io.github.atrimilan.keepillegalblocks.configuration.types.InteractableType;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Switch;
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
class InteractableClassifierTest {

    @Spy
    @InjectMocks
    private InteractableClassifier classifier;

    static Stream<Arguments> provideMaterial() {
        return Stream.of( // Non-exhaustive list of materials to test
                Arguments.of(Material.CAMPFIRE, Campfire.class, InteractableType.CAMPFIRE),
                Arguments.of(Material.OAK_DOOR, Door.class, InteractableType.DOOR),
                Arguments.of(Material.LEVER, Switch.class, InteractableType.SWITCH),
                Arguments.of(Material.OAK_FENCE_GATE, Gate.class, InteractableType.GATE),
                Arguments.of(Material.COMPOSTER, BlockData.class, InteractableType.COMPOSTER),
                Arguments.of(Material.WATER_CAULDRON, BlockData.class, InteractableType.CAULDRON),
                Arguments.of(Material.OXIDIZED_CUT_COPPER_SLAB, BlockData.class, InteractableType.COPPER_BLOCK),
                Arguments.of(Material.WAXED_EXPOSED_CUT_COPPER_STAIRS, BlockData.class, InteractableType.COPPER_BLOCK));
    }

    void prepareClassifierStubs(Material mat, InteractableType expected) {
        lenient().doReturn(expected == InteractableType.CAULDRON).when(classifier).isCauldron(mat);
        lenient().doReturn(expected == InteractableType.COPPER_BLOCK).when(classifier).isNonPlainCopperBlock(mat);
    }

    @ParameterizedTest
    @MethodSource("provideMaterial")
    void shouldClassify(Material mat, Class<? extends BlockData> dataClass, InteractableType expected) {
        BlockData blockData = mock(dataClass);
        Material materialMock = mock(Material.class);
        doReturn(blockData).when(materialMock).createBlockData();
        lenient().doReturn(mat).when(blockData).getMaterial();

        this.prepareClassifierStubs(mat, expected);

        InteractableType result = classifier.classify(materialMock);

        assertEquals(expected, result);
    }
}
