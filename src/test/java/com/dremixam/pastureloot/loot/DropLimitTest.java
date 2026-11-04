package com.dremixam.pastureloot.loot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DropLimitTest {

    private static final int LIMIT = 32;

    @Test
    void dropsStopAtTheLimit() {
        assertTrue(DropLimit.allowsDrop(LIMIT - 1, LIMIT));
        assertFalse(DropLimit.allowsDrop(LIMIT, LIMIT));
    }

    @Test
    void zeroMeansNoLimit() {
        assertTrue(DropLimit.allowsDrop(1_000_000, 0));
    }
}
