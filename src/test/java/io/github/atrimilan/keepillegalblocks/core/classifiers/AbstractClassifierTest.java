package io.github.atrimilan.keepillegalblocks.core.classifiers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractClassifierTest {

    @Spy
    @InjectMocks
    private ReactiveClassifier classifier;

    @Nested
    @DisplayName("hasAnyInterface()")
    class HasAnyInterfaceTest {

        public interface FirstInterfaceStub {
        }

        public interface SecondInterfaceStub {
        }

        @Test
        void shouldBlockDataHaveExpectedInterface() {
            var mockBlock = mock(BlockData.class, Mockito.withSettings().extraInterfaces(FirstInterfaceStub.class));
            assertTrue(classifier.hasAnyInterface(mockBlock, "FirstInterfaceStub"));
        }

        @Test
        void shouldBlockDataHaveAnyInterfaceFromExpectedList() {
            var mockBlock = mock(BlockData.class, Mockito.withSettings().extraInterfaces(SecondInterfaceStub.class));
            assertTrue(classifier.hasAnyInterface(mockBlock, "FirstInterfaceStub", "SecondInterfaceStub"));
        }

        @Test
        void shouldBlockDataNotHaveExpectedInterface() {
            var mockBlock = mock(BlockData.class, Mockito.withSettings().extraInterfaces(FirstInterfaceStub.class));
            assertFalse(classifier.hasAnyInterface(mockBlock, "SecondInterfaceStub"));
        }
    }

    @Nested
    @DisplayName("matchAnyMaterial()")
    class MatchAnyMaterialTest {

        @Test
        void shouldReturnTrueWhenMaterialMatchesAnyProvidedName() {
            assertTrue(classifier.matchAnyMaterial(Material.OAK_DOOR, "OAK_BUTTON", "OAK_DOOR", "SPRUCE_DOOR"));
        }

        @Test
        void shouldReturnFalseWhenMaterialDoesNotMatchAnyProvidedName() {
            assertFalse(classifier.matchAnyMaterial(Material.OAK_DOOR, "OAK_BUTTON", "SPRUCE_DOOR"));
        }

        @Test
        void shouldReturnFalseWhenNameDoesNotExist() {
            assertFalse(classifier.matchAnyMaterial(Material.OAK_DOOR, "UNKNOWN_MATERIAL"));
        }
    }

    @Nested
    @DisplayName("isTagged()")
    class IsTaggedTest {

        public static class MaterialTagsStub {
            public static Tag<Material> MUSHROOMS;
        }

        MockedStatic<Bukkit> mockedBukkit;

        @BeforeEach
        void setUp() {
            mockedBukkit = mockStatic(Bukkit.class);
        }

        @AfterEach
        void tearDown() {
            if (mockedBukkit != null) mockedBukkit.close();
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldReturnTrueWhenMaterialIsTaggedInBukkitRegistry() {
            Tag<Material> mockTag = mock(Tag.class);
            when(mockTag.isTagged(Material.BROWN_MUSHROOM)).thenReturn(true);

            mockedBukkit.when(() -> Bukkit.getTag(eq(Tag.REGISTRY_ITEMS), eq(NamespacedKey.minecraft("mushrooms")),
                                                  eq(Material.class))).thenReturn(mockTag);

            assertTrue(classifier.isTagged(Material.BROWN_MUSHROOM, "mushrooms", "MUSHROOMS"));
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldReturnFalseWhenMaterialIsNotTaggedInBukkitRegistry() {
            Tag<Material> mockTag = mock(Tag.class);
            when(mockTag.isTagged(Material.OAK_BUTTON)).thenReturn(false);

            mockedBukkit.when(() -> Bukkit.getTag(eq(Tag.REGISTRY_ITEMS), any(NamespacedKey.class),
                                                  eq(Material.class))).thenReturn(mockTag);

            assertFalse(classifier.isTagged(Material.OAK_BUTTON, "unknown", "UNKNOWN"));
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldReturnTrueWhenNotTaggedInBukkitRegistryButTaggedInLegacyFallback() throws Exception {
            mockedBukkit.when(() -> Bukkit.getTag(any(), any(), any())).thenReturn(null);

            Tag<Material> mockLegacyTag = mock(Tag.class);
            when(mockLegacyTag.isTagged(Material.BROWN_MUSHROOM)).thenReturn(true);

            MaterialTagsStub.MUSHROOMS = mockLegacyTag;
            doReturn(MaterialTagsStub.class).when(classifier).getMaterialTagsClass(); // Stub reflected class

            assertTrue(classifier.isTagged(Material.BROWN_MUSHROOM, "mushrooms", "MUSHROOMS"));
        }

        @Test
        void shouldReturnFalseWhenTagDoesNotExistInBukkitRegistryAndLegacyFallbackFails() throws Exception {
            mockedBukkit.when(() -> Bukkit.getTag(any(), any(), any())).thenReturn(null);

            doReturn(MaterialTagsStub.class).when(classifier).getMaterialTagsClass(); // Stub reflected class

            assertFalse(classifier.isTagged(Material.OAK_BUTTON, "unknown", "UNKNOWN"));
        }
    }
}
