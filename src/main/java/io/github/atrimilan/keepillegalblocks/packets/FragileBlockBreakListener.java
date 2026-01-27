package io.github.atrimilan.keepillegalblocks.packets;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import io.github.atrimilan.keepillegalblocks.utils.DebugUtils;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.WARN;

public class FragileBlockBreakListener implements PacketListener {

    private static final String CANCELLED_MSG = "Canceled packet event: <white>";

    private final Set<Vector3i> fragileBlockVectors;
    private final String worldName;
    private final boolean isDoorInteracted;

    public FragileBlockBreakListener(Set<BlockState> fragileBlockStates) {
        this.fragileBlockVectors = HashSet.newHashSet(fragileBlockStates.size());
        fragileBlockStates.forEach(s -> this.fragileBlockVectors.add(new Vector3i(s.getX(), s.getY(), s.getZ())));

        BlockState firstBlockState = fragileBlockStates.iterator().next();
        this.worldName = firstBlockState.getWorld().getName();
        this.isDoorInteracted = firstBlockState.getBlockData() instanceof Door;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {

        if (event.getPacketType() == PacketType.Play.Server.EFFECT) {
            this.checkAndCancelEffectPacketEvent(event);

        } else if (event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
            this.checkAndCancelMultiBlockChangePacketEvent(event);
        }
    }

    void checkAndCancelEffectPacketEvent(PacketSendEvent event) {
        if (!worldName.equals(((Player) event.getPlayer()).getWorld().getName())) return;

        var packet = new WrapperPlayServerEffect(event);

        if (fragileBlockVectors.contains(packet.getPosition())) {
            event.setCancelled(true);
            DebugUtils.sendChat(() -> CANCELLED_MSG + PacketType.Play.Server.EFFECT.getName(), WARN);
        }
    }

    void checkAndCancelMultiBlockChangePacketEvent(PacketSendEvent event) {
        // Ignore doors, because if the packet event is canceled, they appear only half-open on the client side
        if (isDoorInteracted) return;
        if (!worldName.equals(((Player) event.getPlayer()).getWorld().getName())) return;

        var packet = new WrapperPlayServerMultiBlockChange(event);

        for (WrapperPlayServerMultiBlockChange.EncodedBlock data : packet.getBlocks()) {
            Vector3i vector = new Vector3i(data.getX(), data.getY(), data.getZ());

            if (fragileBlockVectors.contains(vector)) {
                event.setCancelled(true);
                DebugUtils.sendChat(() -> CANCELLED_MSG + PacketType.Play.Server.MULTI_BLOCK_CHANGE.getName(), WARN);
                break;
            }
        }
    }
}
