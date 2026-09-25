package io.github.atrimilan.keepillegalblocks.core.classifiers;

import io.github.atrimilan.keepillegalblocks.models.ReactiveMaterial;
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
                Arguments.of(Material.RED_BED, Bed.class, ReactiveMaterial.BED),
                Arguments.of(Material.CAKE, Cake.class, ReactiveMaterial.CAKE),
                Arguments.of(Material.OAK_DOOR, Door.class, ReactiveMaterial.DOOR),
                Arguments.of(Material.STONE_BUTTON, Switch.class, ReactiveMaterial.SWITCH),
//                Arguments.of(Material.BLUE_BANNER, BlockData.class, ReactiveMaterial.BANNER),
//                Arguments.of(Material.GREEN_CARPET, BlockData.class, ReactiveMaterial.CARPET),
//                Arguments.of(Material.MOSS_CARPET, BlockData.class, ReactiveMaterial.CARPET),
//                Arguments.of(Material.PITCHER_CROP, BlockData.class, ReactiveMaterial.CROP),
//                Arguments.of(Material.WHEAT_SEEDS, BlockData.class, ReactiveMaterial.CROP),
//                Arguments.of(Material.ATTACHED_PUMPKIN_STEM, BlockData.class, ReactiveMaterial.CROP),
//                Arguments.of(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, BlockData.class, ReactiveMaterial.PRESSURE_PLATE),
//                Arguments.of(Material.TORCH, BlockData.class, ReactiveMaterial.TORCH),
//                Arguments.of(Material.REDSTONE_WALL_TORCH, BlockData.class, ReactiveMaterial.TORCH),
//                Arguments.of(Material.CACTUS, BlockData.class, ReactiveMaterial.CACTUS),
//                Arguments.of(Material.CRIMSON_FUNGUS, BlockData.class, ReactiveMaterial.FUNGUS),
//                Arguments.of(Material.SUGAR_CANE, BlockData.class, ReactiveMaterial.SUGAR_CANE),

                Arguments.of(Material.CRIMSON_FENCE, Fence.class, ReactiveMaterial.FENCE),
                Arguments.of(Material.IRON_BARS, Fence.class, ReactiveMaterial.FENCE),
//                Arguments.of(Material.OXIDIZED_COPPER_BARS, Fence.class, ReactiveMaterial.FENCE),
                Arguments.of(Material.GLASS_PANE, GlassPane.class, ReactiveMaterial.GLASS_PANE),
                Arguments.of(Material.CYAN_STAINED_GLASS_PANE, GlassPane.class, ReactiveMaterial.GLASS_PANE),
                Arguments.of(Material.BRICK_WALL, Wall.class, ReactiveMaterial.WALL)
        );
    }

    void prepareClassifierStubs(Material mat, ReactiveMaterial expected) {
        lenient().doReturn(expected == ReactiveMaterial.BANNER).when(classifier).isBanner(mat);
        lenient().doReturn(expected == ReactiveMaterial.CARPET).when(classifier).isCarpet(mat);
        lenient().doReturn(expected == ReactiveMaterial.CORAL).when(classifier).isCoral(mat);
        lenient().doReturn(expected == ReactiveMaterial.CROP).when(classifier).isCrops(mat);
        lenient().doReturn(expected == ReactiveMaterial.FLOWER).when(classifier).isFlower(mat);
        lenient().doReturn(expected == ReactiveMaterial.MUSHROOM).when(classifier).isMushroom(mat);
        lenient().doReturn(expected == ReactiveMaterial.PRESSURE_PLATE).when(classifier).isPressurePlate(mat);
        lenient().doReturn(expected == ReactiveMaterial.SAPLING).when(classifier).isSapling(mat);
        lenient().doReturn(expected == ReactiveMaterial.SIGN).when(classifier).isSign(mat);
        lenient().doReturn(expected == ReactiveMaterial.TORCH).when(classifier).isTorch(mat);
    }

    @ParameterizedTest
    @MethodSource("provideMaterial")
    void shouldClassify(Material mat, Class<? extends BlockData> dataClass, ReactiveMaterial expected) {
        BlockData blockData = mock(dataClass);
        Material materialMock = mock(Material.class);

        lenient().doReturn(true).when(materialMock).isBlock();
        doReturn(blockData).when(materialMock).createBlockData();
        lenient().doReturn(mat).when(blockData).getMaterial();

        this.prepareClassifierStubs(mat, expected);

        ReactiveMaterial result = classifier.classify(materialMock);

        assertEquals(expected, result);
    }
}
