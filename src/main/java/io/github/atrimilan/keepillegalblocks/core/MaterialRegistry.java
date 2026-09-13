package io.github.atrimilan.keepillegalblocks.core;

import io.github.atrimilan.keepillegalblocks.core.types.InteractableMaterial;
import io.github.atrimilan.keepillegalblocks.core.types.ReactiveMaterial;
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

    private final Map<Material, ReactiveMaterial> reactiveMaterials = new EnumMap<>(Material.class);
    private final Map<Material, InteractableMaterial> interactableMaterials = new EnumMap<>(Material.class);

    public void registerReactive(Material mat, ReactiveMaterial type) {
        reactiveMaterials.put(mat, type);
    }

    public void registerInteractable(Material mat, InteractableMaterial type) {
        interactableMaterials.put(mat, type);
    }

    public void clearAll() {
        reactiveMaterials.clear();
        interactableMaterials.clear();
    }

    public boolean isReactive(Material mat) {
        return getReactiveMaterial(mat) != ReactiveMaterial.NONE;
    }

    public ReactiveMaterial getReactiveMaterial(Material mat) {
        if (mat == null || reactiveMaterials.isEmpty()) return ReactiveMaterial.NONE;
        return reactiveMaterials.getOrDefault(mat, ReactiveMaterial.NONE);
    }

    public InteractableMaterial getInteractableMaterial(Material mat) {
        if (mat == null || interactableMaterials.isEmpty()) return InteractableMaterial.NONE;

        InteractableMaterial interactableMat = interactableMaterials.getOrDefault(mat, InteractableMaterial.NONE);

        DebugUtils.sendChat(() -> "Material <white>" + mat + "</white> " +
                                  (interactableMat != InteractableMaterial.NONE ? ("is interactable: <white>" + mat) :
                                   "is not interactable"), interactableMat != InteractableMaterial.NONE ? OK : ERROR);
        return interactableMat;
    }

    public int getReactiveCount() {
        return reactiveMaterials.size();
    }

    public int getInteractableCount() {
        return interactableMaterials.size();
    }
}
