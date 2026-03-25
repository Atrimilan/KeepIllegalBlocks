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

    private final Map<Material, FragileType> fragileMaterials = new EnumMap<>(Material.class);
    private final Map<Material, ConnectableType> connectableMaterials = new EnumMap<>(Material.class);
    private final Map<Material, InteractableType> interactableMaterials = new EnumMap<>(Material.class);

    public void registerFragile(Material mat, FragileType type) {
        fragileMaterials.put(mat, type);
    }

    public void registerConnectable(Material mat, ConnectableType type) {
        connectableMaterials.put(mat, type);
    }

    public void registerInteractable(Material mat, InteractableType type) {
        interactableMaterials.put(mat, type);
    }

    public void clearAll() {
        fragileMaterials.clear();
        connectableMaterials.clear();
        interactableMaterials.clear();
    }

    public boolean isFragile(Material mat) {
        if (mat == null || fragileMaterials.isEmpty()) return false;
        return fragileMaterials.getOrDefault(mat, FragileType.NONE) != FragileType.NONE;
    }

    public boolean isConnectable(Material mat) {
        if (mat == null || connectableMaterials.isEmpty()) return false;
        return connectableMaterials.getOrDefault(mat, ConnectableType.NONE) != ConnectableType.NONE;
    }

    public InteractableType getInteractableType(Material mat) {
        if (mat == null || interactableMaterials.isEmpty()) return InteractableType.NONE;

        InteractableType interactableType = interactableMaterials.getOrDefault(mat, InteractableType.NONE);

        DebugUtils.sendChat(() -> "Material <white>" + mat + "</white> " +
                                  (interactableType != InteractableType.NONE ? ("is interactable: <white>" + mat) :
                                   "is not interactable"), interactableType != InteractableType.NONE ? OK : ERROR);
        return interactableType;
    }

    public int getFragileCount() {
        return fragileMaterials.size();
    }

    public int getConnectableCount() {
        return connectableMaterials.size();
    }

    public int getInteractableCount() {
        return interactableMaterials.size();
    }
}
