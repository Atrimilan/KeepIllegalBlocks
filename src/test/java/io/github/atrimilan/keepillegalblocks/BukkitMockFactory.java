package io.github.atrimilan.keepillegalblocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

public class BukkitMockFactory {

    public static Block mockBlock(Material currentMaterial) {
        return mockBlockState(currentMaterial).getBlock();
    }

    public static BlockState mockBlockState(Material currentMaterial) {
        Block block = mock(Block.class);
        Location location = mock(Location.class);
        BlockState state = mock(BlockState.class);

        lenient().when(block.getType()).thenReturn(currentMaterial);
        lenient().when(block.getLocation()).thenReturn(location);
        lenient().when(block.getState()).thenReturn(state);
        lenient().when(state.getBlock()).thenReturn(block);

        // Set AIR as default relative
        Block airBlock = mock(Block.class);
        lenient().when(airBlock.getType()).thenReturn(Material.AIR);
        lenient().when(block.getRelative(any(BlockFace.class))).thenReturn(airBlock);

        return state;
    }

    public static void setBlockRelative(Block source, BlockFace face, Block relative) {
        lenient().when(source.getRelative(face)).thenReturn(relative);
    }

    public static void setCoordinates(BlockState state, int x, int y, int z) {
        lenient().when(state.getX()).thenReturn(x);
        lenient().when(state.getY()).thenReturn(y);
        lenient().when(state.getZ()).thenReturn(z);
    }
}
