package io.github.atrimilan.keepillegalblocks.listeners;

import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import io.github.atrimilan.keepillegalblocks.models.InteractableWrapper;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemSpawnListenerTest {

    @Mock
    private JavaPlugin plugin;

    @Mock
    private Server server;

    @Mock
    private PluginManager pluginManager;

    @Mock
    ItemSpawnEvent event;

    @Mock
    private Item item;

    @Mock
    private Location location;

    private ItemSpawnListener listener;

    @BeforeEach
    void setUp() {
        var boundingBox = new BoundingBox(0, 0, 0, 2, 2, 2);
        var bfsResult = new BfsResult(new InteractableWrapper(mock(BlockState.class), false), Set.of(), boundingBox);

        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);

        listener = new ItemSpawnListener(bfsResult, plugin);
    }

    @Test
    void onItemSpawn_ShouldCancel() {
        when(event.getLocation()).thenReturn(location);
        when(location.toVector()).thenReturn(new Vector(0, 1, 1));
        when(event.getEntity()).thenReturn(item);
        when(item.getThrower()).thenReturn(null);

        listener.onItemSpawn(event);

        verify(event).setCancelled(true);
    }

    @Test
    void onItemSpawn_ShouldNotCancelWhenBlockIsNotInBoundingBox() {
        when(event.getLocation()).thenReturn(location);
        when(location.toVector()).thenReturn(new Vector(2, 1, 1));

        listener.onItemSpawn(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void onItemSpawn_ShouldNotCancelWhenThrowerIsNotNull() {
        when(event.getLocation()).thenReturn(location);
        when(location.toVector()).thenReturn(new Vector(1, 0, 0));
        when(event.getEntity()).thenReturn(item);
        when(item.getThrower()).thenReturn(UUID.randomUUID());

        listener.onItemSpawn(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void shouldUnregisterListener() {
        try (MockedStatic<HandlerList> handlerListMock = mockStatic(HandlerList.class)) {
            listener.unregister();
            handlerListMock.verify(() -> HandlerList.unregisterAll(listener));
        }
    }
}
