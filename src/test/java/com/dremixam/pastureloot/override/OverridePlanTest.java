package com.dremixam.pastureloot.override;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverridePlanTest {

    private static final Set<String> NO_TAGS = Set.of();
    private static final DropSpec ICE = new DropSpec("minecraft:ice", 1, 2, 10);
    private static final DropSpec SNOW = new DropSpec("minecraft:snowball", 1, 1, DropSpec.ALWAYS);

    private static OverrideRule rule(OverrideRule.Mode mode, List<DropSpec> drops, List<String> items) {
        return new OverrideRule("vulpix", mode, drops, items, OverrideRule.NO_RATE_CHANGE, List.of(), List.of());
    }

    private static List<DropSpec> specs(OverridePlan plan) {
        return plan.extraDrops().stream().map(OverridePlan.PlannedDrop::spec).toList();
    }

    @Test
    void noRuleKeepsTheCobblemonTable() {
        OverridePlan plan = OverridePlan.of(List.of());
        assertTrue(plan.usesBaseTable());
        assertEquals(List.of(), plan.extraDrops());
        assertTrue(plan.keepsBaseItem("minecraft:charcoal", NO_TAGS));
    }

    @Test
    void addKeepsTheTableAndAppends() {
        OverridePlan plan = OverridePlan.of(List.of(rule(OverrideRule.Mode.ADD, List.of(ICE), List.of())));
        assertTrue(plan.usesBaseTable());
        assertEquals(List.of(ICE), specs(plan));
    }

    @Test
    void replaceForgetsEverythingBeforeIt() {
        OverridePlan plan = OverridePlan.of(List.of(
                rule(OverrideRule.Mode.ADD, List.of(ICE), List.of()),
                rule(OverrideRule.Mode.REPLACE, List.of(SNOW), List.of())));
        assertFalse(plan.usesBaseTable());
        assertEquals(List.of(SNOW), specs(plan));
    }

    @Test
    void removeOnlyTouchesWhatCameBeforeIt() {
        OverridePlan plan = OverridePlan.of(List.of(
                rule(OverrideRule.Mode.ADD, List.of(ICE), List.of()),
                rule(OverrideRule.Mode.REMOVE, List.of(), List.of("minecraft:ice", "minecraft:charcoal")),
                rule(OverrideRule.Mode.ADD, List.of(new DropSpec("minecraft:ice", 1, 1, DropSpec.ALWAYS)), List.of())));
        List<OverridePlan.PlannedDrop> drops = plan.extraDrops();
        assertFalse(plan.keeps(drops.get(0), NO_TAGS));
        assertTrue(plan.keeps(drops.get(1), NO_TAGS));
        assertFalse(plan.keepsBaseItem("minecraft:charcoal", NO_TAGS));
        assertTrue(plan.keepsBaseItem("minecraft:sweet_berries", NO_TAGS));
    }

    @Test
    void removeAcceptsTags() {
        OverridePlan plan = OverridePlan.of(List.of(rule(OverrideRule.Mode.REMOVE, List.of(), List.of("#c:foods"))));
        assertFalse(plan.keepsBaseItem("minecraft:sweet_berries", Set.of("c:foods")));
    }

    @Test
    void rateMultipliersAreMultiplied() {
        OverrideRule half = new OverrideRule("vulpix", OverrideRule.Mode.ADD, List.of(), List.of(), 0.5, List.of(), List.of());
        OverrideRule triple = new OverrideRule("vulpix", OverrideRule.Mode.ADD, List.of(), List.of(), 3, List.of(), List.of());
        assertEquals(1.5, OverridePlan.rateMultiplier(List.of(half, triple)));
        assertEquals(1, OverridePlan.rateMultiplier(List.of()));
    }
}
