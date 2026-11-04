package com.dremixam.pastureloot.loot;

/** Multiplier of the drop rate that grows with the friendship of the pokémon. */
public final class FriendshipBonus {

    /** Highest friendship value in Cobblemon. */
    public static final int MAX_FRIENDSHIP = 255;

    private FriendshipBonus() {
    }

    /** 1 at friendship 0, growing in a straight line up to 1 + maxBonus at maximum friendship. */
    public static double multiplier(int friendship, double maxBonus) {
        int clampedFriendship = Math.max(0, Math.min(MAX_FRIENDSHIP, friendship));
        double bonus = Math.max(0, maxBonus);
        return 1 + bonus * clampedFriendship / MAX_FRIENDSHIP;
    }
}
