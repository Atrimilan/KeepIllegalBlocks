package io.github.atrimilan.keepillegalblocks.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import org.bukkit.block.BlockState;

import java.util.Set;

public class PacketEventsAdapter {

    private PacketEventsAdapter() {
    }

    public static Object registerFragileBlockBreakListener(Set<BlockState> fragileBlockStates) {
        FragileBlockBreakListener listener = new FragileBlockBreakListener(fragileBlockStates);
        return PacketEvents.getAPI().getEventManager().registerListener(listener, PacketListenerPriority.NORMAL);
    }

    public static void unregisterListener(Object listenerObject) {
        if (listenerObject instanceof PacketListenerCommon listener) {
            PacketEvents.getAPI().getEventManager().unregisterListener(listener);
        }
    }
}
