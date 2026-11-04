package com.dremixam.pastureloot.config;

import com.dremixam.pastureloot.loot.DropRate;
import com.dremixam.pastureloot.loot.ItemFilter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Turns the PastureLoot.json file of the 1.0.x versions into pastureloot.toml. */
public final class LegacyJsonMigration {

    public static final String LEGACY_FILE_NAME = "PastureLoot.json";
    public static final String BACKUP_SUFFIX = ".old";

    /** Ticks per minute at normal game speed, the 1.0.x default of tick_per_minute. */
    static final int LEGACY_TICKS_PER_MINUTE = 1200;
    /** The 1.0.x default of drop_chance_per_minute. */
    static final double LEGACY_DROP_CHANCE_PER_MINUTE = 0.05;
    private static final int MINUTES_PER_HOUR = 60;
    /** Migrated rates keep two significant digits, so that float noise does not show in the file. */
    private static final MathContext TWO_DIGITS = new MathContext(2, RoundingMode.HALF_UP);

    private LegacyJsonMigration() {
    }

    /**
     * When the old file exists and the new one does not, writes the new file from the old values and
     * renames the old file with ".old". Returns whether it migrated.
     */
    public static boolean migrate(Path configDirectory, Consumer<String> warnings) throws IOException {
        Path legacyFile = configDirectory.resolve(LEGACY_FILE_NAME);
        Path settingsFile = configDirectory.resolve(SettingsFile.FILE_NAME);
        if (Files.notExists(legacyFile) || Files.exists(settingsFile)) {
            return false;
        }
        JsonObject json;
        try (Reader reader = Files.newBufferedReader(legacyFile, StandardCharsets.UTF_8)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (RuntimeException exception) {
            warnings.accept("Cannot read " + legacyFile + ", it is not migrated: " + exception.getMessage());
            return false;
        }
        SettingsFile.write(settingsFile, fromLegacy(json));
        Files.move(legacyFile, configDirectory.resolve(LEGACY_FILE_NAME + BACKUP_SUFFIX));
        return true;
    }

    static Settings fromLegacy(JsonObject json) {
        double ticksPerMinute = number(json, "tick_per_minute", LEGACY_TICKS_PER_MINUTE);
        if (!(ticksPerMinute > 0)) {
            ticksPerMinute = LEGACY_TICKS_PER_MINUTE;
        }
        double chancePerMinute = number(json, "drop_chance_per_minute", LEGACY_DROP_CHANCE_PER_MINUTE);
        double dropsPerHour = chancePerMinute * MINUTES_PER_HOUR * LEGACY_TICKS_PER_MINUTE / ticksPerMinute;
        return new Settings(
                twoDigits(DropRate.clamp(dropsPerHour)),
                items(json),
                ItemFilter.Mode.BLACKLIST,
                Settings.DEFAULT_FRIENDSHIP_MAX_BONUS,
                Settings.DEFAULT_MAX_ITEMS_NEARBY);
    }

    private static double twoDigits(double value) {
        return new BigDecimal(value).round(TWO_DIGITS).doubleValue();
    }

    private static double number(JsonObject json, String key, double defaultValue) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return defaultValue;
        }
        return element.getAsDouble();
    }

    private static List<String> items(JsonObject json) {
        JsonElement element = json.get("item_blacklist");
        if (element == null || !element.isJsonArray()) {
            return Settings.DEFAULT_FILTER_ITEMS;
        }
        JsonArray array = element.getAsJsonArray();
        List<String> items = new ArrayList<>();
        for (JsonElement item : array) {
            if (item.isJsonPrimitive() && item.getAsJsonPrimitive().isString()) {
                items.add(item.getAsString());
            }
        }
        return items;
    }
}
