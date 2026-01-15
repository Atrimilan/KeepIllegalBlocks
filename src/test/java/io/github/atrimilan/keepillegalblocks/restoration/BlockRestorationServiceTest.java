package io.github.atrimilan.keepillegalblocks.restoration;

import io.github.atrimilan.keepillegalblocks.configuration.KibConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockRestorationServiceTest {

    @Spy
    @InjectMocks
    private BlockRestorationService service;

    @Mock
    private KibConfig config;

    @Mock
    private JavaPlugin plugin;

    @Mock
    private Server server;

    @Mock
    private BukkitScheduler scheduler;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    /********** Private methods - Mock blocks and states **********/

    private Block mockSourceBlock(boolean withRelatives) {
        Block source = mockBlockWithRelativeAir(Material.OAK_DOOR);

        if (withRelatives) {
            Block north = mockBlockWithRelativeAir(Material.STONE_BUTTON);
            Block east = mockBlockWithRelativeAir(Material.STONE_BUTTON);
            Block up = mockBlockWithRelativeAir(Material.STONE_BUTTON);
            lenient().when(source.getRelative(BlockFace.NORTH)).thenReturn(north);
            lenient().when(source.getRelative(BlockFace.EAST)).thenReturn(east);
            lenient().when(source.getRelative(BlockFace.UP)).thenReturn(up);
            when(config.isFragile(Material.STONE_BUTTON)).thenReturn(true);
        }
        return source;
    }

    private Block mockBlockWithRelativeAir(Material material) {
        Block block = mockBlock(material);
        Block air = mockBlock(Material.AIR);

        lenient().when(block.getRelative(any(BlockFace.class))).thenReturn(air);
        when(config.isFragile(Material.AIR)).thenReturn(false);

        return block;
    }

    private Block mockBlock(Material material) {
        return mockBlockState(material, material).getBlock();
    }

    private BlockState mockBlockState(Material material, Material currentWorldMaterial) {
        Block block = mock(Block.class);
        BlockState state = mock(BlockState.class);

        lenient().when(block.getType()).thenReturn(currentWorldMaterial);
        lenient().when(block.getState()).thenReturn(state);
        lenient().when(block.getLocation()).thenReturn(mock(Location.class));

        lenient().when(state.getBlock()).thenReturn(block);
        lenient().when(state.getType()).thenReturn(material);

        return state;
    }

    /********** Tests - Should record fragile block states **********/

    @Test
    void shouldRecordFragileBlockStates() {
        Block source = mockSourceBlock(true);

        List<BlockState> states = service.recordFragileBlockStates(source);

        assertEquals(4, states.size());
        verify(config, times(3)).isFragile(Material.STONE_BUTTON);
        verify(config, never()).isFragile(Material.OAK_DOOR); // Source should not be checked
    }

    @Test
    void shouldRecordFragileBlockStatesWithNoRelatives() {
        Block source = mockSourceBlock(false);

        List<BlockState> states = service.recordFragileBlockStates(source);

        assertEquals(1, states.size());
        verify(config, times(6)).isFragile(Material.AIR);
        verify(config, never()).isFragile(Material.STONE_BUTTON); // Only air has been added as relative
    }

    @Test
    void shouldRecordFragileBlockStatesWithNoFragileRelatives() {
        Block source = mockSourceBlock(true);

        when(config.isFragile(Material.STONE_BUTTON)).thenReturn(false); // Stub as not fragile

        List<BlockState> states = service.recordFragileBlockStates(source);

        assertEquals(1, states.size());
        verify(config, times(3)).isFragile(Material.STONE_BUTTON);
    }

    @Test
    void shouldNotRecordFragileBlockStatesWhenSourceIsNull() {
        List<BlockState> states = service.recordFragileBlockStates(null);

        assertEquals(0, states.size());
        verifyNoInteractions(config);
    }

    /********** Tests - Should schedule restoration **********/

    @Test
    void shouldScheduleAndExecuteRestoration() {
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        doReturn(false).when(service).willTriggerAdditionalUpdate(any());

        List<BlockState> states = List.of( //
                mockBlockState(Material.OAK_DOOR, Material.OAK_DOOR), // Source: Not broken
                mockBlockState(Material.OAK_DOOR, Material.AIR),      // Block 2: Fragile and broken
                mockBlockState(Material.OAK_DOOR, Material.AIR));     // Block 3: Fragile and broken

        service.scheduleRestoration(states);

        verify(scheduler).runTaskLater(eq(plugin), runnableCaptor.capture(), eq(2L));
        runnableCaptor.getValue().run();

        verify(states.get(0), never()).update(anyBoolean(), anyBoolean());
        verify(states.get(1), times(1)).update(true, false);
        verify(states.get(2), times(1)).update(true, false);
    }

    @Test
    void shouldScheduleAndExecuteRestorationWithButtonAsSourceBlock() {
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        doReturn(true).when(service).willTriggerAdditionalUpdate(any());

        List<BlockState> states = List.of( //
                mockBlockState(Material.STONE_BUTTON, Material.STONE_BUTTON), // Source: Not broken
                mockBlockState(Material.STONE_BUTTON, Material.AIR),          // Block 2: Fragile and broken
                mockBlockState(Material.OAK_BUTTON, Material.STONE_BUTTON),   // Block 3: Fragile and broken
                mockBlockState(Material.BLACK_CARPET, Material.AIR),          // Block 4: Fragile and broken
                mockBlockState(Material.STONE, Material.STONE));              // Block 3: Fragile and broken

        service.scheduleRestoration(states);

        verify(scheduler).runTaskLater(eq(plugin), runnableCaptor.capture(), eq(2L));
        runnableCaptor.getValue().run();

        // The source is a button, and as it will trigger an additional update, it is restored even if not broken
        verify(states.get(0), times(1)).update(true, false);
        verify(states.get(1), times(1)).update(true, false);
        verify(states.get(2), never()).update(anyBoolean(), anyBoolean());
        verify(states.get(3), times(1)).update(true, false);
        verify(states.get(4), never()).update(anyBoolean(), anyBoolean());
    }
}
