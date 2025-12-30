package com.dremixam.pastureLoot.mixin;

import com.cobblemon.mod.common.api.drop.DropEntry;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.drop.ItemDropEntry;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.FormData;
import com.dremixam.pastureLoot.Config;
import com.dremixam.pastureLoot.PastureLoot;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(PokemonPastureBlockEntity.class)
public abstract class PokemonPastureBlockEntityMixin implements WorldlyContainer {

    @Unique
    private static final Logger LOGGER = PastureLoot.LOGGER;

    @Unique
    private static Config getConfig() {
        return PastureLoot.INSTANCE.getConfig();
    }

    @Inject(at = @At("HEAD"), method = "TICKER$lambda$0")
    private static void tick(Level world, BlockPos pos, BlockState state, PokemonPastureBlockEntity blockEntity, CallbackInfo ci) {
        if (world.isClientSide) return;

        // Executed on each tick of the block entity

        blockEntity.getTetheredPokemon().forEach(tethering -> {
            double randomNumber = Math.random();

            if (randomNumber < getConfig().getDropChancePerTick()) {

                Pokemon pokemon = tethering.getPokemon();

                if (pokemon != null && !pokemon.isFainted()) {

                    PokemonEntity entity = pokemon.getEntity();

                    // If pokemon is not currently loaded, we can't drop loot
                    if (entity == null) return;

                    try {
                        // Different forms can have different drop tables
                        // Example: Gimmighoul normally drops 24-48 cobblemoin:relic_coin, roaming form drops 1 cobblemoin:relic_coin
                        // Another example: Voltorb [Hisuian] drops Red Apricorn 0-1, Voltorb doesn't
                        FormData form = pokemon.getForm();
                        DropTable dropTable = form.getDrops();

                        // Roll Cobblemon's drop table to get the resulting DropEntry list for this tick (honours percentages/constraints).
                        List<DropEntry> drops = dropTable.getDrops(dropTable.getAmount(), pokemon);
                        ServerLevel serverWorld = (ServerLevel) world;

                        // take one random element drop in rolled drops List
                        if (drops.isEmpty()) return;

                        DropEntry drop = drops.get(serverWorld.random.nextInt(drops.size()));

                        if (drop instanceof ItemDropEntry itemDropEntry) {

                            if (!Arrays.asList(getConfig().getItemBlacklist()).contains(itemDropEntry.getItem().toString())) {
                                if (getConfig().legacyFlattenItemQuantity()) {
                                    Item item = world.registryAccess().registryOrThrow(Registries.ITEM).get(itemDropEntry.getItem());
                                    // legacy behaviour: always 1 item even if the drop has a quantity > 1
                                    if (item != null) {
                                        ItemStack stack = new ItemStack(item, 1);

                                        world.addFreshEntity(new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), stack));

                                        LOGGER.debug("Dropped " + stack + " from " + pokemon.getSpecies().getName() + " at " + pos);
                                    }

                                } else {
                                    // new behaviour: delegate to cobblemon, honouring quantity range

                                    itemDropEntry.drop(entity, serverWorld, entity.position(), null);
                                    LOGGER.debug("Dropping {} from {} with quantityRange={} at {}", itemDropEntry.getItem(), pokemon.getSpecies().getName(), itemDropEntry.getQuantityRange(), pos);
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error while dropping loot", e);
                    }

                }
            }
        });


    }
}
