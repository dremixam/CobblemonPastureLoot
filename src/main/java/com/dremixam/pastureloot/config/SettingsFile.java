package com.dremixam.pastureloot.config;

import com.dremixam.pastureloot.loot.DropRate;
import com.dremixam.pastureloot.loot.ItemFilter;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Reads and writes the main settings file, pastureloot.toml. */
public final class SettingsFile {

    public static final String FILE_NAME = "pastureloot.toml";

    static final String DROPS_PER_HOUR = "drops.per_hour";
    static final String FILTER_MODE = "filter.mode";
    static final String FILTER_ITEMS = "filter.items";
    static final String FRIENDSHIP_MAX_BONUS = "friendship.max_bonus";
    static final String MAX_ITEMS_NEARBY = "anti_lag.max_items_nearby";

    private SettingsFile() {
    }

    /**
     * Reads the file, or writes it with the default values when it does not exist. A value that is
     * missing, of the wrong type or out of bounds takes its default or its bound, with a warning.
     */
    public static Settings load(Path file, Consumer<String> warnings) throws IOException {
        if (Files.notExists(file)) {
            write(file, Settings.DEFAULTS);
            return Settings.DEFAULTS;
        }
        CommentedConfig config;
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            config = new TomlParser().parse(reader);
        } catch (RuntimeException exception) {
            warnings.accept("Cannot read " + file + ", default settings are used: " + exception.getMessage());
            return Settings.DEFAULTS;
        }
        return read(new ValueReader(config, warnings));
    }

    private static Settings read(ValueReader values) {
        return new Settings(
                values.number(DROPS_PER_HOUR, Settings.DEFAULT_DROPS_PER_HOUR, 0, DropRate.MAX_DROPS_PER_HOUR),
                values.strings(FILTER_ITEMS, Settings.DEFAULT_FILTER_ITEMS),
                values.mode(FILTER_MODE, ItemFilter.Mode.BLACKLIST),
                values.number(FRIENDSHIP_MAX_BONUS, Settings.DEFAULT_FRIENDSHIP_MAX_BONUS,
                        0, Settings.MAX_FRIENDSHIP_MAX_BONUS),
                (int) values.number(MAX_ITEMS_NEARBY, Settings.DEFAULT_MAX_ITEMS_NEARBY,
                        0, Settings.MAX_ITEMS_NEARBY_LIMIT));
    }

    /** Writes the settings with a comment on every value, replacing the file. */
    public static void write(Path file, Settings settings) throws IOException {
        CommentedConfig config = CommentedConfig.of(LinkedHashMap::new, TomlFormat.instance());
        config.set(DROPS_PER_HOUR, settings.dropsPerHour());
        config.setComment(DROPS_PER_HOUR, comment(
                "Average number of drops per hour, for each pokemon in a pasture (game time, chunk loaded).",
                "One drop is one roll of the pokemon drop table, it can give several items.",
                "0 disables drops. Maximum 3600, one drop per second."));
        config.set(FILTER_MODE, settings.filterMode().name().toLowerCase(Locale.ROOT));
        config.setComment(FILTER_MODE, comment(
                "\"blacklist\": the items below are never dropped.",
                "\"whitelist\": only the items below are dropped."));
        config.set(FILTER_ITEMS, new ArrayList<>(settings.filterItems()));
        config.setComment(FILTER_ITEMS, comment(
                "Item ids (\"minecraft:bone\") or item tags with a # (\"#c:foods/raw_meat\")."));
        config.set(FRIENDSHIP_MAX_BONUS, settings.friendshipMaxBonus());
        config.setComment(FRIENDSHIP_MAX_BONUS, comment(
                "Extra drop rate at maximum friendship (255), growing with friendship from 0.",
                "1.0 means twice the rate at 255. 0 disables the bonus. Maximum 10."));
        config.set(MAX_ITEMS_NEARBY, settings.maxItemsNearby());
        config.setComment(MAX_ITEMS_NEARBY, comment(
                "A pasture stops dropping while this many item entities or more lie in the area where",
                "its pokemon wander (pastureMaxWanderDistance in the Cobblemon config). 0 disables the limit."));
        Files.createDirectories(file.toAbsolutePath().getParent());
        TomlWriter tomlWriter = new TomlWriter();
        tomlWriter.setIndentArrayElementsPredicate(array -> !array.isEmpty());
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            tomlWriter.write(config, writer);
        }
    }

    /** One comment line per argument, each one after "# ". */
    private static String comment(String... lines) {
        return " " + String.join("\n ", lines);
    }

    /** Reads typed values from a parsed file and reports what it had to fix. */
    private record ValueReader(CommentedConfig config, Consumer<String> warnings) {

        double number(String path, double defaultValue, double minimum, double maximum) {
            Object value = config.get(path);
            if (value == null) {
                return defaultValue;
            }
            if (!(value instanceof Number number) || Double.isNaN(number.doubleValue())) {
                warnings.accept(path + " must be a number, " + defaultValue + " is used.");
                return defaultValue;
            }
            double read = number.doubleValue();
            double clamped = Math.max(minimum, Math.min(maximum, read));
            if (clamped != read) {
                warnings.accept(path + " must be between " + minimum + " and " + maximum + ", " + clamped + " is used.");
            }
            return clamped;
        }

        List<String> strings(String path, List<String> defaultValue) {
            Object value = config.get(path);
            if (value == null) {
                return defaultValue;
            }
            if (!(value instanceof List<?> list) || !list.stream().allMatch(String.class::isInstance)) {
                warnings.accept(path + " must be a list of strings, the default list is used.");
                return defaultValue;
            }
            return list.stream().map(String.class::cast).toList();
        }

        ItemFilter.Mode mode(String path, ItemFilter.Mode defaultValue) {
            Object value = config.get(path);
            if (value == null) {
                return defaultValue;
            }
            for (ItemFilter.Mode mode : ItemFilter.Mode.values()) {
                if (mode.name().equalsIgnoreCase(String.valueOf(value).trim())) {
                    return mode;
                }
            }
            warnings.accept(path + " must be \"blacklist\" or \"whitelist\", \""
                    + defaultValue.name().toLowerCase(Locale.ROOT) + "\" is used.");
            return defaultValue;
        }
    }
}
