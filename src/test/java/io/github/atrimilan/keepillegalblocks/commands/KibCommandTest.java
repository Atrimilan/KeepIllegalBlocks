package io.github.atrimilan.keepillegalblocks.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KibCommandTest {

    @InjectMocks
    private KibCommand kibCommand;

    @Mock
    private CommandSourceStack source;

    @Mock
    private CommandSender sender;

    private MockedStatic<ArgumentTypes> mockedArgumentTypes;

    @BeforeEach
    void setUp() {
        mockedArgumentTypes = mockStatic(ArgumentTypes.class);
        mockedArgumentTypes.when(ArgumentTypes::blockState).thenReturn(mock(ArgumentType.class));
    }

    @AfterEach
    void tearDown() {
        if (mockedArgumentTypes != null) mockedArgumentTypes.close();
    }

    @Test
    void shouldBuild() {
        when(source.getSender()).thenReturn(sender);

        LiteralCommandNode<CommandSourceStack> node = kibCommand.build();

        assertEquals("kib", node.getName());

        assertEquals(4, node.getChildren().size());
        assertNotNull(node.getChild("blacklist"));
        assertNotNull(node.getChild("help"));
        assertNotNull(node.getChild("reload"));
        assertNotNull(node.getChild("rule"));

        when(sender.hasPermission("kib")).thenReturn(false);
        assertFalse(node.getRequirement().test(source));

        when(sender.hasPermission("kib")).thenReturn(true);
        assertTrue(node.getRequirement().test(source));
    }
}
