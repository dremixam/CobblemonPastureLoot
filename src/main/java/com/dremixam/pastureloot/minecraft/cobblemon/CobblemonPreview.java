package com.dremixam.pastureloot.minecraft.cobblemon;

import com.cobblemon.mod.common.api.drop.DropEntry;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.drop.ItemDropEntry;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge.PokemonRef;
import kotlin.ranges.IntRange;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Describes a drop table without rolling it, for /pastureloot preview. */
public final class CobblemonPreview {

    /** Name of the standard form, not worth showing next to the species. */
    private static final String STANDARD_FORM = "Normal";

    private CobblemonPreview() {
    }

    /** Where the table comes from: the current form, or a spawn of the place. */
    public enum Source { FORM, SPAWN }

    public record EntryView(String itemId, int minQuantity, int maxQuantity, float percentage) {
    }

    public record TableView(Source source, int minRolls, int maxRolls, List<EntryView> entries) {
    }

    /** A pokémon found in the world, with the place its drops are read at: its pasture, or where it stands. */
    public record InspectedPokemon(PokemonRef pokemon, ServerLevel level, BlockPos place, boolean pastured) {
    }

    /** A new pokémon built from properties like "vulpix alolan"; empty when no species is given. */
    public static Optional<PokemonRef> fromProperties(String properties) {
        PokemonProperties parsed = PokemonProperties.Companion.parse(properties);
        if (parsed.getSpecies() == null) {
            return Optional.empty();
        }
        return Optional.of(new PokemonRef(parsed.create()));
    }

    public static Optional<InspectedPokemon> fromEntity(Entity entity) {
        if (!(entity instanceof PokemonEntity pokemonEntity) || !(entity.level() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        PokemonPastureBlockEntity.Tethering tethering = pokemonEntity.getTethering();
        BlockPos place = tethering != null ? tethering.getPasturePos() : pokemonEntity.blockPosition();
        return Optional.of(new InspectedPokemon(
                new PokemonRef(pokemonEntity.getPokemon()), level, place, tethering != null));
    }

    /** The table a drop would roll for this pokémon at this place, before the overrides. */
    public static TableView table(PokemonRef pokemonRef, ServerLevel level, BlockPos place) {
        Pokemon pokemon = pokemonRef.pokemon();
        DropTable spawnTable = SpawnDrops.at(pokemon, level, place);
        DropTable table = spawnTable != null ? spawnTable : pokemon.getForm().getDrops();
        List<EntryView> entries = new ArrayList<>();
        for (DropEntry entry : table.getEntries()) {
            if (entry instanceof ItemDropEntry itemEntry) {
                IntRange range = itemEntry.getQuantityRange();
                int minimum = range != null ? range.getFirst() : itemEntry.getQuantity();
                int maximum = range != null ? range.getLast() : itemEntry.getQuantity();
                entries.add(new EntryView(itemEntry.getItem().toString(), minimum, maximum, itemEntry.getPercentage()));
            }
        }
        IntRange rolls = table.getAmount();
        return new TableView(spawnTable != null ? Source.SPAWN : Source.FORM, rolls.getFirst(), rolls.getLast(), entries);
    }

    /** Species name, with the form when it is not the standard one: "Vulpix (Alola)". */
    public static String describe(PokemonRef pokemonRef) {
        Pokemon pokemon = pokemonRef.pokemon();
        String species = pokemon.getSpecies().getName();
        String form = pokemon.getForm().getName();
        return STANDARD_FORM.equalsIgnoreCase(form) ? species : species + " (" + form + ")";
    }

    public static int friendship(PokemonRef pokemonRef) {
        return pokemonRef.pokemon().getFriendship();
    }
}
