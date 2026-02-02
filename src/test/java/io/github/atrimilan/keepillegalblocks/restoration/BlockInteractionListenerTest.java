package io.github.atrimilan.keepillegalblocks.restoration;

import io.github.atrimilan.keepillegalblocks.configuration.KibConfig;
import io.github.atrimilan.keepillegalblocks.models.BfsResult;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockInteractionListenerTest {

    @InjectMocks
    private BlockInteractionListener listener;

    @Mock
    private BlockRestorationService service;

    @Mock
    private KibConfig config;

    @Mock
    private PlayerInteractEvent playerInteractEvent;

    @Mock
    private Player player;

    @Mock
    private Block clickedBlock;

    @Mock
    private BfsResult bfsResult;

    /********** Should restore **********/

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void onPlayerInteract_ShouldRestore(boolean isSneaking) {
        when(config.isOnlyEnabledInCreativeMode()).thenReturn(true);
        when(playerInteractEvent.getPlayer()).thenReturn(player);
        when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
        when(playerInteractEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(playerInteractEvent.getHand()).thenReturn(EquipmentSlot.HAND);
        when(player.isSneaking()).thenReturn(isSneaking);
        if (isSneaking) // When player is sneaking, he must not be holding an item
            when(playerInteractEvent.getItem()).thenReturn(null);
        when(playerInteractEvent.getClickedBlock()).thenReturn(clickedBlock);
        when(clickedBlock.getType()).thenReturn(Material.STONE_BUTTON);
        when(config.isInteractable(Material.STONE_BUTTON)).thenReturn(true);
        when(config.getMaxBlocks()).thenReturn(50);
        when(service.recordFragileBlockStates(clickedBlock, 50)).thenReturn(bfsResult);

        listener.onPlayerInteract(playerInteractEvent);

        verify(service).recordFragileBlockStates(clickedBlock, 50);
        verify(service).scheduleRestoration(any());
    }

    /********** Should not restore **********/

    @Test
    void onPlayerInteract_shouldNotRestoreWhenGamemodeIsNotValid() {
        when(config.isOnlyEnabledInCreativeMode()).thenReturn(true);
        when(playerInteractEvent.getPlayer()).thenReturn(player);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);

        listener.onPlayerInteract(playerInteractEvent);

        verifyNoInteractions(service);
    }

    @Test
    void onPlayerInteract_shouldNotRestoreWhenInteractionIsNotRightClickBlock() {
        when(config.isOnlyEnabledInCreativeMode()).thenReturn(false);
        when(playerInteractEvent.getAction()).thenReturn(Action.LEFT_CLICK_BLOCK);

        listener.onPlayerInteract(playerInteractEvent);

        verifyNoInteractions(service);
    }

    @Test
    void onPlayerInteract_shouldNotRestoreWhenPlayerIsUsingWrongEquipmentSlot() {
        when(config.isOnlyEnabledInCreativeMode()).thenReturn(false);
        when(playerInteractEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(playerInteractEvent.getHand()).thenReturn(EquipmentSlot.OFF_HAND);

        listener.onPlayerInteract(playerInteractEvent);

        verifyNoInteractions(service);
    }

    @Test
    void onPlayerInteract_shouldNotRestoreWhenPlayerIsSneakingAndHoldingAnItem() {
        when(config.isOnlyEnabledInCreativeMode()).thenReturn(false);
        when(playerInteractEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(playerInteractEvent.getHand()).thenReturn(EquipmentSlot.HAND);
        when(playerInteractEvent.getPlayer()).thenReturn(player);
        when(player.isSneaking()).thenReturn(true);
        when(playerInteractEvent.getItem()).thenReturn(new ItemStack(Material.NETHERITE_HOE));

        listener.onPlayerInteract(playerInteractEvent);

        verifyNoInteractions(service);
    }

    @Test
    void onPlayerInteract_shouldNotRestoreWhenSourceBlockIsNull() {
        when(config.isOnlyEnabledInCreativeMode()).thenReturn(false);
        when(playerInteractEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(playerInteractEvent.getHand()).thenReturn(EquipmentSlot.HAND);
        when(playerInteractEvent.getPlayer()).thenReturn(player);
        when(player.isSneaking()).thenReturn(false);
        when(playerInteractEvent.getClickedBlock()).thenReturn(null);

        listener.onPlayerInteract(playerInteractEvent);

        verifyNoInteractions(service);
    }

    @Test
    void onPlayerInteract_ShouldNotRestoreWhenBlockIsNotInteractable() {
        when(config.isOnlyEnabledInCreativeMode()).thenReturn(false);
        when(playerInteractEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(playerInteractEvent.getHand()).thenReturn(EquipmentSlot.HAND);
        when(playerInteractEvent.getPlayer()).thenReturn(player);
        when(player.isSneaking()).thenReturn(false);
        when(playerInteractEvent.getClickedBlock()).thenReturn(clickedBlock);
        when(clickedBlock.getType()).thenReturn(Material.COBBLESTONE);
        when(config.isInteractable(Material.COBBLESTONE)).thenReturn(false);

        listener.onPlayerInteract(playerInteractEvent);

        verifyNoInteractions(service);
    }
}
