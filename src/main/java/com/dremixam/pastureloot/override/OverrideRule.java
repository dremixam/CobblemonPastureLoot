package com.dremixam.pastureloot.override;

import com.dremixam.pastureloot.loot.ItemFilter;

import java.util.List;

/** One [[override]] of the overrides file. */
public record OverrideRule(
        String pokemon,
        Mode mode,
        List<DropSpec> drops,
        List<String> removedItems,
        double rateMultiplier,
        List<String> biomes,
        List<String> dimensions) {

    public static final double NO_RATE_CHANGE = 1.0;

    public enum Mode {
        /** The drop table of Cobblemon is dropped, only the drops of the rule are left. */
        REPLACE,
        /** The drops of the rule come on top of what was there. */
        ADD,
        /** The listed items are taken out of what was there. */
        REMOVE
    }

    public OverrideRule {
        drops = List.copyOf(drops);
        removedItems = List.copyOf(removedItems);
        biomes = List.copyOf(biomes);
        dimensions = List.copyOf(dimensions);
    }

    public boolean changesRate() {
        return rateMultiplier != NO_RATE_CHANGE;
    }

    public boolean hasPlace() {
        return !biomes.isEmpty() || !dimensions.isEmpty();
    }

    /** Whether the place fits: one of the biomes (ids or "#" tags) and one of the dimensions, when given. */
    public boolean placeMatches(Place place) {
        boolean biomeFits = biomes.isEmpty() || biomes.stream().anyMatch(biome -> biomeMatches(biome, place));
        boolean dimensionFits = dimensions.isEmpty() || dimensions.contains(place.dimensionId());
        return biomeFits && dimensionFits;
    }

    private static boolean biomeMatches(String biome, Place place) {
        if (biome.startsWith(ItemFilter.TAG_PREFIX)) {
            return place.biomeTags().contains(biome.substring(ItemFilter.TAG_PREFIX.length()));
        }
        return biome.equals(place.biomeId());
    }
}
