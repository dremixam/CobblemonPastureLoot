package com.dremixam.pastureloot.minecraft;

import com.dremixam.pastureloot.Config;
import com.dremixam.pastureloot.PastureLoot;
import com.dremixam.pastureloot.loot.DropRate;
import com.dremixam.pastureloot.loot.ItemFilter;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge.PasturedPokemon;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge.RolledDrop;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Runs once per tick for each pasture, after the ticker of Cobblemon. */
public final class PastureTicker {

    private PastureTicker() {
    }

    public static void tick(Level level, BlockPos pos, BlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Config config = PastureLoot.config();
        double chance = DropRate.chancePerTick(config.dropsPerHour());
        CobblemonBridge.forEachDroppingPokemon(blockEntity, pasturedPokemon -> {
            if (serverLevel.random.nextDouble() < chance) {
                dropFor(pasturedPokemon, serverLevel, config.itemFilter(), pos);
            }
        });
    }

    private static void dropFor(PasturedPokemon pasturedPokemon, ServerLevel level, ItemFilter filter, BlockPos pos) {
        try {
            for (RolledDrop drop : CobblemonBridge.rollDrops(pasturedPokemon, level, pos)) {
                if (filter.allows(drop.itemId())) {
                    CobblemonBridge.drop(drop, pasturedPokemon, level);
                    PastureLoot.LOGGER.debug("Dropped {} from {} at {}",
                            drop.itemId(), CobblemonBridge.speciesName(pasturedPokemon), pos);
                }
            }
        } catch (RuntimeException exception) {
            PastureLoot.LOGGER.error("Error while dropping loot", exception);
        }
    }
}
