package com.dremixam.pastureloot.minecraft;

import com.dremixam.pastureloot.PastureLoot;
import com.dremixam.pastureloot.config.Settings;
import com.dremixam.pastureloot.loot.DropLimit;
import com.dremixam.pastureloot.loot.DropRate;
import com.dremixam.pastureloot.loot.FriendshipBonus;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge.PasturedPokemon;
import com.dremixam.pastureloot.override.OverridePlan;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/** Runs once per tick for each pasture, after the ticker of Cobblemon. */
public final class PastureTicker {

    private PastureTicker() {
    }

    public static void tick(Level level, BlockPos pos, BlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Settings settings = PastureLoot.settings();
        OverrideMatcher matcher = new OverrideMatcher(serverLevel, pos);
        CobblemonBridge.forEachDroppingPokemon(blockEntity, pasturedPokemon -> {
            double chance = DropRate.chancePerTick(dropsPerHour(pasturedPokemon, settings, matcher));
            if (serverLevel.random.nextDouble() < chance && roomForItems(serverLevel, pos, settings)) {
                PastureDrops.dropFor(pasturedPokemon, serverLevel, pos,
                        matcher.matching(pasturedPokemon.ref(), PastureLoot.overrides()));
            }
        });
    }

    private static double dropsPerHour(PasturedPokemon pasturedPokemon, Settings settings, OverrideMatcher matcher) {
        double overrides = OverridePlan.rateMultiplier(
                matcher.matching(pasturedPokemon.ref(), PastureLoot.rateOverrides()));
        return dropsPerHour(CobblemonBridge.friendship(pasturedPokemon), overrides, settings);
    }

    /** Drops per hour of one pokémon, before the cap of DropRate. */
    static double dropsPerHour(int friendship, double overrideMultiplier, Settings settings) {
        double friendshipMultiplier = FriendshipBonus.multiplier(friendship, settings.friendshipMaxBonus());
        return settings.dropsPerHour() * friendshipMultiplier * overrideMultiplier;
    }

    private static boolean roomForItems(ServerLevel level, BlockPos pos, Settings settings) {
        if (settings.maxItemsNearby() <= 0) {
            return true;
        }
        AABB area = new AABB(pos).inflate(CobblemonBridge.pastureWanderDistance());
        int itemsNearby = level.getEntitiesOfClass(ItemEntity.class, area).size();
        return DropLimit.allowsDrop(itemsNearby, settings.maxItemsNearby());
    }
}
