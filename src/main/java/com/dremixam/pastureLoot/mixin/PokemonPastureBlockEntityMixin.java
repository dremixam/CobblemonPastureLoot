package com.dremixam.pastureLoot.mixin;

import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.dremixam.pastureLoot.PastureLoot;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mixin(PokemonPastureBlockEntity.class)
public class PokemonPastureBlockEntityMixin {

    private static final Map<String, JsonArray> lootTable = new HashMap<>();

    static {
        try {
            InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(PastureLoot.class.getResourceAsStream("/loot_tables.json")));

            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray entries = jsonObject.getAsJsonArray("entries");
            for (int i = 0; i < entries.size(); i++) {
                JsonObject entry = entries.get(i).getAsJsonObject();
                String id = entry.get("id").getAsString();
                JsonArray drops = entry.getAsJsonArray("drops");
                lootTable.put(id, drops);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(at = @At("HEAD"), method = "TICKER$lambda$14")
    private static void init(Level world, BlockPos pos, BlockState state, PokemonPastureBlockEntity blockEntity, CallbackInfo ci) {
        if (world.isClientSide) return;

        // De ce que je comprends ici on est dans un random tick sur le blockentity d'un pasture et on a acces a la liste des pokémon tethered

        blockEntity.getTetheredPokemon().forEach(tethering -> {
            Pokemon pokemon = tethering.getPokemon();

            if (pokemon != null && !pokemon.isFainted()) {
                double randomNumber = Math.random();

                DropTable drops = pokemon.getSpecies().getDrops();




            }

        });


    }
}
