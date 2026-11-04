package com.dremixam.pastureloot.minecraft;

import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge.PasturedPokemon;
import com.dremixam.pastureloot.override.OverrideRule;
import com.dremixam.pastureloot.override.Place;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.stream.Collectors;

/** Finds the override rules that match a pokémon of one pasture; the place is read at most once. */
final class OverrideMatcher {

    private final ServerLevel level;
    private final BlockPos pasturePos;
    private Place place;

    OverrideMatcher(ServerLevel level, BlockPos pasturePos) {
        this.level = level;
        this.pasturePos = pasturePos;
    }

    /** The rules that match, in file order. */
    List<OverrideRule> matching(PasturedPokemon pasturedPokemon, List<OverrideRule> rules) {
        if (rules.isEmpty()) {
            return List.of();
        }
        return rules.stream()
                .filter(rule -> CobblemonBridge.matches(pasturedPokemon, rule.pokemon()))
                .filter(rule -> !rule.hasPlace() || rule.placeMatches(place()))
                .toList();
    }

    private Place place() {
        if (place == null) {
            Holder<Biome> biome = level.getBiome(pasturePos);
            String biomeId = biome.unwrapKey().map(key -> key.location().toString()).orElse("");
            place = new Place(
                    biomeId,
                    biome.tags().map(tag -> tag.location().toString()).collect(Collectors.toSet()),
                    level.dimension().location().toString());
        }
        return place;
    }
}
