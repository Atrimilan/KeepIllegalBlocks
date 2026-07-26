package io.github.atrimilan.keepillegalblocks.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class LoadResultTest {

    @Test
    void shouldReturnLoadResultMessage() {
        LoadResult loadResult = new LoadResult("Reactive", 234, 4);

        assertEquals("<green>Reactive materials loaded: <white>234 <gray>(blacklisted: 4)", loadResult.chatFormat());
        assertEquals("Reactive materials loaded: 234 (blacklisted: 4)", loadResult.consoleFormat());
    }

    @Test
    void shouldReturnLoadResultMessageWithNothingBlacklisted() {
        LoadResult loadResult = new LoadResult("Interactable", 123, 0);

        assertEquals("<green>Interactable materials loaded: <white>123", loadResult.chatFormat());
        assertEquals("Interactable materials loaded: 123", loadResult.consoleFormat());
    }
}
