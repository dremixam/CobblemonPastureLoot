package com.dremixam.pastureloot.override;

import com.dremixam.pastureloot.loot.ItemFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * What the rules matching one pokémon do to its drops, applied in the order of the file: a replace
 * starts again from nothing, an add appends, a remove takes items out of what came before it.
 */
public final class OverridePlan {

    private boolean usesBaseTable = true;
    private final List<PlannedDrop> extraDrops = new ArrayList<>();
    private final List<ItemFilter> removals = new ArrayList<>();

    /** A drop added by a rule, with the index of the first removal that comes after it. */
    public record PlannedDrop(DropSpec spec, int firstRemoval) {
    }

    private OverridePlan() {
    }

    public static OverridePlan of(List<OverrideRule> matchingRules) {
        OverridePlan plan = new OverridePlan();
        for (OverrideRule rule : matchingRules) {
            switch (rule.mode()) {
                case REPLACE -> {
                    plan.usesBaseTable = false;
                    plan.extraDrops.clear();
                    plan.add(rule.drops());
                }
                case ADD -> plan.add(rule.drops());
                case REMOVE -> plan.removals.add(new ItemFilter(rule.removedItems(), ItemFilter.Mode.BLACKLIST));
            }
        }
        return plan;
    }

    /** Product of the rate multipliers of the rules. */
    public static double rateMultiplier(List<OverrideRule> matchingRules) {
        double multiplier = 1;
        for (OverrideRule rule : matchingRules) {
            multiplier *= rule.rateMultiplier();
        }
        return multiplier;
    }

    private void add(List<DropSpec> drops) {
        for (DropSpec drop : drops) {
            extraDrops.add(new PlannedDrop(drop, removals.size()));
        }
    }

    /** Whether the drop table of Cobblemon is still rolled. */
    public boolean usesBaseTable() {
        return usesBaseTable;
    }

    public List<PlannedDrop> extraDrops() {
        return List.copyOf(extraDrops);
    }

    /** Whether an item rolled from the Cobblemon table survives the removals. */
    public boolean keepsBaseItem(String itemId, Collection<String> itemTags) {
        return removals.stream().allMatch(removal -> removal.allows(itemId, itemTags));
    }

    /** Whether an added drop survives the removals that come after it. */
    public boolean keeps(PlannedDrop drop, Collection<String> itemTags) {
        return removals.subList(drop.firstRemoval(), removals.size()).stream()
                .allMatch(removal -> removal.allows(drop.spec().item(), itemTags));
    }
}
