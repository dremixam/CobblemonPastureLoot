package com.dremixam.pastureloot.override;

/** Text of the overrides file written on first start: explanations and examples, no active rule. */
final class OverridesExample {

    static final String TEXT = """
            # PastureLoot overrides: change what a pastured pokemon drops.
            #
            # Each [[override]] block is one rule. Every rule that matches a pokemon is used, in the order
            # of this file: "replace" starts again from nothing, "add" adds drops, "remove" takes items out
            # of what the rules above it gave.
            #
            # Keys of a rule:
            #   pokemon          Required. Pokemon properties, as in /pokegive: "vulpix", "vulpix alolan",
            #                    "ponyta galarian", "shiny=true", "type=fire"...
            #   mode             "add" (default), "replace" or "remove".
            #   drops            For "add" and "replace". A list of { item, quantity, chance }:
            #                      item      item id, "minecraft:snowball"
            #                      quantity  a number (2) or a range ("1-3"), 1 by default
            #                      chance    in percent, 100 by default; each drop is rolled on its own
            #   items            For "remove". Item ids or item tags with a # ("#c:foods/raw_meat").
            #   rate_multiplier  Multiplies the drop rate of this pokemon, 1 by default. 0 stops its drops.
            #   biomes           Optional. The pasture must stand in one of these biomes (ids or # tags).
            #   dimensions       Optional. The pasture must stand in one of these dimensions.
            #
            # The filter of pastureloot.toml applies to the drops of Cobblemon only, not to the drops
            # added here.
            #
            # Examples, remove the # at the start of the lines to use them:
            #
            # [[override]]
            # pokemon = "vulpix alolan"
            # drops = [
            #     { item = "minecraft:ice", quantity = "1-2", chance = 10 },
            # ]
            #
            # [[override]]
            # pokemon = "gastly"
            # biomes = ["#minecraft:is_nether"]
            # drops = [{ item = "minecraft:soul_sand", chance = 5 }]
            #
            # [[override]]
            # pokemon = "mareep"
            # mode = "replace"
            # drops = [{ item = "minecraft:white_wool", quantity = "1-2" }]
            #
            # [[override]]
            # pokemon = "farfetchd"
            # mode = "remove"
            # items = ["minecraft:feather"]
            #
            # [[override]]
            # pokemon = "mewtwo"
            # rate_multiplier = 0
            """;

    private OverridesExample() {
    }
}
