package com.dremixam.pastureloot.loot;

/** Converts a drop rate given per hour of game time into a chance per tick. */
public final class DropRate {

    /** 20 ticks per second, 3600 seconds per hour. */
    public static final double TICKS_PER_HOUR = 72_000;
    /** One drop per second and per pokémon, far above any sane setting. */
    public static final double MAX_DROPS_PER_HOUR = 3_600;

    private DropRate() {
    }

    /** Chance that one pokémon drops on a given tick, so that it drops dropsPerHour times per hour on average. */
    public static double chancePerTick(double dropsPerHour) {
        return clamp(dropsPerHour) / TICKS_PER_HOUR;
    }

    /** The rate actually used: negative and NaN values give 0, values above the maximum give the maximum. */
    public static double clamp(double dropsPerHour) {
        if (!(dropsPerHour > 0)) {
            return 0;
        }
        return Math.min(dropsPerHour, MAX_DROPS_PER_HOUR);
    }
}
