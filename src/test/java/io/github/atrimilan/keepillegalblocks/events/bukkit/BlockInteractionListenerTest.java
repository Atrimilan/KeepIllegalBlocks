package io.github.atrimilan.keepillegalblocks.events.bukkit;

import io.github.atrimilan.keepillegalblocks.BukkitMockFactory;
import io.github.atrimilan.keepillegalblocks.core.MaterialRegistry;
import io.github.atrimilan.keepillegalblocks.config.Settings;
import io.github.atrimilan.keepillegalblocks.models.InteractableMaterial;
import io.github.atrimilan.keepillegalblocks.data.BfsResult;
import io.github.atrimilan.keepillegalblocks.data.InteractableBlockWrapper;
import io.github.atrimilan.keepillegalblocks.services.BlockRestorationService;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockInteractionListenerTest {

    @InjectMocks
    private BlockInteractionListener listener;

    @Mock
    private BlockRestorationService service;

    @Mock
    private Settings settings;

    @Mock
    private MaterialRegistry materialRegistry;

    @Mock
    private PlayerInteractEvent playerInteractEvent;

    @Mock
    private Player player;

    @Mock
    private Block clickedBlock;

    // ********** Should restore **********

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void onPlayerInteract_ShouldRestore(boolean isSneaking) {
        Material interactableMat = Material.STONE_BUTTON;
        InteractableMaterial interactableMaterial = InteractableMaterial.STONE_BUTTON;
        BfsResult bfsResult = new BfsResult(
                new InteractableBlockWrapper(BukkitMockFactory.mockBlockState(interactableMat), false), Set.of(),
                mock(BoundingBox.class));

        when(settings.isOnlyEnabledInCreativeMode()).thenReturn(true);
        when(playerInteractEvent.getPlayer()).thenReturn(player);
        when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
        when(playerInteractEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(playerInteractEvent.getHand()).thenReturn(EquipmentSlot.HAND);
        when(player.isSneaking()).thenReturn(isSneaking);
        if (isSneaking) // When player is sneaking, he must not be holding an item
            when(playerInteractEvent.getItem()).thenReturn(null);
        when(playerInteractEvent.getClickedBlock()).thenReturn(clickedBlock);
        when(clickedBlock.getType()).thenReturn(interactableMat);
        when(materialRegistry.getInteractableMaterial(interactableMat)).thenReturn(interactableMaterial);
        when(settings.getMaxBlocks()).thenReturn(50);
        when(service.recordBlockStates(clickedBlock, 50)).thenReturn(bfsResult);

        listener.onPlayerInteract(playerInteractEvent);

        verify(service).recordBlockStates(clickedBlock, 50);
        verify(service).scheduleRestoration(bfsResult, interactableMaterial);
    }

    // ********** Should not restore **********

    @Test
    void onPlayerInteract_ShouldNotRestoreWhenGamemodeIsNotValid() {
        when(settings.isOnlyEnabledInCreativeMode()).thenReturn(true);
        when(playerInteractEvent.getPlayer()).thenReturn(player);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);

        listener.onPlayerInteract(playerInteractEvent);

        verifyNoInteractions(materialRegistry);
        verifyNoInteractions(service);
    }

    @Test
    void onPlayerInteract_ShouldNotRestoreWhenInteractionIsNotRightClickBlock() {
        when(settings.isOnlyEnabledInCreativeMode()).thenReturn(false);
        when(playerInteractEvent.getAction()).thenReturn(Action.LEFT_CLICK_BLOCK);

        listener.onPlayerInteract(playerInteractEvent);

        verifyNoInteractions(materialRegistry);
        verifyNoInteractions(service);
    }

    @Test
    void onPlayerInteract_ShouldNotRestoreWhenPlayerIsUsingWrongEquipmentSlot() {
        when(settings.isOnlyEnabledInCreativeMode()).thenReturn(false);
        when(playerInteractEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(playerInteractEvent.getHand()).thenReturn(EquipmentSlot.OFF_HAND);

        listener.onPlayerInteract(playerInteractEvent);

        verifyNoInteractions(materialRegistry);
        verifyNoInteractions(service);
    }

    @Test
    void onPlayerInteract_ShouldNotRestoreWhenPlayerIsSneakingAndHoldingAnItem() {
        when(settings.isOnlyEnabledInCreativeMode()).thenReturn(false);
        when(playerInteractEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(playerInteractEvent.getHand()).thenReturn(EquipmentSlot.HAND);
        when(playerInteractEvent.getPlayer()).thenReturn(player);
        when(player.isSneaking()).thenReturn(true);
        when(playerInteractEvent.getItem()).thenReturn(mock(ItemStack.class));

        listener.onPlayerInteract(playerInteractEvent);

        verifyNoInteractions(materialRegistry);
        verifyNoInteractions(service);
    }

    @Test
    void onPlayerInteract_ShouldNotRestoreWhenSourceBlockIsNull() {
        when(settings.isOnlyEnabledInCreativeMode()).thenReturn(false);
        when(playerInteractEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(playerInteractEvent.getHand()).thenReturn(EquipmentSlot.HAND);
        when(playerInteractEvent.getPlayer()).thenReturn(player);
        when(player.isSneaking()).thenReturn(false);
        when(playerInteractEvent.getClickedBlock()).thenReturn(null);

        listener.onPlayerInteract(playerInteractEvent);

        verifyNoInteractions(materialRegistry);
        verifyNoInteractions(service);
    }

    @Test
    void onPlayerInteract_ShouldNotRestoreWhenBlockIsNotInteractable() {
        Material clickedBlockMaterial = Material.COBBLESTONE;

        when(settings.isOnlyEnabledInCreativeMode()).thenReturn(false);
        when(playerInteractEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(playerInteractEvent.getHand()).thenReturn(EquipmentSlot.HAND);
        when(playerInteractEvent.getPlayer()).thenReturn(player);
        when(player.isSneaking()).thenReturn(false);
        when(playerInteractEvent.getClickedBlock()).thenReturn(clickedBlock);
        when(clickedBlock.getType()).thenReturn(clickedBlockMaterial);
        when(materialRegistry.getInteractableMaterial(clickedBlockMaterial)).thenReturn(InteractableMaterial.NONE);

        listener.onPlayerInteract(playerInteractEvent);

        verify(materialRegistry).getInteractableMaterial(clickedBlockMaterial);
        verifyNoInteractions(service);
    }
}
