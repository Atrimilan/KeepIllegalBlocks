package io.github.atrimilan.keepillegalblocks.packets;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import io.github.atrimilan.keepillegalblocks.BukkitMockFactory;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import io.github.atrimilan.keepillegalblocks.models.InteractableBlockWrapper;
import io.github.atrimilan.keepillegalblocks.models.ReactiveBlockWrapper;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactiveBlockUpdateListenerTest {

    private static final int REACTIVE_X = 10, REACTIVE_Y = 64, REACTIVE_Z = 10; // X = 10
    private static final int CONNECTABLE_X = 11, CONNECTABLE_Y = 64, CONNECTABLE_Z = 10; // X = 11
    private static final int NON_REACTIVE_X = 12, NON_REACTIVE_Y = 64, NON_REACTIVE_Z = 10; // X = 12

    private ReactiveBlockUpdateListener listener;

    @Mock
    private PacketSendEvent event;

    @Mock
    private Player player;

    @Mock
    private World world;

    @Mock
    private BoundingBox boundingBox;

    private MockedConstruction<WrapperPlayServerEffect> mockedEffect;
    private MockedConstruction<WrapperPlayServerMultiBlockChange> mockedMultiBlock;

    @BeforeEach
    void setUp() {
        // Prepare BFS result
        InteractableBlockWrapper interactableBlockWrapper = new InteractableBlockWrapper(
                BukkitMockFactory.mockBlockState(Material.OAK_DOOR), false);
        when(interactableBlockWrapper.blockState().getWorld()).thenReturn(world);

        BlockState reactiveState = BukkitMockFactory.mockBlockState(Material.AIR);
        BukkitMockFactory.setCoordinates(reactiveState, REACTIVE_X, REACTIVE_Y, REACTIVE_Z);
        ReactiveBlockWrapper reactiveWrapper = new ReactiveBlockWrapper(reactiveState, false);

        BlockState connectableState = BukkitMockFactory.mockBlockState(Material.BRICK_WALL);
        BukkitMockFactory.setCoordinates(connectableState, CONNECTABLE_X, CONNECTABLE_Y, CONNECTABLE_Z);
        ReactiveBlockWrapper connectableWrapper = new ReactiveBlockWrapper(connectableState, true);

        BfsResult bfsResult = new BfsResult(interactableBlockWrapper, Set.of(reactiveWrapper, connectableWrapper),
                                            boundingBox);

        listener = spy(new ReactiveBlockUpdateListener(bfsResult));

        lenient().when(event.getPlayer()).thenReturn(player);
        lenient().when(player.getWorld()).thenReturn(world);
    }

    @AfterEach
    void tearDown() {
        if (mockedEffect != null) mockedEffect.close();
        if (mockedMultiBlock != null) mockedMultiBlock.close();
    }

    @Test
    void shouldIgnorePacketFromNullPlayer() {
        when(event.getPlayer()).thenReturn(null);

        listener.onPacketSend(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void shouldIgnorePacketFromDifferentWorld() {
        when(player.getWorld()).thenReturn(mock(World.class));

        listener.onPacketSend(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void shouldIgnorePacketOfDifferentType() {
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.CHUNK_DATA);

        listener.onPacketSend(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    // ********** EFFECT Packet **********

    @Test
    void shouldCancelEffectPacket() {
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.EFFECT);
        mockedEffect = mockConstruction(WrapperPlayServerEffect.class, (mock, context) -> {
            when(mock.getPosition()).thenReturn(new Vector3i(REACTIVE_X, REACTIVE_Y, REACTIVE_Z));
        });

        listener.onPacketSend(event);

        verify(event).setCancelled(true);
    }

    @Test
    void shouldNotCancelEffectWhenVectorIsNotInReactiveBlockVectors() {
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.EFFECT);
        mockedEffect = mockConstruction(WrapperPlayServerEffect.class, (mock, context) -> {
            when(mock.getPosition()).thenReturn(new Vector3i(1, 2, 3));
        });

        listener.onPacketSend(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    // ********** MULTI_BLOCK_CHANGE Packet **********

    private WrapperPlayServerMultiBlockChange.EncodedBlock mockEncodedBlock(boolean isAir, int x, int y, int z) {
        var block = mock(WrapperPlayServerMultiBlockChange.EncodedBlock.class);
        when(block.getBlockId()).thenReturn(isAir ? 0 : 1);
        when(block.getX()).thenReturn(x);
        when(block.getY()).thenReturn(y);
        when(block.getZ()).thenReturn(z);
        return block;
    }

    @Test
    void shouldTweakMultiBlockChangePacket() {
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.MULTI_BLOCK_CHANGE);

        // Block position was initialized in @BeforeEach
        var brokenBlock = mockEncodedBlock(true, REACTIVE_X, REACTIVE_Y, REACTIVE_Z); // To remove (AIR = broken)
        var updatedBlock = mockEncodedBlock(false, CONNECTABLE_X, CONNECTABLE_Y, CONNECTABLE_Z); // To remove
        var safeBlock = mockEncodedBlock(false, NON_REACTIVE_X, NON_REACTIVE_Y, NON_REACTIVE_Z); // To keep

        WrapperPlayServerMultiBlockChange.EncodedBlock[] blocks = {brokenBlock, updatedBlock, safeBlock};

        mockedMultiBlock = mockConstruction(WrapperPlayServerMultiBlockChange.class, (mock, context) -> {
            when(mock.getBlocks()).thenReturn(blocks);
        });

        listener.onPacketSend(event);

        var captor = ArgumentCaptor.forClass(WrapperPlayServerMultiBlockChange.EncodedBlock[].class);
        verify(mockedMultiBlock.constructed().getFirst(), times(1)).setBlocks(captor.capture());
        verify(event, times(1)).markForReEncode(true);
        verify(event, never()).setCancelled(anyBoolean());

        var result = captor.getValue();
        assertEquals(1, result.length);
        assertEquals(safeBlock, result[0]); // Only the safe block was NOT removed from the packet
    }

    @Test
    void shouldNotTweakMultiBlockChangeWhenReactiveBlockIsNotBrokenNorUpdated() {
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.MULTI_BLOCK_CHANGE);

        // Reactive is not AIR so it's not broken, and it must be kept in the packet
        var nonBrokenReactiveBlock = mockEncodedBlock(false, REACTIVE_X, REACTIVE_Y, REACTIVE_Z);
        var safeBlock = mockEncodedBlock(false, NON_REACTIVE_X, NON_REACTIVE_Y, NON_REACTIVE_Z);

        WrapperPlayServerMultiBlockChange.EncodedBlock[] blocks = {nonBrokenReactiveBlock, safeBlock};

        mockedMultiBlock = mockConstruction(WrapperPlayServerMultiBlockChange.class, (mock, context) -> {
            when(mock.getBlocks()).thenReturn(blocks);
        });

        listener.onPacketSend(event);

        verify(mockedMultiBlock.constructed().getFirst(), never()).setBlocks(any());
        verify(event, never()).markForReEncode(anyBoolean());
        verify(event, never()).setCancelled(anyBoolean());
    }
}
