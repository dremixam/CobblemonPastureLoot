package com.dremixam.pastureloot.minecraft;

import com.dremixam.pastureloot.PastureLoot;
import com.dremixam.pastureloot.loot.ItemFilter;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge.PasturedPokemon;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge.RolledDrop;
import com.dremixam.pastureloot.override.DropSpec;
import com.dremixam.pastureloot.override.OverridePlan;
import com.dremixam.pastureloot.override.OverridePlan.PlannedDrop;
import com.dremixam.pastureloot.override.OverrideRule;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Set;

/** One drop of one pokémon: its Cobblemon table, then the drops added by the overrides. */
final class PastureDrops {

    /** Chances of override drops are written in percent. */
    private static final double PERCENT = 100;

    private PastureDrops() {
    }

    static void dropFor(PasturedPokemon pasturedPokemon, ServerLevel level, BlockPos pos, List<OverrideRule> rules) {
        try {
            OverridePlan plan = OverridePlan.of(rules);
            if (plan.usesBaseTable()) {
                dropBaseTable(pasturedPokemon, level, pos, plan);
            }
            for (PlannedDrop drop : plan.extraDrops()) {
                dropExtra(pasturedPokemon, level, plan, drop);
            }
        } catch (RuntimeException exception) {
            PastureLoot.LOGGER.error("Error while dropping loot", exception);
        }
    }

    private static void dropBaseTable(PasturedPokemon pasturedPokemon, ServerLevel level, BlockPos pos, OverridePlan plan) {
        ItemFilter filter = PastureLoot.itemFilter();
        for (RolledDrop drop : CobblemonBridge.rollDrops(pasturedPokemon, level, pos)) {
            Set<String> tags = ItemLookup.tagsOf(drop.itemId());
            if (filter.allows(drop.itemId(), tags) && plan.keepsBaseItem(drop.itemId(), tags)) {
                CobblemonBridge.drop(drop, pasturedPokemon, level);
                PastureLoot.LOGGER.debug("Dropped {} from {} at {}",
                        drop.itemId(), CobblemonBridge.speciesName(pasturedPokemon), pos);
            }
        }
    }

    private static void dropExtra(PasturedPokemon pasturedPokemon, ServerLevel level, OverridePlan plan, PlannedDrop drop) {
        DropSpec spec = drop.spec();
        if (level.random.nextDouble() * PERCENT >= spec.chance()) {
            return;
        }
        if (!plan.keeps(drop, ItemLookup.tagsOf(spec.item()))) {
            return;
        }
        int quantity = spec.minQuantity() + level.random.nextInt(spec.maxQuantity() - spec.minQuantity() + 1);
        if (quantity > 0) {
            ItemLookup.spawn(level, CobblemonBridge.position(pasturedPokemon), spec.item(), quantity);
        }
    }
}
