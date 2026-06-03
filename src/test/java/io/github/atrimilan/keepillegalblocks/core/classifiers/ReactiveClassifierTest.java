package io.github.atrimilan.keepillegalblocks.core.classifiers;

import io.github.atrimilan.keepillegalblocks.core.types.ReactiveType;
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
class ReactiveClassifierTest {

    @Spy
    @InjectMocks
    private ReactiveClassifier classifier;

    // TODO - Restore missing test cases.
    //        This will require reworking the classifiers so that they do not use actual instances of Material, Tag, and MaterialTags
    //        (since these require an instance of the Bukkit plugin, and it is hard to mock without MockBukkit)

    static Stream<Arguments> provideMaterial() {
        return Stream.of( // Non-exhaustive list of materials to test
                Arguments.of(Material.RED_BED, Bed.class, ReactiveType.BED),
                Arguments.of(Material.CAKE, Cake.class, ReactiveType.CAKE),
                Arguments.of(Material.OAK_DOOR, Door.class, ReactiveType.DOOR),
                Arguments.of(Material.STONE_BUTTON, Switch.class, ReactiveType.SWITCH),
//                Arguments.of(Material.BLUE_BANNER, BlockData.class, ReactiveType.BANNER),
//                Arguments.of(Material.GREEN_CARPET, BlockData.class, ReactiveType.CARPET),
//                Arguments.of(Material.MOSS_CARPET, BlockData.class, ReactiveType.CARPET),
//                Arguments.of(Material.PITCHER_CROP, BlockData.class, ReactiveType.CROP),
//                Arguments.of(Material.WHEAT_SEEDS, BlockData.class, ReactiveType.CROP),
//                Arguments.of(Material.ATTACHED_PUMPKIN_STEM, BlockData.class, ReactiveType.CROP),
//                Arguments.of(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, BlockData.class, ReactiveType.PRESSURE_PLATE),
//                Arguments.of(Material.TORCH, BlockData.class, ReactiveType.TORCH),
//                Arguments.of(Material.REDSTONE_WALL_TORCH, BlockData.class, ReactiveType.TORCH),
//                Arguments.of(Material.CACTUS, BlockData.class, ReactiveType.CACTUS),
//                Arguments.of(Material.CRIMSON_FUNGUS, BlockData.class, ReactiveType.FUNGUS),
//                Arguments.of(Material.SUGAR_CANE, BlockData.class, ReactiveType.SUGAR_CANE),

                Arguments.of(Material.CRIMSON_FENCE, Fence.class, ReactiveType.FENCE),
                Arguments.of(Material.IRON_BARS, Fence.class, ReactiveType.FENCE),
//                Arguments.of(Material.OXIDIZED_COPPER_BARS, Fence.class, ReactiveType.FENCE),
                Arguments.of(Material.GLASS_PANE, GlassPane.class, ReactiveType.GLASS_PANE),
                Arguments.of(Material.CYAN_STAINED_GLASS_PANE, GlassPane.class, ReactiveType.GLASS_PANE),
                Arguments.of(Material.BRICK_WALL, Wall.class, ReactiveType.WALL)
        );
    }

    void prepareClassifierStubs(Material mat, ReactiveType expected) {
        lenient().doReturn(expected == ReactiveType.BANNER).when(classifier).isBanner(mat);
        lenient().doReturn(expected == ReactiveType.CARPET).when(classifier).isCarpet(mat);
        lenient().doReturn(expected == ReactiveType.CORAL).when(classifier).isCoral(mat);
        lenient().doReturn(expected == ReactiveType.CROP).when(classifier).isCrops(mat);
        lenient().doReturn(expected == ReactiveType.FLOWER).when(classifier).isFlower(mat);
        lenient().doReturn(expected == ReactiveType.MUSHROOM).when(classifier).isMushroom(mat);
        lenient().doReturn(expected == ReactiveType.PRESSURE_PLATE).when(classifier).isPressurePlate(mat);
        lenient().doReturn(expected == ReactiveType.SAPLING).when(classifier).isSapling(mat);
        lenient().doReturn(expected == ReactiveType.SIGN).when(classifier).isSign(mat);
        lenient().doReturn(expected == ReactiveType.TORCH).when(classifier).isTorch(mat);
    }

    @ParameterizedTest
    @MethodSource("provideMaterial")
    void shouldClassify(Material mat, Class<? extends BlockData> dataClass, ReactiveType expected) {
        BlockData blockData = mock(dataClass);
        Material materialMock = mock(Material.class);

        lenient().doReturn(true).when(materialMock).isBlock();
        doReturn(blockData).when(materialMock).createBlockData();
        lenient().doReturn(mat).when(blockData).getMaterial();

        this.prepareClassifierStubs(mat, expected);

        ReactiveType result = classifier.classify(materialMock);

        assertEquals(expected, result);
    }
}
