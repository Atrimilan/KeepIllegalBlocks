package io.github.atrimilan.keepillegalblocks.restoration;

import com.github.retrooper.packetevents.event.PacketListenerCommon;
import io.github.atrimilan.keepillegalblocks.BukkitMockFactory;
import io.github.atrimilan.keepillegalblocks.configuration.KibConfig;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import io.github.atrimilan.keepillegalblocks.packets.PacketEventsAdapter;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockRestorationServiceTest {

    private BlockRestorationService service;

    @Mock
    private KibConfig config;

    @Mock
    private JavaPlugin plugin;

    @Mock
    private Server server;

    @Mock
    private BukkitScheduler scheduler;

    @Mock
    private World world;

    @Captor
    ArgumentCaptor<Runnable> itemDeletionTaskCaptor;

    @Captor
    ArgumentCaptor<Runnable> blockRestorationTaskCaptor;

    @BeforeEach
    void setUp() {
        when(config.getPlugin()).thenReturn(plugin);
        service = spy(new BlockRestorationService(config));
    }

    private Block mockSourceBlock(boolean withRelatives) {
        Block source = BukkitMockFactory.mockBlock(Material.OAK_DOOR);

        if (withRelatives) {
            Block north = BukkitMockFactory.mockBlock(Material.STONE_BUTTON);
            Block east = BukkitMockFactory.mockBlock(Material.STONE_BUTTON);
            Block up = BukkitMockFactory.mockBlock(Material.STONE_BUTTON);
            BukkitMockFactory.setBlockRelative(source, BlockFace.NORTH, north);
            BukkitMockFactory.setBlockRelative(source, BlockFace.EAST, east);
            BukkitMockFactory.setBlockRelative(source, BlockFace.UP, up);

            lenient().when(config.isFragile(Material.STONE_BUTTON)).thenReturn(true);
        } else {
            lenient().when(config.isFragile(Material.AIR)).thenReturn(false);
        }
        return source;
    }

    /********** Tests - Should record fragile block states **********/

    @Test
    void shouldRecordFragileBlockStates() {
        Block source = mockSourceBlock(true);

        BfsResult res = service.recordFragileBlockStates(source, 50);
        Set<BlockState> fragileBlocks = res.fragileBlocks();
        BlockState interactableBlock = res.interactableBlock();
        BoundingBox boundingBox = res.boundingBox();

        assertTrue(boundingBox.getVolume() >= 8); // 2x2x2 (because block positions are 0 in the mock + 1 for margin)
        assertEquals(source.getState(), interactableBlock);
        assertEquals(3, fragileBlocks.size());
        verify(config, times(3)).isFragile(Material.STONE_BUTTON);
        verify(config, never()).isFragile(Material.OAK_DOOR); // Source should not be checked
    }

    @Test
    void shouldRecordFragileBlockStatesWhenMaxBlocksIsLow() {
        Block source = mockSourceBlock(true);

        BfsResult res = service.recordFragileBlockStates(source, 2); // Max blocks is 2
        Set<BlockState> fragileBlocks = res.fragileBlocks();
        BlockState interactableBlock = res.interactableBlock();
        BoundingBox boundingBox = res.boundingBox();

        assertTrue(boundingBox.getVolume() >= 8); // 2x2x2 (because block positions are 0 in the mock + 1 for margin)
        assertEquals(source.getState(), interactableBlock);
        assertEquals(2, fragileBlocks.size()); // Only 2 blocks are added as relative
        verify(config, times(2)).isFragile(Material.STONE_BUTTON);
        verify(config, never()).isFragile(Material.OAK_DOOR); // Source should not be checked
    }

    @Test
    void shouldRecordFragileBlockStatesWithNoRelatives() {
        Block source = mockSourceBlock(false);

        BfsResult res = service.recordFragileBlockStates(source, 50);
        Set<BlockState> fragileBlocks = res.fragileBlocks();
        BlockState interactableBlock = res.interactableBlock();
        BoundingBox boundingBox = res.boundingBox();

        assertTrue(boundingBox.getVolume() >= 8); // 2x2x2 (because block positions are 0 in the mock + 1 for margin)
        assertEquals(source.getState(), interactableBlock);
        assertEquals(0, fragileBlocks.size());
        verify(config, times(6)).isFragile(Material.AIR);
        verify(config, never()).isFragile(Material.STONE_BUTTON); // Only air has been added as relative
    }

    @Test
    void shouldRecordFragileBlockStatesWithNoFragileRelatives() {
        Block source = mockSourceBlock(true);

        when(config.isFragile(Material.STONE_BUTTON)).thenReturn(false); // Stub as not fragile

        BfsResult res = service.recordFragileBlockStates(source, 50);
        Set<BlockState> fragileBlocks = res.fragileBlocks();
        BlockState interactableBlock = res.interactableBlock();
        BoundingBox boundingBox = res.boundingBox();

        assertTrue(boundingBox.getVolume() >= 8); // 2x2x2 (because block positions are 0 in the mock + 1 for margin)
        assertEquals(source.getState(), interactableBlock);
        assertEquals(0, fragileBlocks.size());
        verify(config, times(3)).isFragile(Material.STONE_BUTTON);
    }

    @Test
    void shouldNotRecordFragileBlockStatesWhenSourceIsNull() {
        clearInvocations(config); // Clear invocation in service init

        BfsResult res = service.recordFragileBlockStates(null, 50);

        assertNull(res);
        verifyNoInteractions(config);
    }

    @Test
    void shouldNotRecordFragileBlockStatesWhenMaxBlocksIsZero() {
        clearInvocations(config); // Clear invocation in service init
        Block source = mockSourceBlock(true);

        BfsResult res = service.recordFragileBlockStates(source, 0);

        assertNull(res);
        verifyNoInteractions(config); // No fragile blocks were recorded
    }

    /********** Tests - Should schedule restoration **********/

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldScheduleAndExecuteRestorationWithAndWithoutPacketEvents(boolean isPacketEventsPresent) {
        this.executeScheduleRestorationTest(isPacketEventsPresent, false);
    }

    @Test
    void shouldScheduleAndExecuteRestorationWithButtonAsInteractable() {
        this.executeScheduleRestorationTest(true, true);
    }

    private void executeScheduleRestorationTest(boolean isPacketEventsPresent, boolean expectInteractableUpdate) {
        when(config.isPacketEventsPresent()).thenReturn(isPacketEventsPresent);
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);

        // Prepare blocks
        BlockState interactableBlock = BukkitMockFactory.mockBlockState(Material.STONE_BUTTON); // Must not be updated
        BlockState unbrokenFragile = BukkitMockFactory.mockBlockState(Material.OAK_DOOR);       // Must not be updated
        BlockState brokenFragile = BukkitMockFactory.mockBlockState(Material.AIR);              // AIR = Must be updated
        BfsResult res = new BfsResult(interactableBlock, Set.of(unbrokenFragile, brokenFragile),
                                      new BoundingBox(-0.5, -0.5, -0.5, 1.5, 1.5, 1.5));

        // Service behavior
        doReturn(expectInteractableUpdate).when(service).willTriggerAdditionalUpdate(any());

        // Prepare world and entities
        Item recentItem = mock(Item.class);      // Must be removed
        Item oldItem = mock(Item.class);         // Must not be removed
        Entity otherEntity = mock(Entity.class); // Must not be removed

        when(recentItem.getTicksLived()).thenReturn(1);
        when(oldItem.getTicksLived()).thenReturn(10);
        when(interactableBlock.getWorld()).thenReturn(world);

        when(world.getNearbyEntities(eq(res.boundingBox()), any())).thenAnswer(invocation -> {
            Predicate<Entity> predicate = invocation.getArgument(1);
            return Stream.of(recentItem, oldItem, otherEntity).filter(predicate).collect(Collectors.toSet());
        });

        Object packetEventsListener = mock(PacketListenerCommon.class);

        try (MockedStatic<PacketEventsAdapter> packetEventsMock = mockStatic(PacketEventsAdapter.class)) {
            if (isPacketEventsPresent) {
                packetEventsMock //
                        .when(() -> PacketEventsAdapter.registerFragileBlockBreakListener(res))
                        .thenReturn(packetEventsListener);
            }

            // Call main method
            service.scheduleRestoration(res);

            // Capture and execute scheduled tasks
            verify(scheduler).runTask(eq(plugin), itemDeletionTaskCaptor.capture());
            verify(scheduler).runTaskLater(eq(plugin), blockRestorationTaskCaptor.capture(), eq(2L));

            itemDeletionTaskCaptor.getValue().run();
            blockRestorationTaskCaptor.getValue().run();

            // Verify interactions with PacketEventsAdapter
            if (isPacketEventsPresent) {
                packetEventsMock.verify(() -> PacketEventsAdapter.registerFragileBlockBreakListener(res));
                packetEventsMock.verify(() -> PacketEventsAdapter.unregisterListener(packetEventsListener));
            } else {
                packetEventsMock.verifyNoInteractions();
            }
        }

        // Verify deletion of block items
        verify(recentItem).remove();
        verify(oldItem, never()).remove();
        verify(otherEntity, never()).remove();

        // Verify restoration of blocks
        verify(unbrokenFragile, never()).update(anyBoolean(), anyBoolean());
        verify(brokenFragile).update(true, false);
        if (expectInteractableUpdate) {
            verify(interactableBlock).update(true, false);
        } else {
            verify(interactableBlock, never()).update(anyBoolean(), anyBoolean());
        }
    }

    @Test
    void shouldNotScheduleRestorationWhenBfsResultIsNull() {
        clearInvocations(config);

        service.scheduleRestoration(null);

        verifyNoInteractions(scheduler);
        verifyNoInteractions(config);
    }

    @Test
    void shouldNotScheduleRestorationWhenFragileBlockSetIsEmpty() {
        clearInvocations(config);

        BfsResult res = new BfsResult( //
                BukkitMockFactory.mockBlockState(Material.OAK_DOOR), //
                Collections.emptySet(), //
                new BoundingBox(-0.5, -0.5, -0.5, 1.5, 1.5, 1.5));

        service.scheduleRestoration(res);

        verifyNoInteractions(scheduler);
        verifyNoInteractions(config);
    }
}
