package io.github.atrimilan.keepillegalblocks.services;

import com.github.retrooper.packetevents.event.PacketListenerCommon;
import io.github.atrimilan.keepillegalblocks.BukkitMockFactory;
import io.github.atrimilan.keepillegalblocks.core.MaterialRegistry;
import io.github.atrimilan.keepillegalblocks.core.Settings;
import io.github.atrimilan.keepillegalblocks.core.types.InteractableType;
import io.github.atrimilan.keepillegalblocks.core.types.ReactiveType;
import io.github.atrimilan.keepillegalblocks.listeners.ItemSpawnListener;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import io.github.atrimilan.keepillegalblocks.models.InteractableBlockWrapper;
import io.github.atrimilan.keepillegalblocks.models.ReactiveBlockWrapper;
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
    ArgumentCaptor<Runnable> tick1Captor;

    @Captor
    ArgumentCaptor<Runnable> tick2Captor;

    @Captor
    ArgumentCaptor<Runnable> secondUpdateCaptor;

    @BeforeEach
    void setUp() {
        service = spy(new BlockRestorationService(plugin, materialRegistry, settings));
    }

    private Block mockSourceBlock(Material sourceMaterial, boolean withReactiveRelatives,
                                  boolean withConnectableReactiveRelatives) {
        Block source = BukkitMockFactory.mockBlock(sourceMaterial);

        lenient().when(materialRegistry.getReactiveType(any(Material.class))).thenReturn(ReactiveType.NONE);

        if (withReactiveRelatives) {
            Block north = BukkitMockFactory.mockBlock(Material.STONE_BUTTON);
            Block east = BukkitMockFactory.mockBlock(Material.STONE_BUTTON);
            Block up = BukkitMockFactory.mockBlock(Material.STONE_BUTTON);
            BukkitMockFactory.setBlockRelative(source, BlockFace.NORTH, north);
            BukkitMockFactory.setBlockRelative(source, BlockFace.EAST, east);
            BukkitMockFactory.setBlockRelative(source, BlockFace.UP, up);

            ReactiveType reactiveType = mock(ReactiveType.class);
            lenient().when(reactiveType.isConnectable()).thenReturn(false);
            lenient().when(materialRegistry.getReactiveType(Material.STONE_BUTTON)).thenReturn(reactiveType);
        }
        if (withConnectableReactiveRelatives) {
            Block south = BukkitMockFactory.mockBlock(Material.BRICK_WALL);
            Block down = BukkitMockFactory.mockBlock(Material.BRICK_WALL);
            BukkitMockFactory.setBlockRelative(source, BlockFace.SOUTH, south);
            BukkitMockFactory.setBlockRelative(source, BlockFace.DOWN, down);

            ReactiveType reactiveType = mock(ReactiveType.class);
            lenient().when(reactiveType.isConnectable()).thenReturn(true);
            lenient().when(materialRegistry.getReactiveType(Material.BRICK_WALL)).thenReturn(reactiveType);
        }

        return source;
    }

    // ********** Tests - Should record reactive block states **********

    @Test
    void shouldRecordBlockStates() {
        lenient().when(materialRegistry.isReactive(Material.OAK_DOOR)).thenReturn(true); // Interactable also reactive

        Block source = mockSourceBlock(Material.OAK_DOOR, true, true);
        BfsResult res = service.recordBlockStates(source, 50);

        assertTrue(res.boundingBox().getVolume() >= 1); // 1x1x1 (because recorded blocks positions are 0 in the mock)
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(5, res.reactiveBlocks().size());
        assertTrue(res.interactableBlock().isAlsoReactive()); // Interactable is also reactive
        assertEquals(6, res.getAllReactiveBlocks().size());
        verify(materialRegistry, times(3)).getReactiveType(Material.STONE_BUTTON);
        verify(materialRegistry, times(2)).getReactiveType(Material.BRICK_WALL);
        verify(materialRegistry).isReactive(Material.OAK_DOOR);
    }

    @Test
    void shouldRecordBlockStatesWhenInteractableIsNotReactive() {
        Block source = mockSourceBlock(Material.COMPOSTER, true, false);
        BfsResult res = service.recordBlockStates(source, 50);

        assertTrue(res.boundingBox().getVolume() >= 1); // 1x1x1 (because reactive blocks positions are 0 in the mock)
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(3, res.reactiveBlocks().size());
        assertFalse(res.interactableBlock().isAlsoReactive()); // Interactable is not reactive
        verify(materialRegistry, times(3)).getReactiveType(Material.STONE_BUTTON);
        verify(materialRegistry, never()).getReactiveType(Material.BRICK_WALL);
        verify(materialRegistry).isReactive(Material.COMPOSTER);
    }

    @Test
    void shouldRecordBlockStatesWhenMaxBlocksIsLow() {
        Block source = mockSourceBlock(Material.COMPOSTER, true, true);
        BfsResult res = service.recordBlockStates(source, 2); // Set max blocks to 2

        assertTrue(res.boundingBox().getVolume() >= 1);
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(2, res.reactiveBlocks().size()); // Only 2 blocks can be recorded
        assertFalse(res.interactableBlock().isAlsoReactive());
        // In the BFS method, 1st scanned block is a normal reactive (BlockFace.UP), and 2nd is a connectable reactive (BlockFace.DOWN)
        verify(materialRegistry, times(1)).getReactiveType(Material.STONE_BUTTON);
        verify(materialRegistry, times(1)).getReactiveType(Material.BRICK_WALL);
        verify(materialRegistry).isReactive(Material.COMPOSTER);
    }

    @Test
    void shouldRecordBlockStatesWithNoRelatives() {
        Block source = mockSourceBlock(Material.COMPOSTER, false, false);
        BfsResult res = service.recordBlockStates(source, 50);

        assertTrue(res.boundingBox().getVolume() >= 1);
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(0, res.getAllReactiveBlocks().size());
        assertFalse(res.interactableBlock().isAlsoReactive()); // Interactable is not reactive
        verify(materialRegistry, never()).getReactiveType(Material.STONE_BUTTON);
        verify(materialRegistry, never()).getReactiveType(Material.BRICK_WALL);
        verify(materialRegistry, atLeastOnce()).getReactiveType(Material.AIR);
        verify(materialRegistry).isReactive(Material.COMPOSTER);
    }

    @Test
    void shouldRecordBlockStatesWithNoRelativesButInteractableIsAlsoReactive() {
        lenient().when(materialRegistry.isReactive(Material.OAK_DOOR)).thenReturn(true); // Interactable also reactive

        Block source = mockSourceBlock(Material.OAK_DOOR, false, false);
        BfsResult res = service.recordBlockStates(source, 50);

        assertTrue(res.boundingBox().getVolume() >= 1);
        assertEquals(source.getState(), res.interactableBlock().blockState());
        assertEquals(0, res.reactiveBlocks().size());
        assertTrue(res.interactableBlock().isAlsoReactive()); // Interactable is reactive
        assertEquals(1, res.getAllReactiveBlocks().size());
        verify(materialRegistry, never()).getReactiveType(Material.STONE_BUTTON);
        verify(materialRegistry, never()).getReactiveType(Material.BRICK_WALL);
        verify(materialRegistry, atLeastOnce()).getReactiveType(Material.AIR);
        verify(materialRegistry).isReactive(Material.OAK_DOOR);
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
        verifyNoInteractions(materialRegistry); // No reactive blocks were recorded
    }

    // ********** Tests - Should schedule restoration **********

    static Stream<Arguments> provideRestorationParameters() {
        return Stream.of( // isPacketEventsPresent, currentInteractableMaterial, interactableType
                Arguments.of(true, Material.COMPOSTER, InteractableType.COMPOSTER),
                Arguments.of(false, Material.AIR, InteractableType.STONE_BUTTON), // AIR -> Is also reactive
                Arguments.of(true, Material.AIR, InteractableType.WOODEN_BUTTON), // AIR -> Is also reactive
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
        boolean interactableIsAlsoReactive = Material.AIR.equals(currentInteractableMaterial);
        BlockState interactableState = BukkitMockFactory.mockBlockState(currentInteractableMaterial);
        InteractableBlockWrapper interactableBlock = new InteractableBlockWrapper(interactableState,
                                                                                  interactableIsAlsoReactive);

        // Prepare BFS result
        BlockState updatedState = BukkitMockFactory.mockBlockState(Material.BRICK_WALL);
        BlockState brokenState = BukkitMockFactory.mockBlockState(Material.STONE_BUTTON);
        BlockState unchangedState = BukkitMockFactory.mockBlockState(Material.STONE);

        ReactiveBlockWrapper updatedReactive = new ReactiveBlockWrapper(updatedState, true);
        ReactiveBlockWrapper brokenReactive = new ReactiveBlockWrapper(brokenState, false);
        ReactiveBlockWrapper unchangedReactive = new ReactiveBlockWrapper(unchangedState, false);

        doReturn(true).when(service).wasUpdated(updatedReactive.state());
        lenient().doReturn(true).when(service).wasReplacedByAir(brokenReactive.state());

        BfsResult res = new BfsResult(interactableBlock, Set.of(updatedReactive, brokenReactive, unchangedReactive),
                                      boundingBox);

        Object packetEventsListener = mock(PacketListenerCommon.class);

        try (MockedStatic<PacketEventsAdapter> packetEventsMock = mockStatic(PacketEventsAdapter.class); //
             MockedConstruction<ItemSpawnListener> itemSpawnListenerMock = mockConstruction(ItemSpawnListener.class)) {

            if (isPacketEventsPresent) {
                packetEventsMock //
                        .when(() -> PacketEventsAdapter.registerReactiveBlockUpdateListener(res))
                        .thenReturn(packetEventsListener);
            }

            service.scheduleRestoration(res, interactableType);

            // Capture and execute scheduled tasks
            verify(scheduler, times(1)).runTaskLater(eq(plugin), tick1Captor.capture(), eq(1L));
            tick1Captor.getValue().run();
            verify(scheduler, times(1)).runTaskLater(eq(plugin), tick2Captor.capture(), eq(2L));
            tick2Captor.getValue().run();

            // Get the ItemSpawnListener instance
            assertEquals(1, itemSpawnListenerMock.constructed().size());
            ItemSpawnListener listenerInstance = itemSpawnListenerMock.constructed().getFirst();

            boolean hasSecondUpdate = interactableType.hasSecondUpdate(); // Whether a second restoration must be scheduled
            long delay = interactableType.getDelayBeforeSecondUpdate();

            if (hasSecondUpdate) {
                // Listeners must not be unregistered yet
                verify(listenerInstance, never()).unregister();
                verify(scheduler, times(2)).runTaskLater(eq(plugin), secondUpdateCaptor.capture(), eq(delay));
                secondUpdateCaptor.getAllValues().forEach(Runnable::run);
            }

            // Listeners must now be unregistered once
            verify(listenerInstance, times(1)).unregister();
            if (isPacketEventsPresent) {
                packetEventsMock.verify(() -> PacketEventsAdapter.registerReactiveBlockUpdateListener(res), times(1));
                packetEventsMock.verify(() -> PacketEventsAdapter.unregisterListener(packetEventsListener), times(1));
            } else {
                packetEventsMock.verifyNoInteractions();
            }

            int updateCount = hasSecondUpdate ? 2 : 1;
            verify(updatedReactive.state(), times(updateCount)).update(true, false);
            verify(brokenReactive.state(), times(updateCount)).update(true, false);
            verify(unchangedReactive.state(), never()).update(anyBoolean(), anyBoolean());
            verify(interactableState, interactableIsAlsoReactive ? times(updateCount) : never()).update(true, false);
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
    void shouldNotScheduleRestorationWhenThereAreNoReactiveBlocks() {
        clearInvocations(settings);

        var interactableBlock = new InteractableBlockWrapper(BukkitMockFactory.mockBlockState(Material.CAULDRON),
                                                             false);
        BfsResult res = new BfsResult(interactableBlock, Collections.emptySet(), boundingBox);

        service.scheduleRestoration(res, InteractableType.CAULDRON);

        verifyNoInteractions(scheduler);
        verifyNoInteractions(settings);
    }
}
