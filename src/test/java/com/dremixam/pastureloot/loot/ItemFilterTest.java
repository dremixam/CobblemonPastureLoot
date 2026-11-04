package com.dremixam.pastureloot.loot;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemFilterTest {

    @Test
    void blacklistedItemsAreRefused() {
        ItemFilter filter = new ItemFilter(List.of("minecraft:bone", "minecraft:beef"));
        assertFalse(filter.allows("minecraft:bone"));
        assertTrue(filter.allows("minecraft:string"));
    }

    @Test
    void duplicatesInTheListAreAccepted() {
        ItemFilter filter = new ItemFilter(List.of("minecraft:bone", "minecraft:bone"));
        assertFalse(filter.allows("minecraft:bone"));
    }
}
