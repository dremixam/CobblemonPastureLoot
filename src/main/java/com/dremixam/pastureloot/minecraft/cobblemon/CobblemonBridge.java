package com.dremixam.pastureloot.minecraft.cobblemon;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.drop.DropEntry;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.drop.ItemDropEntry;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Entry point of the package that calls the Cobblemon API. */
public final class CobblemonBridge {

    private CobblemonBridge() {
    }

    /** Runs the action for each pokémon of the pasture that can drop now: not fainted and with a loaded entity. */
    public static void forEachDroppingPokemon(BlockEntity blockEntity, Consumer<PasturedPokemon> action) {
        if (!(blockEntity instanceof PokemonPastureBlockEntity pasture)) {
            return;
        }
        for (PokemonPastureBlockEntity.Tethering tethering : pasture.getTetheredPokemon()) {
            Pokemon pokemon = tethering.getPokemon();
            if (pokemon == null || pokemon.isFainted()) {
                continue;
            }
            PokemonEntity entity = pokemon.getEntity();
            if (entity != null) {
                action.accept(new PasturedPokemon(pokemon, entity));
            }
        }
    }

    /**
     * Rolls the drop table of the pokémon and keeps the item entries. The table of a spawn at the
     * place of the pasture wins over the table of the current form, as it would for a wild pokémon.
     */
    public static List<RolledDrop> rollDrops(PasturedPokemon pasturedPokemon, ServerLevel level, BlockPos pasturePos) {
        Pokemon pokemon = pasturedPokemon.pokemon();
        DropTable dropTable = SpawnDrops.at(pokemon, level, pasturePos);
        if (dropTable == null) {
            // Not species.getForm(aspects): it misses forms that need several aspects (Galarian Darmanitan).
            dropTable = pokemon.getForm().getDrops();
        }
        List<RolledDrop> rolled = new ArrayList<>();
        for (DropEntry entry : dropTable.getDrops(dropTable.getAmount(), pokemon)) {
            if (entry instanceof ItemDropEntry itemEntry) {
                rolled.add(new RolledDrop(itemEntry.getItem().toString(), itemEntry));
            }
        }
        return rolled;
    }

    /** Spawns the drop at the pokémon, with the quantity rules of Cobblemon. */
    public static void drop(RolledDrop drop, PasturedPokemon pasturedPokemon, ServerLevel level) {
        PokemonEntity entity = pasturedPokemon.entity();
        drop.entry().drop(entity, level, entity.position(), null);
    }

    /** How far from the pasture block its pokémon may wander, from the Cobblemon config. */
    public static int pastureWanderDistance() {
        return Cobblemon.INSTANCE.getConfig().getPastureMaxWanderDistance();
    }

    /** Whether the pokémon matches a Cobblemon property string, as written in /pokegive. */
    public static boolean matches(PasturedPokemon pasturedPokemon, String properties) {
        return PropertiesMatcher.matches(pasturedPokemon.pokemon(), properties);
    }

    public static Vec3 position(PasturedPokemon pasturedPokemon) {
        return pasturedPokemon.entity().position();
    }

    public static int friendship(PasturedPokemon pasturedPokemon) {
        return pasturedPokemon.pokemon().getFriendship();
    }

    public static String speciesName(PasturedPokemon pasturedPokemon) {
        return pasturedPokemon.pokemon().getSpecies().getName();
    }

    /** A pokémon tethered to a pasture, with its loaded entity. */
    public record PasturedPokemon(Pokemon pokemon, PokemonEntity entity) {
    }

    /** One item entry rolled from a drop table. */
    public record RolledDrop(String itemId, ItemDropEntry entry) {
    }
}
