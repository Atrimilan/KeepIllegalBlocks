package io.github.atrimilan.keepillegalblocks.core;

import io.github.atrimilan.keepillegalblocks.core.types.ConnectableType;
import io.github.atrimilan.keepillegalblocks.core.types.FragileType;
import io.github.atrimilan.keepillegalblocks.core.types.InteractableType;
import io.github.atrimilan.keepillegalblocks.utils.DebugUtils;
import org.bukkit.Material;

import java.util.EnumMap;
import java.util.Map;

import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.ERROR;
import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.OK;

/**
 * Handle the registry of materials.
 */
public class MaterialRegistry {

    private final Map<Material, FragileType> fragileBlocks = new EnumMap<>(Material.class);
    private final Map<Material, ConnectableType> connectableBlocks = new EnumMap<>(Material.class);
    private final Map<Material, InteractableType> interactableBlocks = new EnumMap<>(Material.class);

    public void registerFragile(Material mat, FragileType type) {
        fragileBlocks.put(mat, type);
    }

    public void registerConnectable(Material mat, ConnectableType type) {
        connectableBlocks.put(mat, type);
    }

    public void registerInteractable(Material mat, InteractableType type) {
        interactableBlocks.put(mat, type);
    }

    public void clearAll() {
        fragileBlocks.clear();
        connectableBlocks.clear();
        interactableBlocks.clear();
    }

    public boolean isFragile(Material mat) {
        if (mat == null || fragileBlocks.isEmpty()) return false;
        return fragileBlocks.getOrDefault(mat, FragileType.NONE) != FragileType.NONE;
    }

    public boolean isConnectable(Material mat) {
        if (mat == null || connectableBlocks.isEmpty()) return false;
        return connectableBlocks.getOrDefault(mat, ConnectableType.NONE) != ConnectableType.NONE;
    }

    public InteractableType getInteractableType(Material mat) {
        if (mat == null || interactableBlocks.isEmpty()) return InteractableType.NONE;

        InteractableType interactableType = interactableBlocks.getOrDefault(mat, InteractableType.NONE);

        DebugUtils.sendChat(() -> "Material <white>" + mat + "</white> " +
                                  (interactableType != InteractableType.NONE ? ("is interactable: <white>" + mat) :
                                   "is not interactable"), interactableType != InteractableType.NONE ? OK : ERROR);
        return interactableType;
    }

    public int getFragileCount() {
        return fragileBlocks.size();
    }

    public int getConnectableCount() {
        return connectableBlocks.size();
    }

    public int getInteractableCount() {
        return interactableBlocks.size();
    }
}
