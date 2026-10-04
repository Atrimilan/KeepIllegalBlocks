package io.github.atrimilan.keepillegalblocks.commands.kib;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.github.atrimilan.keepillegalblocks.BukkitMockFactory;
import io.github.atrimilan.keepillegalblocks.config.ConfigManager;
import io.github.atrimilan.keepillegalblocks.core.RegistryLoader;
import io.github.atrimilan.keepillegalblocks.models.MaterialGroup;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KibBlacklistCommandNodeTest {

    @InjectMocks
    private KibBlacklistCommandNode kibBlacklistCommandNode;

    @Mock
    private CommandContext<CommandSourceStack> ctx;

    @Mock
    private CommandSourceStack source;

    @Mock
    private CommandSender sender;

    @Mock
    private RegistryLoader registryLoader;

    @Mock
    private ConfigManager configManager;

    private MockedStatic<ArgumentTypes> mockedArgumentTypes;

    @BeforeEach
    void setUp() {
        lenient().when(ctx.getSource()).thenReturn(source);
        lenient().when(source.getSender()).thenReturn(sender);
        mockedArgumentTypes = mockStatic(ArgumentTypes.class);
        mockedArgumentTypes.when(ArgumentTypes::blockState).thenReturn(mock(ArgumentType.class));
    }

    @AfterEach
    void tearDown() {
        if (mockedArgumentTypes != null) mockedArgumentTypes.close();
    }

    @ParameterizedTest
    @EnumSource(value = MaterialGroup.class)
    void shouldBuild(MaterialGroup materialGroup) {
        LiteralArgumentBuilder<CommandSourceStack> node = kibBlacklistCommandNode.build();
        assertEquals("blacklist", node.getLiteral());
        assertNull(node.getCommand());

        CommandNode<CommandSourceStack> materialGroupNode = node.build().getChild(materialGroup.getName());
        assertNotNull(materialGroupNode);
        assertNull(materialGroupNode.getCommand());

        CommandNode<CommandSourceStack> addNode = materialGroupNode.getChild("add");
        assertNotNull(addNode);
        assertNull(addNode.getCommand());

        CommandNode<CommandSourceStack> addMaterialNode = addNode.getChild("material");
        assertNotNull(addMaterialNode);
        assertNotNull(addMaterialNode.getCommand());

        CommandNode<CommandSourceStack> removeNode = materialGroupNode.getChild("remove");
        assertNotNull(removeNode);
        assertNull(removeNode.getCommand());

        CommandNode<CommandSourceStack> removeMaterialNode = removeNode.getChild("material");
        assertNotNull(removeMaterialNode);
        assertNotNull(removeMaterialNode.getCommand());

        when(sender.hasPermission("kib.blacklist")).thenReturn(false);
        assertFalse(node.getRequirement().test(source));

        when(sender.hasPermission("kib.blacklist")).thenReturn(true);
        assertTrue(node.getRequirement().test(source));
    }

    @ParameterizedTest
    @EnumSource(MaterialGroup.class)
    void shouldAddToBlacklist(MaterialGroup group) throws Exception {
        Material material = Material.OAK_DOOR;
        BlockState blockState = BukkitMockFactory.mockBlockState(material);
        when(ctx.getArgument(eq("material"), any())).thenReturn(blockState);

        when(configManager.addToBlacklist(group, material.name())).thenReturn(true);

        CommandNode<CommandSourceStack> node = kibBlacklistCommandNode.build().build().getChild(group.getName())
                .getChild("add").getChild("material");
        int result = node.getCommand().run(ctx);

        assertEquals(Command.SINGLE_SUCCESS, result);
        verify(configManager, times(1)).addToBlacklist(group, material.name());
        verify(registryLoader, times(1)).loadMaterialRegistry(configManager.getConfig());
        verify(sender, times(1)).sendMessage(any(Component.class));
    }

    @ParameterizedTest
    @EnumSource(MaterialGroup.class)
    void shouldNotAddToBlacklistWhenAlreadyPresent(MaterialGroup group) throws Exception {
        Material material = Material.OAK_DOOR;
        BlockState blockState = BukkitMockFactory.mockBlockState(material);
        when(ctx.getArgument(eq("material"), any())).thenReturn(blockState);

        when(configManager.addToBlacklist(group, material.name())).thenReturn(false);

        CommandNode<CommandSourceStack> node = kibBlacklistCommandNode.build().build().getChild(group.getName())
                .getChild("add").getChild("material");
        int result = node.getCommand().run(ctx);

        assertEquals(Command.SINGLE_SUCCESS, result);
        verify(configManager, times(1)).addToBlacklist(group, material.name());
        verify(registryLoader, never()).loadMaterialRegistry(configManager.getConfig());
        verify(sender, times(1)).sendMessage(any(Component.class));
    }


    @ParameterizedTest
    @EnumSource(MaterialGroup.class)
    void shouldRemoveFromBlacklist(MaterialGroup group) throws Exception {
        Material material = Material.OAK_DOOR;
        BlockState blockState = BukkitMockFactory.mockBlockState(material);
        when(ctx.getArgument(eq("material"), any())).thenReturn(blockState);

        when(configManager.removeFromBlacklist(group, material.name())).thenReturn(true);

        CommandNode<CommandSourceStack> node = kibBlacklistCommandNode.build().build().getChild(group.getName())
                .getChild("remove").getChild("material");
        int result = node.getCommand().run(ctx);

        assertEquals(Command.SINGLE_SUCCESS, result);
        verify(configManager, times(1)).removeFromBlacklist(group, material.name());
        verify(registryLoader, times(1)).loadMaterialRegistry(configManager.getConfig());
        verify(sender, times(1)).sendMessage(any(Component.class));
    }

    @ParameterizedTest
    @EnumSource(MaterialGroup.class)
    void shouldNotRemoveFromBlacklistWhenAlreadyPresent(MaterialGroup group) throws Exception {
        Material material = Material.OAK_DOOR;
        BlockState blockState = BukkitMockFactory.mockBlockState(material);
        when(ctx.getArgument(eq("material"), any())).thenReturn(blockState);

        when(configManager.removeFromBlacklist(group, material.name())).thenReturn(false);

        CommandNode<CommandSourceStack> node = kibBlacklistCommandNode.build().build().getChild(group.getName())
                .getChild("remove").getChild("material");
        int result = node.getCommand().run(ctx);

        assertEquals(Command.SINGLE_SUCCESS, result);
        verify(configManager, times(1)).removeFromBlacklist(group, material.name());
        verify(registryLoader, never()).loadMaterialRegistry(configManager.getConfig());
        verify(sender, times(1)).sendMessage(any(Component.class));
    }
}
