package io.github.atrimilan.keepillegalblocks.packets;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import io.github.atrimilan.keepillegalblocks.utils.DebugUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.Arrays;
import java.util.Set;

import static com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.*;
import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.WARN;

/**
 * Listen to packets events sent to players when fragile blocks are broken, and cancel or tweak them in order to improve
 * client-side rendering and performances:
 * <li>Cancel break particles.</li>
 * <li>Cancel break sounds.</li>
 * <li>Cancel item drop from broken blocks.</li>
 * <li>Fake fragile blocks presence until they are restored.</li>
 */
public class FragileBlockBreakListener implements PacketListener {

    private final LongOpenHashSet fragileBlockVectors;
    private final LongOpenHashSet fragileBlockVectorsWithoutDoors;
    private final World world;
    private final BoundingBox boundingBox;

    public FragileBlockBreakListener(BfsResult bfsResult) {
        Set<BlockState> fragileBlockStates = bfsResult.fragileBlocks();
        int size = fragileBlockStates.size();

        this.fragileBlockVectors = new LongOpenHashSet(size);
        this.fragileBlockVectorsWithoutDoors = new LongOpenHashSet(size);
        this.world = bfsResult.interactableBlock().getWorld();
        this.boundingBox = bfsResult.boundingBox();

        for (BlockState s : fragileBlockStates) {
            long vector = packVector(s.getX(), s.getY(), s.getZ());
            this.fragileBlockVectors.add(vector);

            if (!(s.getBlockData() instanceof Door)) {
                this.fragileBlockVectorsWithoutDoors.add(vector);
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (((Player) event.getPlayer()).getWorld() != world) return;

        if (event.getPacketType() == EFFECT) {
            this.cancelEffectPacketEvent(event, new WrapperPlayServerEffect(event));

        } else if (event.getPacketType() == SPAWN_ENTITY) {
            this.cancelSpawnItemPacketEvent(event, new WrapperPlayServerSpawnEntity(event));

        } else if (event.getPacketType() == MULTI_BLOCK_CHANGE) {
            this.tweakMultiBlockChangePacketEvent(event, new WrapperPlayServerMultiBlockChange(event));
        }
    }

    /**
     * Cancel the {@code EFFECT} packet to hide break particles and cancel break sounds of fragile blocks. This makes
     * client-side rendering smoother, but also improves performance, as displaying a large number of particles can be
     * quite slow on some clients.
     *
     * @param event  The packet event
     * @param packet The wrapped packet to cancel
     */
    void cancelEffectPacketEvent(PacketSendEvent event, WrapperPlayServerEffect packet) {
        Vector3i pos = packet.getPosition();
        long vector = packVector(pos.getX(), pos.getY(), pos.getZ());

        if (fragileBlockVectors.contains(vector)) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancel the {@code SPAWN_ENTITY} packet when the entity is of type {@link EntityTypes#ITEM} and is inside the
     * fragile blocks bounding box, in order to hide item drops from these fragile blocks.
     *
     * @param event  The packet event
     * @param packet The wrapped packet to cancel
     */
    void cancelSpawnItemPacketEvent(PacketSendEvent event, WrapperPlayServerSpawnEntity packet) {
        boolean isItem = packet.getEntityType().isInstanceOf(EntityTypes.ITEM);
        Vector3d pos = packet.getPosition();

        if (isItem && boundingBox.contains(pos.getX(), pos.getY(), pos.getZ())) {
            event.setCancelled(true);
        }
    }

    /**
     * Tweak the {@code MULTI_BLOCK_CHANGE} packet to fake fragile blocks presence when they are broken and restored.
     * This hides block flickering, making client-side rendering smoother.
     * <p>
     * This does not include:
     * <li>Doors - As they would appear half-open.</li>
     * <li>The interactable source block - As it would not always be updated correctly for all players.</li>
     *
     * @param event  The packet event
     * @param packet The wrapped packet to tweak
     */
    void tweakMultiBlockChangePacketEvent(PacketSendEvent event, WrapperPlayServerMultiBlockChange packet) {
        var packetBlocks = packet.getBlocks();

        int keptCount = 0; // Number of blocks that must remain in the packet (the source interactable and doors)
        var keptBlocks = new WrapperPlayServerMultiBlockChange.EncodedBlock[packetBlocks.length];

        for (var pb : packetBlocks) {
            if (!fragileBlockVectorsWithoutDoors.contains(packVector(pb.getX(), pb.getY(), pb.getZ()))) {
                keptBlocks[keptCount++] = pb; // Packet should not be canceled for blocks that are not in the list
            }
        }

        // If all blocks must be sent, send the packet as is
        if (keptCount == packetBlocks.length) return;

        // Otherwise, remove all fragile blocks from the packet
        packet.setBlocks(Arrays.copyOf(keptBlocks, keptCount));
        event.markForReEncode(true);
        // Not using `event.setCancelled(true)` because the first blocks is always an interactable, and therefore not
        // in the list of fragile blocks, so there is always at least one block in the packet that needs to be sent.

        DebugUtils.sendChat(() -> "Sending modified packet: <white>" + MULTI_BLOCK_CHANGE.name(), WARN);
    }

    /**
     * Encode a vector (x, y, z) into a 64-bit long, using a bit packing.
     */
    private static long packVector(int x, int y, int z) {
        // X: 0x3FFFFFF masks 26 bits, left shifted by 38 -> Encoded into bits 38 to 63
        // Z: 0x3FFFFFF masks 26 bits, left shifted by 12 -> Encoded into bits 12 to 37
        // Y: 0xFFF masks 12 bits, without left shift     -> Encoded into bits 0 to 11
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }
}
