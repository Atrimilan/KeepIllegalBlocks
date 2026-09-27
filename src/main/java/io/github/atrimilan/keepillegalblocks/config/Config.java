package io.github.atrimilan.keepillegalblocks.config;

import io.github.atrimilan.keepillegalblocks.models.MaterialGroup;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record Config(
        boolean packetEventsPresent,
        int maxBlocks,
        boolean onlyEnabledInCreativeMode,
        boolean usePacketEventsIfDetected,
        Map<MaterialGroup, Set<String>> blacklists,
        Map<MaterialGroup, Set<String>> enabledCategories
) {

    /**
     * @return Whether PacketEvents should be used to improve rendering (if it's detected and enabled on the server).
     */
    public boolean isPacketEventsEnabled() {
        return packetEventsPresent && usePacketEventsIfDetected;
    }

    /**
     * @param key The {@link MaterialGroup}
     * @return A set of blacklisted material names for the specified group.
     */
    public Set<String> getBlacklistedMaterialsForGroup(MaterialGroup key) {
        return blacklists.getOrDefault(key, Collections.emptySet());
    }

    /**
     * @param key The {@link MaterialGroup}
     * @return A set of enabled categories for the specified group.
     */
    public Set<String> getEnabledCategoriesForGroup(MaterialGroup key) {
        return enabledCategories.getOrDefault(key, Collections.emptySet());
    }

    /**
     * @param rule The {@link Rule} to get the value for.
     * @return The value of the specified rule.
     */
    public Object getRule(Rule rule) {
        return switch (rule) {
            case Rule.MAX_BLOCKS -> maxBlocks;
            case Rule.ONLY_USE_KIB_IN_CREATIVE_MODE -> onlyEnabledInCreativeMode;
            case Rule.USE_PACKET_EVENTS_IF_DETECTED -> usePacketEventsIfDetected;
        };
    }
}
