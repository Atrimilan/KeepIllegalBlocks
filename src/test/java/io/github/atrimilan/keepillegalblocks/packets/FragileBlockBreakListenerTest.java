package io.github.atrimilan.keepillegalblocks.packets;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.atrimilan.keepillegalblocks.BukkitMockFactory;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
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
class FragileBlockBreakListenerTest {

    private static final int X_INT = 10, Y_INT = 64, Z_INT = 10;
    private static final double X_DOUBLE = 10.5, Y_DOUBLE = 64.0, Z_DOUBLE = 10.5;

    private FragileBlockBreakListener listener;

    @Mock
    private PacketSendEvent event;

    @Mock
    private Player player;

    @Mock
    private World world;

    private MockedConstruction<WrapperPlayServerEffect> mockedEffect;
    private MockedConstruction<WrapperPlayServerSpawnEntity> mockedSpawn;
    private MockedConstruction<WrapperPlayServerMultiBlockChange> mockedMultiBlock;

    @BeforeEach
    void setUp() {
        // Prepare BFS result
        BlockState interactable = BukkitMockFactory.mockBlockState(Material.OAK_DOOR);
        when(interactable.getWorld()).thenReturn(world);

        BlockState fragile = BukkitMockFactory.mockBlockState(Material.AIR);
        BukkitMockFactory.setCoordinates(fragile, X_INT, Y_INT, Z_INT);

        BoundingBox box = new BoundingBox(X_DOUBLE - 0.5, Y_DOUBLE - 0.5, Z_DOUBLE - 0.5, //
                                          X_DOUBLE + 1.5, Y_DOUBLE + 1.5, Z_DOUBLE + 1.5);

        BfsResult bfsResult = new BfsResult(interactable, Set.of(fragile), box);

        listener = spy(new FragileBlockBreakListener(bfsResult));

        lenient().when(event.getPlayer()).thenReturn(player);
        lenient().when(player.getWorld()).thenReturn(world);
    }

    @AfterEach
    void tearDown() {
        if (mockedEffect != null) mockedEffect.close();
        if (mockedSpawn != null) mockedSpawn.close();
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
            when(mock.getPosition()).thenReturn(new Vector3i(X_INT, Y_INT, Z_INT));
        });

        listener.onPacketSend(event);

        verify(event).setCancelled(true);
    }

    @Test
    void shouldNotCancelEffectWhenVectorIsNotInFragileBlockVectors() {
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.EFFECT);
        mockedEffect = mockConstruction(WrapperPlayServerEffect.class, (mock, context) -> {
            when(mock.getPosition()).thenReturn(new Vector3i(1, 2, 3));
        });

        listener.onPacketSend(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    // ********** SPAWN_ENTITY Packet **********

    @Test
    void shouldCancelItemSpawnInsideBoundingBox() {
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.SPAWN_ENTITY);
        doReturn(true).when(listener).isItemEntity(any());
        mockedSpawn = mockConstruction(WrapperPlayServerSpawnEntity.class, (mock, context) -> {
            when(mock.getPosition()).thenReturn(new Vector3d(X_DOUBLE, Y_DOUBLE, Z_DOUBLE));
        });

        listener.onPacketSend(event);

        verify(event).setCancelled(true);
    }

    @Test
    void shouldNotCancelItemSpawnOutsideBoundingBox() {
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.SPAWN_ENTITY);
        doReturn(true).when(listener).isItemEntity(any());
        mockedSpawn = mockConstruction(WrapperPlayServerSpawnEntity.class, (mock, context) -> {
            when(mock.getPosition()).thenReturn(new Vector3d(1d, 2d, 3d));
        });

        listener.onPacketSend(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void shouldNotCancelSpawnWhenEntityIsNotItem() {
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.SPAWN_ENTITY);
        doReturn(false).when(listener).isItemEntity(any());
        mockedSpawn = mockConstruction(WrapperPlayServerSpawnEntity.class);

        listener.onPacketSend(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    // ********** MULTI_BLOCK_CHANGE Packet **********

    private WrapperPlayServerMultiBlockChange.EncodedBlock mockEncodedBlock(boolean isAir) {
        var block = mock(WrapperPlayServerMultiBlockChange.EncodedBlock.class);
        when(block.getBlockId()).thenReturn(isAir ? 0 : 1);
        return block;
    }

    @Test
    void shouldTweakMultiBlockChangePacket() {
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.MULTI_BLOCK_CHANGE);

        var fragileBlock = mockEncodedBlock(true);
        var safeBlock = mockEncodedBlock(false);

        WrapperPlayServerMultiBlockChange.EncodedBlock[] blocks = {fragileBlock, safeBlock};

        mockedMultiBlock = mockConstruction(WrapperPlayServerMultiBlockChange.class, (mock, context) -> {
            when(mock.getBlocks()).thenReturn(blocks);
        });

        listener.onPacketSend(event);

        var captor = ArgumentCaptor.forClass(WrapperPlayServerMultiBlockChange.EncodedBlock[].class);

        verify(mockedMultiBlock.constructed().getFirst()).setBlocks(captor.capture());
        verify(event).markForReEncode(true);
        verify(event, never()).setCancelled(anyBoolean());

        WrapperPlayServerMultiBlockChange.EncodedBlock[] result = captor.getValue();
        assertEquals(1, result.length);
        assertEquals(safeBlock, result[0]); // The safe block is kept in the packet to be sent
    }

    @Test
    void shouldNotTweakMultiBlockChangeWhenPacketContainsNoAirBlock() {
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.MULTI_BLOCK_CHANGE);

        var fragileBlock = mockEncodedBlock(false); // Was not replaced by AIR
        var safeBlock = mockEncodedBlock(false);

        WrapperPlayServerMultiBlockChange.EncodedBlock[] blocks = {fragileBlock, safeBlock};

        mockedMultiBlock = mockConstruction(WrapperPlayServerMultiBlockChange.class, (mock, context) -> {
            when(mock.getBlocks()).thenReturn(blocks);
        });

        listener.onPacketSend(event);

        verify(mockedMultiBlock.constructed().getFirst(), never()).setBlocks(any());
        verify(event, never()).markForReEncode(anyBoolean());
        verify(event, never()).setCancelled(anyBoolean());
    }
}
