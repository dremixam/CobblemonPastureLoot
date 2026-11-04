package com.dremixam.pastureloot.loot;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemFilterTest {

    private static final Set<String> NO_TAGS = Set.of();

    @Test
    void blacklistRefusesListedIds() {
        ItemFilter filter = new ItemFilter(List.of("minecraft:bone", "minecraft:beef"), ItemFilter.Mode.BLACKLIST);
        assertFalse(filter.allows("minecraft:bone", NO_TAGS));
        assertTrue(filter.allows("minecraft:string", NO_TAGS));
    }

    @Test
    void blacklistRefusesItemsOfAListedTag() {
        ItemFilter filter = new ItemFilter(List.of("#c:foods/raw_meat"), ItemFilter.Mode.BLACKLIST);
        assertFalse(filter.allows("minecraft:beef", Set.of("c:foods", "c:foods/raw_meat")));
        assertTrue(filter.allows("minecraft:bread", Set.of("c:foods")));
    }

    @Test
    void whitelistOnlyAcceptsListedItems() {
        ItemFilter filter = new ItemFilter(List.of("minecraft:snowball", "#c:gems"), ItemFilter.Mode.WHITELIST);
        assertTrue(filter.allows("minecraft:snowball", NO_TAGS));
        assertTrue(filter.allows("minecraft:diamond", Set.of("c:gems")));
        assertFalse(filter.allows("minecraft:bone", NO_TAGS));
    }

    @Test
    void blankEntriesAndSpacesAreIgnored() {
        ItemFilter filter = new ItemFilter(List.of("  minecraft:bone ", ""), ItemFilter.Mode.BLACKLIST);
        assertFalse(filter.allows("minecraft:bone", NO_TAGS));
        assertTrue(filter.allows("", NO_TAGS));
    }
}
