package io.github.atrimilan.keepillegalblocks.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;

public class PacketEventsAdapter {

    private PacketEventsAdapter() {
    }

    public static Object registerReactiveBlockUpdateListener(BfsResult bfsResult) {
        ReactiveBlockUpdateListener listener = new ReactiveBlockUpdateListener(bfsResult);
        return PacketEvents.getAPI().getEventManager().registerListener(listener, PacketListenerPriority.NORMAL);
    }

    public static void unregisterListener(Object listenerObject) {
        if (listenerObject instanceof PacketListenerCommon listener) {
            PacketEvents.getAPI().getEventManager().unregisterListener(listener);
        }
    }
}
