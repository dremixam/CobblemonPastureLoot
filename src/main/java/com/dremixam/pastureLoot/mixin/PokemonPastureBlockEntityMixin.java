package com.dremixam.pastureLoot.mixin;

import com.cobblemon.mod.common.api.drop.DropEntry;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.drop.ItemDropEntry;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.dremixam.pastureLoot.Config;
import com.dremixam.pastureLoot.PastureLoot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(PokemonPastureBlockEntity.class)
public abstract class PokemonPastureBlockEntityMixin {

    @Unique
    private static final Logger LOGGER = PastureLoot.LOGGER;

    @Unique
    private static Config getConfig() {
        return PastureLoot.INSTANCE.getConfig();
    }

    @Unique
    private static void processDropEntry(DropEntry drop, PokemonEntity entity, ServerLevel serverWorld, Pokemon pokemon, BlockPos pos) {
        if (drop instanceof ItemDropEntry itemDropEntry) {
            if (!Arrays.asList(getConfig().getItemBlacklist()).contains(itemDropEntry.getItem().toString())) {
                // Delegate to Cobblemon to preserve quantity range and conditions.
                itemDropEntry.drop(entity, serverWorld, entity.position(), null);
                LOGGER.debug(
                        "Dropped {} from {} with quantityRange={} at {}",
                        itemDropEntry.getItem(),
                        pokemon.getSpecies().getName(),
                        itemDropEntry.getQuantityRange(),
                        pos
                );
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "TICKER$lambda$0")
    private static void tick(Level world, BlockPos pos, BlockState state, PokemonPastureBlockEntity blockEntity, CallbackInfo ci) {
        if (world.isClientSide) return;

        blockEntity.getTetheredPokemon().forEach(tethering -> {
            double randomNumber = Math.random();

            if (randomNumber < getConfig().getDropChancePerTick()) {

                Pokemon pokemon = tethering.getPokemon();

                if (pokemon != null && !pokemon.isFainted()) {

                    PokemonEntity entity = pokemon.getEntity();

                    // If pokemon is not currently loaded, we can't drop loot
                    if (entity == null) return;

                    try {
                        FormData form = pokemon.getSpecies().getForm(pokemon.getAspects());
                        DropTable dropTable = form.getDrops();

                        // Roll Cobblemon's full drop result and emit each entry.
                        List<DropEntry> drops = dropTable.getDrops(dropTable.getAmount(), pokemon);
                        if (drops.isEmpty()) return;

                        ServerLevel serverWorld = (ServerLevel) world;
                        for (DropEntry drop : drops) {
                            processDropEntry(drop, entity, serverWorld, pokemon, pos);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error while dropping loot", e);
                    }

                }
            }
        });
    }
}
