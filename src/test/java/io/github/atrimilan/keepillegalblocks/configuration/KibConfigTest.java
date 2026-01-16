package io.github.atrimilan.keepillegalblocks.configuration;

import io.github.atrimilan.keepillegalblocks.configuration.types.FragileType;
import io.github.atrimilan.keepillegalblocks.configuration.types.InteractableType;
import io.github.atrimilan.keepillegalblocks.models.LoadResult;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KibConfigTest {

    @Spy
    @InjectMocks
    private KibConfig kibConfig;

    @Mock
    private JavaPlugin plugin;

    @Mock
    private FileConfiguration fileConfig;

    @Mock
    private FileConfigurationOptions fileConfigOptions;

    @Captor
    private ArgumentCaptor<Supplier<String>> logCaptor;

    @Test
    void shouldInit() {
        Logger logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getConfig()).thenReturn(fileConfig);
        when(fileConfig.options()).thenReturn(fileConfigOptions);

        List<LoadResult> results = List.of(new LoadResult("Fragile", 10, 2), //
                                           new LoadResult("Interactable", 5, 1));
        doReturn(results).when(kibConfig).loadRegistries();

        kibConfig.init();

        verify(plugin).saveDefaultConfig();
        verify(fileConfigOptions).copyDefaults(true);
        verify(plugin).saveConfig();
        verify(plugin, never()).reloadConfig(); // Never
        verify(kibConfig).loadRegistries();
        verify(logger, times(2)).info(logCaptor.capture());

        List<String> logMsg = logCaptor.getAllValues().stream().map(Supplier::get).toList();
        assertEquals(2, logMsg.size());
        assertTrue(logMsg.contains("Fragile blocks loaded: 10 (2 blacklisted)"));
        assertTrue(logMsg.contains("Interactable blocks loaded: 5 (1 blacklisted)"));
    }

    @Test
    void shouldReload() {
        List<LoadResult> results = List.of(new LoadResult("Fragile", 10, 2), //
                                           new LoadResult("Interactable", 5, 1));
        doReturn(results).when(kibConfig).loadRegistries();
        when(fileConfig.options()).thenReturn(fileConfigOptions);

        List<LoadResult> actualResults = kibConfig.reload();

        assertEquals(results, actualResults);

        verify(plugin, never()).saveDefaultConfig(); // Never
        verify(fileConfig.options(), never()).copyDefaults(true); // Never
        verify(plugin, never()).saveConfig(); // Never
        verify(plugin).reloadConfig();
        verify(kibConfig).loadRegistries();
    }

    @Test
    void shouldLoadRegistries() {
        List<LoadResult> results = List.of(new LoadResult("Fragile", 0, 2), //
                                           new LoadResult("Interactable", 0, 1));
        doReturn(2).when(kibConfig).loadRegistry(eq(fileConfig), eq("fragile-blocks."), anyMap(), any());
        doReturn(1).when(kibConfig).loadRegistry(eq(fileConfig), eq("interactable-blocks."), anyMap(), any());

        when(plugin.getConfig()).thenReturn(fileConfig);

        List<LoadResult> actualResults = kibConfig.loadRegistries();

        assertEquals(results, actualResults);

        verify(plugin).getConfig();
        verify(fileConfig).getInt("max-blocks");
        verify(kibConfig).loadRegistry(eq(fileConfig), eq("fragile-blocks."), anyMap(), any());
        verify(kibConfig).loadRegistry(eq(fileConfig), eq("interactable-blocks."), anyMap(), any());
    }

    @Test
    void shouldBeFragile() throws Exception {
        Field field = KibConfig.class.getDeclaredField("fragileBlocks"); // Reflection
        field.setAccessible(true);
        Map<Material, FragileType> map = (Map<Material, FragileType>) field.get(kibConfig);

        map.put(Material.RED_BED, FragileType.BED);

        assertTrue(kibConfig.isFragile(Material.RED_BED));
        assertFalse(kibConfig.isFragile(Material.DIRT));
        assertFalse(kibConfig.isFragile(null));
    }

    @Test
    void shouldBeInteractable() throws Exception {
        Field field = KibConfig.class.getDeclaredField("interactableBlocks"); // Reflection
        field.setAccessible(true);
        Map<Material, InteractableType> map = (Map<Material, InteractableType>) field.get(kibConfig);

        map.put(Material.STONE_BUTTON, InteractableType.SWITCH);

        assertTrue(kibConfig.isInteractable(Material.STONE_BUTTON));
        assertFalse(kibConfig.isInteractable(Material.DIRT));
        assertFalse(kibConfig.isInteractable(null));
    }
}
