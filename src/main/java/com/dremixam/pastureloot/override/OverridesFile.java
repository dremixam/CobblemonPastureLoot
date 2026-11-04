package com.dremixam.pastureloot.override;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** Reads the overrides file, pastureloot-overrides.toml. A broken rule is skipped with a warning. */
public final class OverridesFile {

    public static final String FILE_NAME = "pastureloot-overrides.toml";
    /** Highest rate multiplier of a rule, already far beyond what a pasture needs. */
    public static final double MAX_RATE_MULTIPLIER = 100;

    private static final Set<String> RULE_KEYS =
            Set.of("pokemon", "mode", "drops", "items", "rate_multiplier", "biomes", "dimensions");
    private static final Set<String> DROP_KEYS = Set.of("item", "quantity", "chance");

    private OverridesFile() {
    }

    /** Reads the rules in file order, or writes the example file and returns no rule when it does not exist. */
    public static List<OverrideRule> load(Path file, Consumer<String> warnings) throws IOException {
        if (Files.notExists(file)) {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Files.writeString(file, OverridesExample.TEXT, StandardCharsets.UTF_8);
            return List.of();
        }
        UnmodifiableConfig config;
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            config = new TomlParser().parse(reader);
        } catch (RuntimeException exception) {
            warnings.accept("Cannot read " + file + ", no override is used: " + exception.getMessage());
            return List.of();
        }
        Object rules = config.get("override");
        if (rules == null) {
            return List.of();
        }
        if (!(rules instanceof List<?> list)) {
            warnings.accept("override must be written [[override]], no override is used.");
            return List.of();
        }
        List<OverrideRule> read = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            OverrideRule rule = readRule(list.get(index), "override #" + (index + 1), warnings);
            if (rule != null) {
                read.add(rule);
            }
        }
        return read;
    }

    private static OverrideRule readRule(Object value, String label, Consumer<String> warnings) {
        if (!(value instanceof UnmodifiableConfig rule)) {
            warnings.accept(label + " is not a table, it is skipped.");
            return null;
        }
        Values values = new Values(rule, label, warnings);
        values.warnUnknownKeys(RULE_KEYS);
        String pokemon = rule.get("pokemon") instanceof String text ? text.trim() : "";
        if (pokemon.isEmpty()) {
            warnings.accept(label + " has no \"pokemon\", it is skipped.");
            return null;
        }
        Values named = new Values(rule, label + " (" + pokemon + ")", warnings);
        OverrideRule.Mode mode = named.mode();
        if (mode == null) {
            return null;
        }
        List<String> removedItems = named.strings("items");
        if (mode == OverrideRule.Mode.REMOVE && removedItems.isEmpty()) {
            warnings.accept(named.label() + " has mode \"remove\" but no \"items\", it removes nothing.");
        }
        return new OverrideRule(pokemon, mode, named.drops(), removedItems,
                named.number("rate_multiplier", OverrideRule.NO_RATE_CHANGE, 0, MAX_RATE_MULTIPLIER),
                named.strings("biomes"), named.strings("dimensions"));
    }

    /** Typed values of one rule, with warnings that name the rule. */
    private record Values(UnmodifiableConfig config, String label, Consumer<String> warnings) {

        void warnUnknownKeys(Set<String> known) {
            for (UnmodifiableConfig.Entry entry : config.entrySet()) {
                if (!known.contains(entry.getKey())) {
                    warnings.accept(label + ": unknown key \"" + entry.getKey() + "\" is ignored.");
                }
            }
        }

        OverrideRule.Mode mode() {
            Object value = config.get("mode");
            if (value == null) {
                return OverrideRule.Mode.ADD;
            }
            for (OverrideRule.Mode mode : OverrideRule.Mode.values()) {
                if (mode.name().equalsIgnoreCase(String.valueOf(value).trim())) {
                    return mode;
                }
            }
            warnings.accept(label + ": mode must be \"replace\", \"add\" or \"remove\", the rule is skipped.");
            return null;
        }

        double number(String key, double defaultValue, double minimum, double maximum) {
            Object value = config.get(key);
            if (value == null) {
                return defaultValue;
            }
            if (!(value instanceof Number number) || Double.isNaN(number.doubleValue())) {
                warnings.accept(label + ": " + key + " must be a number, " + defaultValue + " is used.");
                return defaultValue;
            }
            double clamped = Math.max(minimum, Math.min(maximum, number.doubleValue()));
            if (clamped != number.doubleValue()) {
                warnings.accept(label + ": " + key + " must be between " + minimum + " and " + maximum
                        + ", " + clamped + " is used.");
            }
            return clamped;
        }

        List<String> strings(String key) {
            Object value = config.get(key);
            if (value == null) {
                return List.of();
            }
            if (!(value instanceof List<?> list) || !list.stream().allMatch(String.class::isInstance)) {
                warnings.accept(label + ": " + key + " must be a list of strings, it is ignored.");
                return List.of();
            }
            return list.stream().map(text -> ((String) text).trim()).filter(text -> !text.isEmpty()).toList();
        }

        List<DropSpec> drops() {
            Object value = config.get("drops");
            if (value == null) {
                return List.of();
            }
            if (!(value instanceof List<?> list)) {
                warnings.accept(label + ": drops must be a list, it is ignored.");
                return List.of();
            }
            List<DropSpec> drops = new ArrayList<>();
            for (int index = 0; index < list.size(); index++) {
                DropSpec drop = drop(list.get(index), label + ", drop #" + (index + 1));
                if (drop != null) {
                    drops.add(drop);
                }
            }
            return drops;
        }

        private DropSpec drop(Object value, String dropLabel) {
            if (!(value instanceof UnmodifiableConfig drop)) {
                warnings.accept(dropLabel + " is not a table like { item = \"minecraft:stick\" }, it is skipped.");
                return null;
            }
            Values values = new Values(drop, dropLabel, warnings);
            values.warnUnknownKeys(DROP_KEYS);
            if (!(drop.get("item") instanceof String item) || item.isBlank()) {
                warnings.accept(dropLabel + " has no \"item\", it is skipped.");
                return null;
            }
            int[] quantity = values.quantity();
            if (quantity == null) {
                return null;
            }
            double chance = values.number("chance", DropSpec.ALWAYS, 0, DropSpec.ALWAYS);
            return new DropSpec(item.trim(), quantity[0], quantity[1], chance);
        }

        /** "quantity" as {min, max}: a number, or a range written "1-3". Null when it cannot be read. */
        private int[] quantity() {
            Object value = config.get("quantity");
            if (value == null) {
                return new int[]{1, 1};
            }
            int[] range = QuantityRange.parse(value);
            if (range == null) {
                warnings.accept(label + ": quantity must be a number or a range like \"1-3\", the drop is skipped.");
            }
            return range;
        }
    }
}
