package com.dremixam.pastureloot.minecraft;

import com.dremixam.pastureloot.PastureLoot;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Finds items from their id, reads their tags and spawns them in the world. */
final class ItemLookup {

    private static final Set<String> REPORTED_UNKNOWN_ITEMS = new HashSet<>();

    private ItemLookup() {
    }

    /** Ids of the item tags of an item, without "#"; empty for an unknown item. */
    static Set<String> tagsOf(String itemId) {
        return holder(itemId)
                .map(item -> item.tags().map(tag -> tag.location().toString()).collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    /** Spawns the item at the position, split into stacks. An unknown id is reported once and skipped. */
    static void spawn(ServerLevel level, Vec3 position, String itemId, int quantity) {
        Optional<Holder.Reference<Item>> item = holder(itemId);
        if (item.isEmpty()) {
            if (REPORTED_UNKNOWN_ITEMS.add(itemId)) {
                PastureLoot.LOGGER.warn("Override drop \"{}\" is not a known item, it is skipped.", itemId);
            }
            return;
        }
        int left = quantity;
        while (left > 0) {
            ItemStack stack = new ItemStack(item.get(), Math.min(left, item.get().value().getDefaultMaxStackSize()));
            left -= stack.getCount();
            ItemEntity entity = new ItemEntity(level, position.x, position.y, position.z, stack);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
    }

    private static Optional<Holder.Reference<Item>> holder(String itemId) {
        ResourceLocation location = ResourceLocation.tryParse(itemId);
        return location == null ? Optional.empty() : BuiltInRegistries.ITEM.getHolder(location);
    }
}
