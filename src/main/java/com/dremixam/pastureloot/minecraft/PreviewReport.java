package com.dremixam.pastureloot.minecraft;

import com.dremixam.pastureloot.PastureLoot;
import com.dremixam.pastureloot.config.Settings;
import com.dremixam.pastureloot.loot.DropRate;
import com.dremixam.pastureloot.loot.FriendshipBonus;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge.PokemonRef;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonPreview;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonPreview.EntryView;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonPreview.TableView;
import com.dremixam.pastureloot.override.DropSpec;
import com.dremixam.pastureloot.override.OverridePlan;
import com.dremixam.pastureloot.override.OverridePlan.PlannedDrop;
import com.dremixam.pastureloot.override.OverrideRule;
import com.dremixam.pastureloot.override.Place;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Chat lines of /pastureloot preview: rate, place, Cobblemon table and override drops of one pokémon. */
final class PreviewReport {

    private final PokemonRef pokemon;
    private final ServerLevel level;
    private final BlockPos place;
    private final Settings settings = PastureLoot.settings();
    private final List<Component> lines = new ArrayList<>();

    private PreviewReport(PokemonRef pokemon, ServerLevel level, BlockPos place) {
        this.pokemon = pokemon;
        this.level = level;
        this.place = place;
    }

    /** Lines for a pokémon; realFriendship tells whether its friendship is its own or a new pokémon's. */
    static List<Component> of(PokemonRef pokemon, ServerLevel level, BlockPos place, String placeLabel, boolean realFriendship) {
        PreviewReport report = new PreviewReport(pokemon, level, place);
        OverrideMatcher matcher = new OverrideMatcher(level, place);
        List<OverrideRule> rules = matcher.matching(pokemon, PastureLoot.overrides());
        OverridePlan plan = OverridePlan.of(rules);
        report.header(matcher.place(), placeLabel, rules.size());
        report.rate(OverridePlan.rateMultiplier(rules), realFriendship);
        report.baseTable(plan);
        report.extraDrops(plan);
        return report.lines;
    }

    private void header(Place where, String placeLabel, int matchingRules) {
        lines.add(Component.literal("PastureLoot: " + CobblemonPreview.describe(pokemon)).withStyle(ChatFormatting.GOLD));
        lines.add(Component.literal(placeLabel + ": " + where.biomeId() + ", " + where.dimensionId()
                + " | override rules matching: " + matchingRules).withStyle(ChatFormatting.GRAY));
    }

    private void rate(double overrideMultiplier, boolean realFriendship) {
        String overrideText = overrideMultiplier == OverrideRule.NO_RATE_CHANGE ? "" : ", x" + number(overrideMultiplier) + " from overrides";
        if (realFriendship) {
            int friendship = CobblemonPreview.friendship(pokemon);
            double rate = DropRate.clamp(PastureTicker.dropsPerHour(friendship, overrideMultiplier, settings));
            lines.add(Component.literal("Rate: " + number(rate) + " drops per hour (friendship " + friendship + overrideText + ")"));
            return;
        }
        double lowest = DropRate.clamp(PastureTicker.dropsPerHour(0, overrideMultiplier, settings));
        double highest = DropRate.clamp(PastureTicker.dropsPerHour(FriendshipBonus.MAX_FRIENDSHIP, overrideMultiplier, settings));
        lines.add(Component.literal("Rate: " + number(lowest) + " to " + number(highest)
                + " drops per hour, from friendship 0 to " + FriendshipBonus.MAX_FRIENDSHIP + overrideText));
    }

    private void baseTable(OverridePlan plan) {
        if (!plan.usesBaseTable()) {
            lines.add(Component.literal("Cobblemon table: replaced by an override").withStyle(ChatFormatting.GRAY));
            return;
        }
        TableView table = CobblemonPreview.table(pokemon, level, place);
        String source = table.source() == CobblemonPreview.Source.SPAWN ? "spawn of this place" : "form";
        String rolls = table.minRolls() == table.maxRolls() ? String.valueOf(table.minRolls()) : table.minRolls() + "-" + table.maxRolls();
        lines.add(Component.literal("Cobblemon table (" + source + ", " + rolls + " rolls):"));
        for (EntryView entry : table.entries()) {
            Set<String> tags = ItemLookup.tagsOf(entry.itemId());
            String refusal = !PastureLoot.itemFilter().allows(entry.itemId(), tags) ? "filtered by pastureloot.toml"
                    : !plan.keepsBaseItem(entry.itemId(), tags) ? "removed by an override" : null;
            lines.add(entryLine("-", entry.itemId(), entry.minQuantity(), entry.maxQuantity(), entry.percentage(), refusal));
        }
    }

    private void extraDrops(OverridePlan plan) {
        if (plan.extraDrops().isEmpty()) {
            return;
        }
        lines.add(Component.literal("Added by overrides (each one rolled on its own):"));
        for (PlannedDrop drop : plan.extraDrops()) {
            DropSpec spec = drop.spec();
            String refusal = plan.keeps(drop, ItemLookup.tagsOf(spec.item())) ? null : "removed by a later override";
            lines.add(entryLine("+", spec.item(), spec.minQuantity(), spec.maxQuantity(), spec.chance(), refusal));
        }
    }

    private static Component entryLine(String mark, String item, int minimum, int maximum, double chance, String refusal) {
        String quantity = minimum == maximum ? "x" + minimum : "x" + minimum + "-" + maximum;
        MutableComponent line = Component.literal(" " + mark + " " + item + " " + quantity + " (" + number(chance) + "%)");
        if (refusal == null) {
            return line.withStyle(ChatFormatting.GREEN);
        }
        return line.withStyle(ChatFormatting.RED, ChatFormatting.STRIKETHROUGH)
                .append(Component.literal(" " + refusal).withStyle(style -> style.withStrikethrough(false)));
    }

    private static String number(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.2f", value);
    }
}
