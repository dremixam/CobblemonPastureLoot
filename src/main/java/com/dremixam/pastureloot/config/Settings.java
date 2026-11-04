package com.dremixam.pastureloot.config;

import com.dremixam.pastureloot.loot.ItemFilter;

import java.util.List;

/** Values of the main settings file, already checked and clamped. */
public record Settings(
        double dropsPerHour,
        List<String> filterItems,
        ItemFilter.Mode filterMode,
        double friendshipMaxBonus,
        int maxItemsNearby) {

    /** Same average as the 1.0.x default of 0.05 per minute. */
    public static final double DEFAULT_DROPS_PER_HOUR = 3.0;
    /** Twice the rate at maximum friendship. */
    public static final double DEFAULT_FRIENDSHIP_MAX_BONUS = 1.0;
    public static final double MAX_FRIENDSHIP_MAX_BONUS = 10.0;
    /** A handful of drops left on the ground, far below what slows a server. */
    public static final int DEFAULT_MAX_ITEMS_NEARBY = 32;
    public static final int MAX_ITEMS_NEARBY_LIMIT = 10_000;

    /** Items a pokémon only gives when it dies, and items Cobblemon already lets players harvest from pokémon. */
    public static final List<String> DEFAULT_FILTER_ITEMS = List.of(
            "#c:foods/raw_meat",
            "#c:foods/cooked_meat",
            "#c:foods/raw_fish",
            "#c:foods/cooked_fish",
            "minecraft:pufferfish",
            "minecraft:leather",
            "minecraft:rabbit_hide",
            "minecraft:rabbit_foot",
            "minecraft:bone",
            "minecraft:bone_block",
            "minecraft:bone_meal",
            "minecraft:spider_eye",
            "minecraft:rotten_flesh",
            "minecraft:honey_bottle",
            "minecraft:white_wool",
            "cobblemon:sharp_beak"
    );

    public static final Settings DEFAULTS = new Settings(
            DEFAULT_DROPS_PER_HOUR,
            DEFAULT_FILTER_ITEMS,
            ItemFilter.Mode.BLACKLIST,
            DEFAULT_FRIENDSHIP_MAX_BONUS,
            DEFAULT_MAX_ITEMS_NEARBY);

    public Settings {
        filterItems = List.copyOf(filterItems);
    }

    public ItemFilter itemFilter() {
        return new ItemFilter(filterItems, filterMode);
    }
}
