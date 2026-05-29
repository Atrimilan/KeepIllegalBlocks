package io.github.atrimilan.keepillegalblocks.core;

import io.github.atrimilan.keepillegalblocks.core.types.ReactiveType;
import io.github.atrimilan.keepillegalblocks.core.types.InteractableType;
import io.github.atrimilan.keepillegalblocks.utils.DebugUtils;
import net.minecraft.world.level.block.Block;
import org.bukkit.Material;

import java.util.EnumMap;
import java.util.Map;

import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.ERROR;
import static io.github.atrimilan.keepillegalblocks.utils.DebugUtils.MessageType.OK;

/**
 * Handle the registry of materials.
 */
public class MaterialRegistry {

    private final Map<Material, ReactiveType> reactiveMaterials = new EnumMap<>(Material.class);
    private final Map<Material, InteractableType> interactableMaterials = new EnumMap<>(Material.class);

    public void registerReactive(Material mat, ReactiveType type) {
        reactiveMaterials.put(mat, type);
    }

    public void registerInteractable(Material mat, InteractableType type) {
        interactableMaterials.put(mat, type);
    }

    public void clearAll() {
        reactiveMaterials.clear();
        interactableMaterials.clear();
    }

    public boolean isReactive(Material mat) {
        return getReactiveType(mat) != ReactiveType.NONE;
    }

    public ReactiveType getReactiveType(Material mat) {
        if (mat == null || reactiveMaterials.isEmpty()) return ReactiveType.NONE;
        return reactiveMaterials.getOrDefault(mat, ReactiveType.NONE);
    }

    public InteractableType getInteractableType(Material mat) {
        if (mat == null || interactableMaterials.isEmpty()) return InteractableType.NONE;

        InteractableType interactableType = interactableMaterials.getOrDefault(mat, InteractableType.NONE);

        DebugUtils.sendChat(() -> "Material <white>" + mat + "</white> " +
                                  (interactableType != InteractableType.NONE ? ("is interactable: <white>" + mat) :
                                   "is not interactable"), interactableType != InteractableType.NONE ? OK : ERROR);
        return interactableType;
    }

    public int getReactiveCount() {
        return reactiveMaterials.size();
    }

    public int getInteractableCount() {
        return interactableMaterials.size();
    }
}
