package io.github.atrimilan.keepillegalblocks.services;

import com.github.retrooper.packetevents.event.PacketListenerCommon;
import io.github.atrimilan.keepillegalblocks.BukkitMockFactory;
import io.github.atrimilan.keepillegalblocks.configuration.KibConfig;
import io.github.atrimilan.keepillegalblocks.configuration.types.InteractableType;
import io.github.atrimilan.keepillegalblocks.listeners.ItemSpawnListener;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import io.github.atrimilan.keepillegalblocks.models.InteractableWrapper;
import io.github.atrimilan.keepillegalblocks.packets.PacketEventsAdapter;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
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
    private KibConfig config;

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
        when(config.getPlugin()).thenReturn(plugin);
        service = spy(new BlockRestorationService(config));
    }

    private Block mockSourceBlock(Material sourceMaterial, boolean withRelatives) {
        Block source = BukkitMockFactory.mockBlock(sourceMaterial);

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

    // ********** Tests - Should record fragile block states **********

    @Test
    void shouldRecordFragileBlockStates() {
        lenient().when(config.isFragile(Material.OAK_DOOR)).thenReturn(true); // Set interactable as also fragile

        Block source = mockSourceBlock(Material.OAK_DOOR, true);
        BfsResult res = service.recordFragileBlockStates(source, 50);

        assertTrue(res.boundingBox().getVolume() >= 1); // 1x1x1 (because fragile blocks positions are 0 in the mock)
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(3, res.fragileBlocks().size());
        assertTrue(res.interactableBlock().isAlsoFragile()); // Interactable is also fragile
        verify(config, times(3)).isFragile(Material.STONE_BUTTON);
        verify(config).isFragile(Material.OAK_DOOR);
    }

    @Test
    void shouldRecordFragileBlockStatesWhenInteractableIsNotFragile() {
        Block source = mockSourceBlock(Material.COMPOSTER, true);
        BfsResult res = service.recordFragileBlockStates(source, 50);

        assertTrue(res.boundingBox().getVolume() >= 1); // 1x1x1 (because fragile blocks positions are 0 in the mock)
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(3, res.fragileBlocks().size());
        assertFalse(res.interactableBlock().isAlsoFragile()); // Interactable is not fragile
        verify(config, times(3)).isFragile(Material.STONE_BUTTON);
        verify(config).isFragile(Material.COMPOSTER);
    }

    @Test
    void shouldRecordFragileBlockStatesWhenMaxBlocksIsLow() {
        Block source = mockSourceBlock(Material.COMPOSTER, true);
        BfsResult res = service.recordFragileBlockStates(source, 2); // Set max blocks to 2

        assertTrue(res.boundingBox().getVolume() >= 1);
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(2, res.fragileBlocks().size()); // Only 2 blocks can be recorded
        assertFalse(res.interactableBlock().isAlsoFragile());
        verify(config, times(3)).isFragile(Material.STONE_BUTTON);
        verify(config).isFragile(Material.COMPOSTER);
    }

    @Test
    void shouldRecordFragileBlockStatesWithNoRelatives() {
        Block source = mockSourceBlock(Material.COMPOSTER, false);
        BfsResult res = service.recordFragileBlockStates(source, 50);

        assertTrue(res.boundingBox().getVolume() >= 1);
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(0, res.fragileBlocks().size()); // No relatives
        assertFalse(res.interactableBlock().isAlsoFragile());
        verify(config, times(6)).isFragile(Material.AIR);
        verify(config).isFragile(Material.COMPOSTER);
    }

    @Test
    void shouldRecordFragileBlockStatesWithNoFragileRelatives2() {
        Block source = mockSourceBlock(Material.COMPOSTER, true);

        when(config.isFragile(Material.STONE_BUTTON)).thenReturn(false); // Stub as not fragile

        BfsResult res = service.recordFragileBlockStates(source, 50);

        assertTrue(res.boundingBox().getVolume() >= 1);
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(0, res.fragileBlocks().size()); // No relatives
        assertFalse(res.interactableBlock().isAlsoFragile());
        verify(config, times(3)).isFragile(Material.STONE_BUTTON);
        verify(config).isFragile(Material.COMPOSTER);
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
        Block source = mockSourceBlock(Material.STONE_BUTTON, true);

        BfsResult res = service.recordFragileBlockStates(source, 0);

        assertNull(res);
        verifyNoInteractions(config); // No fragile blocks were recorded
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
        when(config.isPacketEventsPresent()).thenReturn(isPacketEventsPresent);
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);

        // Prepare interactable
        boolean interactableIsAlsoFragile = Material.AIR.equals(currentInteractableMaterial);
        BlockState interactableState = BukkitMockFactory.mockBlockState(currentInteractableMaterial);
        InteractableWrapper interactableBlock = new InteractableWrapper(interactableState, interactableIsAlsoFragile);

        // Prepare BFS result
        BlockState unbrokenFragile = BukkitMockFactory.mockBlockState(Material.OAK_DOOR);
        BlockState brokenFragile = BukkitMockFactory.mockBlockState(Material.AIR);
        BfsResult res = new BfsResult(interactableBlock, Set.of(unbrokenFragile, brokenFragile), boundingBox);

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
            verify(interactableState, interactableIsAlsoFragile ? times(updateCount) : never()).update(true, false);
        }
    }

    @Test
    void shouldNotScheduleRestorationWhenBfsResultIsNull() {
        clearInvocations(config);

        service.scheduleRestoration(null, InteractableType.CAULDRON);

        verifyNoInteractions(scheduler);
        verifyNoInteractions(config);
    }

    @Test
    void shouldNotScheduleRestorationWhenFragileBlockSetIsEmpty() {
        clearInvocations(config);

        var interactableBlock = new InteractableWrapper(BukkitMockFactory.mockBlockState(Material.CAULDRON), false);
        BfsResult res = new BfsResult(interactableBlock, Collections.emptySet(), boundingBox);

        service.scheduleRestoration(res, InteractableType.CAULDRON);

        verifyNoInteractions(scheduler);
        verifyNoInteractions(config);
    }
}
