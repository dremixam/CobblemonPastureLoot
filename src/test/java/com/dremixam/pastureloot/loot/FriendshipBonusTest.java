package com.dremixam.pastureloot.loot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FriendshipBonusTest {

    private static final double TOLERANCE = 1e-12;
    private static final double DOUBLE_RATE = 1.0;

    @Test
    void noFriendshipGivesNoBonus() {
        assertEquals(1, FriendshipBonus.multiplier(0, DOUBLE_RATE), TOLERANCE);
    }

    @Test
    void maximumFriendshipGivesTheFullBonus() {
        assertEquals(2, FriendshipBonus.multiplier(FriendshipBonus.MAX_FRIENDSHIP, DOUBLE_RATE), TOLERANCE);
    }

    @Test
    void bonusGrowsInAStraightLine() {
        int halfFriendship = 51;
        double expected = 1 + (double) halfFriendship / FriendshipBonus.MAX_FRIENDSHIP;
        assertEquals(expected, FriendshipBonus.multiplier(halfFriendship, DOUBLE_RATE), TOLERANCE);
    }

    @Test
    void valuesOutOfRangeAreClamped() {
        assertEquals(2, FriendshipBonus.multiplier(1000, DOUBLE_RATE), TOLERANCE);
        assertEquals(1, FriendshipBonus.multiplier(-5, DOUBLE_RATE), TOLERANCE);
        assertEquals(1, FriendshipBonus.multiplier(FriendshipBonus.MAX_FRIENDSHIP, -3), TOLERANCE);
    }
}
