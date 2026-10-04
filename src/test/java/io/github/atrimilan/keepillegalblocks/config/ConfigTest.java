package io.github.atrimilan.keepillegalblocks.config;

import io.github.atrimilan.keepillegalblocks.models.MaterialGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConfigTest {

    @Test
    void shouldCheckIfPacketEventsIsEnabled() {
        Config configWithPacketEventsEnabled = new Config(true, 42, true, true, Map.of(), Map.of());
        assertTrue(configWithPacketEventsEnabled.isPacketEventsEnabled()); // Present and must be used

        Config configWithPacketEventsDisabled = new Config(true, 42, true, false, Map.of(), Map.of());
        assertFalse(configWithPacketEventsDisabled.isPacketEventsEnabled()); // Present but must not be used

        Config configWithoutPacketEvents = new Config(false, 42, true, true, Map.of(), Map.of());
        assertFalse(configWithoutPacketEvents.isPacketEventsEnabled()); // Not present
    }

    @Test
    void shouldGetBlacklistedMaterialsForGroup() {
        var blacklists = Map.of(MaterialGroup.INTERACTABLE, Set.of("doors"));
        var config = new Config(false, 42, false, false, blacklists, Map.of());

        // The interactable blacklist contains the "doors" category
        assertEquals(Set.of("doors"), config.getBlacklistedMaterialsForGroup(MaterialGroup.INTERACTABLE));

        // The reactive blacklist is empty
        assertTrue(config.getBlacklistedMaterialsForGroup(MaterialGroup.REACTIVE).isEmpty());
    }

    @Test
    void shouldGetEnabledCategoriesForGroup() {
        var categories = Map.of(MaterialGroup.REACTIVE, Set.of("switches"));
        var config = new Config(false, 42, false, false, Map.of(), categories);

        // "switches" is enabled for reactive
        assertEquals(Set.of("switches"), config.getEnabledCategoriesForGroup(MaterialGroup.REACTIVE));

        // No categories are enabled for interactable
        assertTrue(config.getEnabledCategoriesForGroup(MaterialGroup.INTERACTABLE).isEmpty());
    }

    @ParameterizedTest
    @EnumSource(Rule.class)
    void shouldGetRuleValue(Rule rule) {
        var config = new Config(false, 42, true, false, Map.of(), Map.of());

        Object value = config.getRule(rule);

        switch (rule) {
            case MAX_BLOCKS -> assertEquals(42, value);
            case ONLY_USE_KIB_IN_CREATIVE_MODE -> assertEquals(true, value);
            case USE_PACKET_EVENTS_IF_DETECTED -> assertEquals(false, value);
        }
    }
}
