package io.github.atrimilan.keepillegalblocks.services;

import com.github.retrooper.packetevents.event.PacketListenerCommon;
import io.github.atrimilan.keepillegalblocks.BukkitMockFactory;
import io.github.atrimilan.keepillegalblocks.core.MaterialRegistry;
import io.github.atrimilan.keepillegalblocks.core.Settings;
import io.github.atrimilan.keepillegalblocks.core.types.InteractableType;
import io.github.atrimilan.keepillegalblocks.listeners.ItemSpawnListener;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import io.github.atrimilan.keepillegalblocks.models.InteractableWrapper;
import io.github.atrimilan.keepillegalblocks.packets.PacketEventsAdapter;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockRestorationServiceTest {

    private BlockRestorationService service;

    @Mock
    private Settings settings;

    @Mock
    private MaterialRegistry materialRegistry;

    @Mock
    private JavaPlugin plugin;

    @Mock
    private Server server;

    @Mock
    private BukkitScheduler scheduler;

    @Mock
    private BoundingBox boundingBox;

    @Captor
    ArgumentCaptor<Runnable> taskCaptor;

    @Captor
    ArgumentCaptor<Runnable> secondTaskCaptor;

    @BeforeEach
    void setUp() {
        service = spy(new BlockRestorationService(plugin, materialRegistry, settings));
    }

    private Block mockSourceBlock(Material sourceMaterial, boolean withFragileRelatives,
                                  boolean withConnectableRelatives) {
        Block source = BukkitMockFactory.mockBlock(sourceMaterial);

        if (withFragileRelatives) {
            Block north = BukkitMockFactory.mockBlock(Material.STONE_BUTTON);
            Block east = BukkitMockFactory.mockBlock(Material.STONE_BUTTON);
            Block up = BukkitMockFactory.mockBlock(Material.STONE_BUTTON);
            BukkitMockFactory.setBlockRelative(source, BlockFace.NORTH, north);
            BukkitMockFactory.setBlockRelative(source, BlockFace.EAST, east);
            BukkitMockFactory.setBlockRelative(source, BlockFace.UP, up);
            lenient().when(materialRegistry.isFragile(Material.STONE_BUTTON)).thenReturn(true);
        }
        if (withConnectableRelatives) {
            Block south = BukkitMockFactory.mockBlock(Material.BRICK_WALL);
            Block down = BukkitMockFactory.mockBlock(Material.BRICK_WALL);
            BukkitMockFactory.setBlockRelative(source, BlockFace.SOUTH, south);
            BukkitMockFactory.setBlockRelative(source, BlockFace.DOWN, down);
            lenient().when(materialRegistry.isConnectable(Material.BRICK_WALL)).thenReturn(true);
        }

        return source;
    }

    // ********** Tests - Should record fragile block states **********

    @Test
    void shouldRecordBlockStates() {
        lenient().when(materialRegistry.isFragile(Material.OAK_DOOR)).thenReturn(true); // Interactable also fragile

        Block source = mockSourceBlock(Material.OAK_DOOR, true, true);
        BfsResult res = service.recordBlockStates(source, 50);

        assertTrue(res.boundingBox().getVolume() >= 1); // 1x1x1 (because recorded blocks positions are 0 in the mock)
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(5, res.fragileBlocks().size() + res.connectableBlocks().size());
        assertTrue(res.interactableBlock().isAlsoFragile()); // Interactable is also fragile
        verify(materialRegistry, times(3)).isFragile(Material.STONE_BUTTON);
        verify(materialRegistry, times(2)).isConnectable(Material.BRICK_WALL);
        verify(materialRegistry).isFragile(Material.OAK_DOOR);
    }

    @Test
    void shouldRecordBlockStatesWhenInteractableIsNotFragile() {
        Block source = mockSourceBlock(Material.COMPOSTER, true, false);
        BfsResult res = service.recordBlockStates(source, 50);

        assertTrue(res.boundingBox().getVolume() >= 1); // 1x1x1 (because fragile blocks positions are 0 in the mock)
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(3, res.fragileBlocks().size() + res.connectableBlocks().size());
        assertFalse(res.interactableBlock().isAlsoFragile()); // Interactable is not fragile
        verify(materialRegistry, times(3)).isFragile(Material.STONE_BUTTON);
        verify(materialRegistry, never()).isConnectable(Material.BRICK_WALL);
        verify(materialRegistry).isFragile(Material.COMPOSTER);
    }

    @Test
    void shouldRecordBlockStatesWhenMaxBlocksIsLow() {
        Block source = mockSourceBlock(Material.COMPOSTER, true, true);
        BfsResult res = service.recordBlockStates(source, 2); // Set max blocks to 2

        assertTrue(res.boundingBox().getVolume() >= 1);
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(2, res.fragileBlocks().size() + res.connectableBlocks().size()); // Only 2 blocks can be recorded
        assertFalse(res.interactableBlock().isAlsoFragile());
        // In the BFS method, 1st scanned block is a fragile (BlockFace.UP), and 2nd is a connectable (BlockFace.DOWN)
        verify(materialRegistry, times(1)).isFragile(Material.STONE_BUTTON);
        verify(materialRegistry, times(1)).isConnectable(Material.BRICK_WALL);
        verify(materialRegistry).isFragile(Material.COMPOSTER);
    }

    @Test
    void shouldRecordBlockStatesWithNoRelatives() {
        Block source = mockSourceBlock(Material.COMPOSTER, false, false);
        BfsResult res = service.recordBlockStates(source, 50);

        assertTrue(res.boundingBox().getVolume() >= 1);
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(0, res.fragileBlocks().size() + res.connectableBlocks().size());
        assertFalse(res.interactableBlock().isAlsoFragile()); // Interactable is not fragile
        verify(materialRegistry, never()).isFragile(Material.STONE_BUTTON);
        verify(materialRegistry, never()).isConnectable(Material.BRICK_WALL);
        verify(materialRegistry, atLeastOnce()).isFragile(Material.AIR);
        verify(materialRegistry, atLeastOnce()).isConnectable(Material.AIR);
        verify(materialRegistry).isFragile(Material.COMPOSTER);
    }

    @Test
    void shouldRecordBlockStatesWithNoRelativesButInteractableIsAlsoFragile() {
        lenient().when(materialRegistry.isFragile(Material.OAK_DOOR)).thenReturn(true); // Interactable also fragile

        Block source = mockSourceBlock(Material.OAK_DOOR, false, false);
        BfsResult res = service.recordBlockStates(source, 50);

        assertTrue(res.boundingBox().getVolume() >= 1);
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(0, res.fragileBlocks().size() + res.connectableBlocks().size());
        assertTrue(res.interactableBlock().isAlsoFragile()); // Interactable is fragile
        verify(materialRegistry, never()).isFragile(Material.STONE_BUTTON);
        verify(materialRegistry, never()).isConnectable(Material.BRICK_WALL);
        verify(materialRegistry, atLeastOnce()).isFragile(Material.AIR);
        verify(materialRegistry, atLeastOnce()).isConnectable(Material.AIR);
        verify(materialRegistry).isFragile(Material.OAK_DOOR);
    }

    @Test
    void shouldNotRecordBlockStatesWhenSourceIsNull() {
        clearInvocations(materialRegistry); // Clear invocation in service init

        BfsResult res = service.recordBlockStates(null, 50);

        assertNull(res);
        verifyNoInteractions(materialRegistry);
    }

    @Test
    void shouldNotRecordBlockStatesWhenMaxBlocksIsZero() {
        clearInvocations(materialRegistry); // Clear invocation in service init
        Block source = mockSourceBlock(Material.STONE_BUTTON, true, true);

        BfsResult res = service.recordBlockStates(source, 0);

        assertNull(res);
        verifyNoInteractions(materialRegistry); // No fragile blocks were recorded
    }

    // ********** Tests - Should schedule restoration **********

    static Stream<Arguments> provideRestorationParameters() {
        return Stream.of( // isPacketEventsPresent, currentInteractableMaterial, interactableType
                Arguments.of(true, Material.COMPOSTER, InteractableType.COMPOSTER),
                Arguments.of(false, Material.AIR, InteractableType.STONE_BUTTON), // AIR -> Is also fragile
                Arguments.of(true, Material.AIR, InteractableType.WOODEN_BUTTON), // AIR -> Is also fragile
                Arguments.of(false, Material.COMPOSTER, InteractableType.COMPOSTER));
    }

    @ParameterizedTest
    @MethodSource("provideRestorationParameters")
    void shouldScheduleRestorationTest(boolean isPacketEventsPresent, Material currentInteractableMaterial,
                                       InteractableType interactableType) {
        when(settings.isPacketEventsEnabled()).thenReturn(isPacketEventsPresent);
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);

        // Prepare interactable
        boolean interactableIsAlsoFragile = Material.AIR.equals(currentInteractableMaterial);
        BlockState interactableState = BukkitMockFactory.mockBlockState(currentInteractableMaterial);
        InteractableWrapper interactableBlock = new InteractableWrapper(interactableState, interactableIsAlsoFragile);

        // Prepare BFS result
        BlockState unbrokenFragile = BukkitMockFactory.mockBlockState(Material.OAK_DOOR);
        BlockState brokenFragile = BukkitMockFactory.mockBlockState(Material.AIR);
        BlockState changedConnectable = BukkitMockFactory.mockBlockState(Material.BRICK_WALL);
        BlockState unchangedConnectable = BukkitMockFactory.mockBlockState(Material.GLASS_PANE);
        BfsResult res = new BfsResult(interactableBlock, Set.of(unbrokenFragile, brokenFragile),
                                      Set.of(changedConnectable, unchangedConnectable), boundingBox);

        // Make sure the current BlockData of the connectable is different from the recorded one
        when(changedConnectable.getBlockData()).thenReturn(mock(BlockData.class));
        when(changedConnectable.getBlock().getBlockData()).thenReturn(mock(BlockData.class));

        Object packetEventsListener = mock(PacketListenerCommon.class);

        try (MockedStatic<PacketEventsAdapter> packetEventsMock = mockStatic(PacketEventsAdapter.class); //
             MockedConstruction<ItemSpawnListener> itemSpawnListenerMock = mockConstruction(ItemSpawnListener.class)) {

            if (isPacketEventsPresent) {
                packetEventsMock //
                        .when(() -> PacketEventsAdapter.registerFragileBlockBreakListener(res))
                        .thenReturn(packetEventsListener);
            }

            // Call main method
            service.scheduleRestoration(res, interactableType);

            // Capture and execute scheduled tasks
            verify(scheduler, times(1)).runTaskLater(eq(plugin), taskCaptor.capture(), eq(2L));
            taskCaptor.getValue().run();

            // Get the ItemSpawnListener instance
            assertEquals(1, itemSpawnListenerMock.constructed().size());
            ItemSpawnListener listenerInstance = itemSpawnListenerMock.constructed().getFirst();

            long delay = interactableType.getDelayBeforeSecondUpdate();
            boolean hasSecondUpdate = delay > 0; // A second restoration must be scheduled

            if (hasSecondUpdate) {
                // Listeners must not be unregistered yet
                verify(listenerInstance, never()).unregister();
                if (isPacketEventsPresent) {
                    packetEventsMock.verify(() -> PacketEventsAdapter.unregisterListener(any()), never());
                }

                // Capture and execute the second scheduled tasks
                verify(scheduler, times(1)).runTaskLater(eq(plugin), secondTaskCaptor.capture(), eq(delay));
                secondTaskCaptor.getValue().run();
            }

            // Listeners must now be unregistered once
            verify(listenerInstance, times(1)).unregister();
            if (isPacketEventsPresent) {
                packetEventsMock.verify(() -> PacketEventsAdapter.registerFragileBlockBreakListener(res), times(1));
                packetEventsMock.verify(() -> PacketEventsAdapter.unregisterListener(packetEventsListener), times(1));
            } else {
                packetEventsMock.verifyNoInteractions();
            }

            int updateCount = hasSecondUpdate ? 2 : 1;
            verify(brokenFragile, times(updateCount)).update(true, false);
            verify(unbrokenFragile, never()).update(anyBoolean(), anyBoolean());
            verify(changedConnectable, times(updateCount)).update(true, false);
            verify(unchangedConnectable, never()).update(anyBoolean(), anyBoolean());
            verify(interactableState, interactableIsAlsoFragile ? times(updateCount) : never()).update(true, false);
        }
    }

    @Test
    void shouldNotScheduleRestorationWhenBfsResultIsNull() {
        clearInvocations(settings);

        service.scheduleRestoration(null, InteractableType.CAULDRON);

        verifyNoInteractions(scheduler);
        verifyNoInteractions(settings);
    }

    @Test
    void shouldNotScheduleRestorationWhenThereAreNoFragileNorConnectableBlocks() {
        clearInvocations(settings);

        var interactableBlock = new InteractableWrapper(BukkitMockFactory.mockBlockState(Material.CAULDRON), false);
        BfsResult res = new BfsResult(interactableBlock, Collections.emptySet(), Collections.emptySet(), boundingBox);

        service.scheduleRestoration(res, InteractableType.CAULDRON);

        verifyNoInteractions(scheduler);
        verifyNoInteractions(settings);
    }
}
