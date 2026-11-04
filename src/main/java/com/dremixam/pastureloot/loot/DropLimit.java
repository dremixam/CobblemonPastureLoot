package com.dremixam.pastureloot.loot;

/** Stops a pasture from dropping while too many items lie around it. */
public final class DropLimit {

    private DropLimit() {
    }

    /** Whether a drop may happen with this many item entities nearby; a limit of 0 or less means no limit. */
    public static boolean allowsDrop(int itemsNearby, int maxItemsNearby) {
        return maxItemsNearby <= 0 || itemsNearby < maxItemsNearby;
    }
}
