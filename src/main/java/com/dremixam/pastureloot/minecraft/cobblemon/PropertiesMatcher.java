package com.dremixam.pastureloot.minecraft.cobblemon;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.dremixam.pastureloot.PastureLoot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Matches pokémon against property strings like "vulpix alolan", parsed once each. */
final class PropertiesMatcher {

    private static final String ASSIGNER = "=";
    private static final Map<String, Optional<PokemonProperties>> PARSED = new HashMap<>();

    private PropertiesMatcher() {
    }

    static boolean matches(Pokemon pokemon, String properties) {
        return PARSED.computeIfAbsent(properties, PropertiesMatcher::parse)
                .map(parsed -> parsed.matches(pokemon))
                .orElse(false);
    }

    /** Parsed lazily: the species are only known once Cobblemon has loaded its data. */
    private static Optional<PokemonProperties> parse(String properties) {
        try {
            PokemonProperties parsed = PokemonProperties.Companion.parse(properties);
            warnWhenSpeciesIsUnknown(properties, parsed);
            return Optional.of(parsed);
        } catch (RuntimeException exception) {
            PastureLoot.LOGGER.warn("Override pokemon \"{}\" cannot be read, the rule is ignored: {}",
                    properties, exception.getMessage());
            return Optional.empty();
        }
    }

    private static void warnWhenSpeciesIsUnknown(String properties, PokemonProperties parsed) {
        String firstWord = properties.trim().split("\\s+")[0];
        if (parsed.getSpecies() == null && !firstWord.contains(ASSIGNER) && PokemonSpecies.getByName(firstWord) == null) {
            PastureLoot.LOGGER.warn("Override pokemon \"{}\": \"{}\" is not a species, the rule matches any "
                    + "pokemon with the other properties.", properties, firstWord);
        }
    }
}
