package io.github.atrimilan.lockblockstate.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.github.atrimilan.lockblockstate.services.ReadConfigService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReadConfigCommandTest {

    @Mock
    ReadConfigService readConfigService;

    @Mock
    CommandSourceStack commandSourceStack;

    @Mock
    CommandContext<CommandSourceStack> ctx;

    @Mock
    CommandSender commandSender;

    @InjectMocks
    ReadConfigCommand readConfigCommand;

    @BeforeEach
    void setUp() {
        lenient().when(ctx.getSource()).thenReturn(commandSourceStack);
        lenient().when(commandSourceStack.getSender()).thenReturn(commandSender);
    }

    static Stream<Arguments> provideSimpleValueTestData() {
        return Stream.of( //
                Arguments.of("ultimate-answer", "example.ultimate-answer",
                        "The answer to the Ultimate Question of Life, the Universe, and Everything is: <dark_red><bold><value>"),
                Arguments.of("pangram", "example.pangram", "Pangram: <gold><value>"),
                Arguments.of("boolean", "example.boolean", "Isn't this a Boolean: <gold><value>")
        );
    }

    @ParameterizedTest()
    @MethodSource("provideSimpleValueTestData")
    void shouldReadSimpleValues(String subCommand, String expectedPath, String expectedTemplate) throws Exception {
        // Given
        CommandNode<CommandSourceStack> node = readConfigCommand.create().getChild(subCommand);
        // When
        node.getCommand().run(ctx);
        // Then
        verify(readConfigService).readSimpleValue(commandSender, expectedPath, expectedTemplate);
    }

    @Test
    void shouldReadPlayer() throws Exception {
        // Given
        CommandNode<CommandSourceStack> node = readConfigCommand.create().getChild("player");
        // When
        node.getCommand().run(ctx);
        // Then
        verify(readConfigService).readPlayer(commandSender);
    }
}
