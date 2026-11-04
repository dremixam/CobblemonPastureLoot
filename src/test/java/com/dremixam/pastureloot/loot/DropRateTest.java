package com.dremixam.pastureloot.loot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DropRateTest {

    private static final double TOLERANCE = 1e-12;

    @Test
    void averageOverAnHourGivesTheRate() {
        double dropsPerHour = 3;
        double expected = DropRate.chancePerTick(dropsPerHour) * DropRate.TICKS_PER_HOUR;
        assertEquals(dropsPerHour, expected, TOLERANCE);
    }

    @Test
    void negativeAndNanRatesDisableDrops() {
        assertEquals(0, DropRate.chancePerTick(-1));
        assertEquals(0, DropRate.chancePerTick(Double.NaN));
        assertEquals(0, DropRate.chancePerTick(0));
    }

    @Test
    void absurdRatesAreCapped() {
        assertEquals(DropRate.MAX_DROPS_PER_HOUR, DropRate.clamp(1e9));
        assertEquals(DropRate.MAX_DROPS_PER_HOUR, DropRate.clamp(Double.POSITIVE_INFINITY));
    }

    @Test
    void chanceStaysBelowOneAtTheCap() {
        assertTrue(DropRate.chancePerTick(DropRate.MAX_DROPS_PER_HOUR) < 1);
    }
}
