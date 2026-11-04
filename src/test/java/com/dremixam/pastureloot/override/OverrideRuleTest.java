package com.dremixam.pastureloot.override;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverrideRuleTest {

    private static final Place NETHER_WASTES =
            new Place("minecraft:nether_wastes", Set.of("minecraft:is_nether"), "minecraft:the_nether");
    private static final Place PLAINS =
            new Place("minecraft:plains", Set.of("minecraft:is_overworld"), "minecraft:overworld");

    private static OverrideRule placed(List<String> biomes, List<String> dimensions) {
        return new OverrideRule("gastly", OverrideRule.Mode.ADD, List.of(), List.of(),
                OverrideRule.NO_RATE_CHANGE, biomes, dimensions);
    }

    @Test
    void ruleWithoutPlaceMatchesEverywhere() {
        OverrideRule rule = placed(List.of(), List.of());
        assertFalse(rule.hasPlace());
        assertTrue(rule.placeMatches(PLAINS));
    }

    @Test
    void biomeTagsAndIdsAreBothAccepted() {
        assertTrue(placed(List.of("#minecraft:is_nether"), List.of()).placeMatches(NETHER_WASTES));
        assertTrue(placed(List.of("minecraft:plains"), List.of()).placeMatches(PLAINS));
        assertFalse(placed(List.of("#minecraft:is_nether"), List.of()).placeMatches(PLAINS));
    }

    @Test
    void biomesAndDimensionsMustBothFit() {
        OverrideRule rule = placed(List.of("#minecraft:is_nether"), List.of("minecraft:overworld"));
        assertFalse(rule.placeMatches(NETHER_WASTES));
        assertTrue(placed(List.of(), List.of("minecraft:the_nether")).placeMatches(NETHER_WASTES));
    }
}
