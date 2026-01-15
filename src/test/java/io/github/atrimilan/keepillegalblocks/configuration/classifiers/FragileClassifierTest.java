package io.github.atrimilan.keepillegalblocks.configuration.classifiers;

import io.github.atrimilan.keepillegalblocks.configuration.types.FragileType;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.Door;
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
class FragileClassifierTest {

    @Spy
    @InjectMocks
    private FragileClassifier classifier;

    static Stream<Arguments> provideMaterial() {
        return Stream.of( // Non-exhaustive list of materials to test
                Arguments.of(Material.RED_BED, Bed.class, FragileType.BED),
                Arguments.of(Material.CAKE, Cake.class, FragileType.CAKE),
                Arguments.of(Material.OAK_DOOR, Door.class, FragileType.DOOR),
                Arguments.of(Material.STONE_BUTTON, Switch.class, FragileType.SWITCH),
                Arguments.of(Material.BLUE_BANNER, BlockData.class, FragileType.BANNER),
                Arguments.of(Material.GREEN_CARPET, BlockData.class, FragileType.CARPET),
                Arguments.of(Material.MOSS_CARPET, BlockData.class, FragileType.CARPET),
                Arguments.of(Material.PITCHER_CROP, BlockData.class, FragileType.CROP),
                Arguments.of(Material.WHEAT_SEEDS, BlockData.class, FragileType.CROP),
                Arguments.of(Material.ATTACHED_PUMPKIN_STEM, BlockData.class, FragileType.CROP),
                Arguments.of(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, BlockData.class, FragileType.PRESSURE_PLATE),
                Arguments.of(Material.TORCH, BlockData.class, FragileType.TORCH),
                Arguments.of(Material.REDSTONE_WALL_TORCH, BlockData.class, FragileType.TORCH),
                Arguments.of(Material.CACTUS, BlockData.class, FragileType.CACTUS),
                Arguments.of(Material.CRIMSON_FUNGUS, BlockData.class, FragileType.FUNGUS),
                Arguments.of(Material.SUGAR_CANE, BlockData.class, FragileType.SUGAR_CANE));
    }

    void prepareClassifierStubs(Material mat, FragileType expected) {
        lenient().doReturn(expected == FragileType.BANNER).when(classifier).isBanner(mat);
        lenient().doReturn(expected == FragileType.CARPET).when(classifier).isCarpet(mat);
        lenient().doReturn(expected == FragileType.CORAL).when(classifier).isCoral(mat);
        lenient().doReturn(expected == FragileType.CROP).when(classifier).isCrop(mat);
        lenient().doReturn(expected == FragileType.FLOWER).when(classifier).isFlower(mat);
        lenient().doReturn(expected == FragileType.MUSHROOM).when(classifier).isMushroom(mat);
        lenient().doReturn(expected == FragileType.PRESSURE_PLATE).when(classifier).isPressurePlate(mat);
        lenient().doReturn(expected == FragileType.SAPLING).when(classifier).isSapling(mat);
        lenient().doReturn(expected == FragileType.SIGN).when(classifier).isSign(mat);
        lenient().doReturn(expected == FragileType.TORCH).when(classifier).isTorch(mat);
    }

    @ParameterizedTest
    @MethodSource("provideMaterial")
    void shouldClassify(Material mat, Class<? extends BlockData> dataClass, FragileType expected) {
        BlockData blockData = mock(dataClass);
        Material materialMock = mock(Material.class);
        doReturn(blockData).when(materialMock).createBlockData();
        lenient().doReturn(mat).when(blockData).getMaterial();

        this.prepareClassifierStubs(mat, expected);

        FragileType result = classifier.classify(materialMock);

        assertEquals(expected, result);
    }
}
