package com.dremixam.pastureloot.minecraft;

import com.dremixam.pastureloot.PastureLoot;
import com.dremixam.pastureloot.config.Settings;
import com.dremixam.pastureloot.loot.DropLimit;
import com.dremixam.pastureloot.loot.DropRate;
import com.dremixam.pastureloot.loot.FriendshipBonus;
import com.dremixam.pastureloot.loot.ItemFilter;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge.PasturedPokemon;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge.RolledDrop;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.Set;
import java.util.stream.Collectors;

/** Runs once per tick for each pasture, after the ticker of Cobblemon. */
public final class PastureTicker {

    private PastureTicker() {
    }

    public static void tick(Level level, BlockPos pos, BlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Settings settings = PastureLoot.settings();
        CobblemonBridge.forEachDroppingPokemon(blockEntity, pasturedPokemon -> {
            double multiplier = FriendshipBonus.multiplier(
                    CobblemonBridge.friendship(pasturedPokemon), settings.friendshipMaxBonus());
            double chance = DropRate.chancePerTick(settings.dropsPerHour() * multiplier);
            if (serverLevel.random.nextDouble() < chance && roomForItems(serverLevel, pos, settings)) {
                dropFor(pasturedPokemon, serverLevel, PastureLoot.itemFilter(), pos);
            }
        });
    }

    private static boolean roomForItems(ServerLevel level, BlockPos pos, Settings settings) {
        if (settings.maxItemsNearby() <= 0) {
            return true;
        }
        AABB area = new AABB(pos).inflate(CobblemonBridge.pastureWanderDistance());
        int itemsNearby = level.getEntitiesOfClass(ItemEntity.class, area).size();
        return DropLimit.allowsDrop(itemsNearby, settings.maxItemsNearby());
    }

    private static void dropFor(PasturedPokemon pasturedPokemon, ServerLevel level, ItemFilter filter, BlockPos pos) {
        try {
            for (RolledDrop drop : CobblemonBridge.rollDrops(pasturedPokemon, level, pos)) {
                if (filter.allows(drop.itemId(), tagsOf(drop.itemId()))) {
                    CobblemonBridge.drop(drop, pasturedPokemon, level);
                    PastureLoot.LOGGER.debug("Dropped {} from {} at {}",
                            drop.itemId(), CobblemonBridge.speciesName(pasturedPokemon), pos);
                }
            }
        } catch (RuntimeException exception) {
            PastureLoot.LOGGER.error("Error while dropping loot", exception);
        }
    }

    /** Ids of the item tags of an item, without "#"; empty for an unknown item. */
    private static Set<String> tagsOf(String itemId) {
        ResourceLocation location = ResourceLocation.tryParse(itemId);
        if (location == null) {
            return Set.of();
        }
        return BuiltInRegistries.ITEM.getHolder(location)
                .map(holder -> holder.tags().map(tag -> tag.location().toString()).collect(Collectors.toSet()))
                .orElse(Set.of());
    }
}
