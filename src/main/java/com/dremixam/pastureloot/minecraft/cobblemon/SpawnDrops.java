package com.dremixam.pastureloot.minecraft.cobblemon;

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools;
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Set;

/** Finds the drops that a Cobblemon spawn gives to wild pokémon at a given place. */
final class SpawnDrops {

    private SpawnDrops() {
    }

    /**
     * The drop table of the first spawn of this pokémon that has its own drops and whose biomes or
     * dimensions contain the place, or null. Only the place is checked, not time, light or sky.
     */
    static DropTable at(Pokemon pokemon, ServerLevel level, BlockPos pos) {
        Place place = new Place(level.getBiome(pos), level.dimension().location());
        for (SpawnDetail detail : CobblemonSpawnPools.WORLD_SPAWN_POOL) {
            if (detail instanceof PokemonSpawnDetail spawn
                    && spawn.getDrops() != null
                    && spawn.getPokemon().matches(pokemon)
                    && fits(spawn, place)) {
                return spawn.getDrops();
            }
        }
        return null;
    }

    private static boolean fits(SpawnDetail detail, Place place) {
        boolean anyCondition = detail.getConditions().stream()
                .anyMatch(condition -> matches(condition, place, true));
        boolean anyAnticondition = detail.getAnticonditions().stream()
                .anyMatch(condition -> matches(condition, place, false));
        return anyCondition && !anyAnticondition;
    }

    /** Whether the place matches the biomes and dimensions of the condition; withoutPlace when it names none. */
    private static boolean matches(SpawningCondition<?> condition, Place place, boolean withoutPlace) {
        Set<RegistryLikeCondition<Biome>> biomes = condition.getBiomes();
        List<ResourceLocation> dimensions = condition.getDimensions();
        boolean hasBiomes = biomes != null && !biomes.isEmpty();
        boolean hasDimensions = dimensions != null && !dimensions.isEmpty();
        if (!hasBiomes && !hasDimensions) {
            return withoutPlace;
        }
        boolean biomeFits = !hasBiomes || biomes.stream().anyMatch(biome -> biome.fits(place.biome()));
        boolean dimensionFits = !hasDimensions || dimensions.contains(place.dimension());
        return biomeFits && dimensionFits;
    }

    private record Place(Holder<Biome> biome, ResourceLocation dimension) {
    }
}
