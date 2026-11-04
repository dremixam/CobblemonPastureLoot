package com.dremixam.pastureloot.config;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyJsonMigrationTest {

    /** Migrated rates keep two significant digits. */
    private static final double ROUNDING = 0.05;

    @TempDir
    Path directory;

    private final List<String> warnings = new ArrayList<>();

    private void writeLegacy(String json) throws IOException {
        Files.writeString(directory.resolve(LegacyJsonMigration.LEGACY_FILE_NAME), json, StandardCharsets.UTF_8);
    }

    @Test
    void legacyDefaultsGiveTheNewDefaultRate() {
        Settings settings = LegacyJsonMigration.fromLegacy(JsonParser.parseString(
                "{\"tick_per_minute\": 1200, \"drop_chance_per_minute\": 0.05}").getAsJsonObject());
        assertEquals(Settings.DEFAULT_DROPS_PER_HOUR, settings.dropsPerHour());
    }

    @Test
    void fasterTicksPerMinuteRaiseTheRate() {
        Settings normal = LegacyJsonMigration.fromLegacy(JsonParser.parseString(
                "{\"tick_per_minute\": 1200, \"drop_chance_per_minute\": 0.05}").getAsJsonObject());
        Settings faster = LegacyJsonMigration.fromLegacy(JsonParser.parseString(
                "{\"tick_per_minute\": 600, \"drop_chance_per_minute\": 0.05}").getAsJsonObject());
        assertEquals(2 * normal.dropsPerHour(), faster.dropsPerHour(), ROUNDING);
    }

    @Test
    void customBlacklistIsKept() {
        Settings settings = LegacyJsonMigration.fromLegacy(JsonParser.parseString(
                "{\"item_blacklist\": [\"minecraft:bone\", 3]}").getAsJsonObject());
        assertEquals(List.of("minecraft:bone"), settings.filterItems());
    }

    @Test
    void migrationWritesTheNewFileAndKeepsABackup() throws IOException {
        writeLegacy("{\"tick_per_minute\": 1200, \"drop_chance_per_minute\": 1.0, \"item_blacklist\": []}");
        assertTrue(LegacyJsonMigration.migrate(directory, warnings::add));
        Settings settings = SettingsFile.load(directory.resolve(SettingsFile.FILE_NAME), warnings::add);
        assertEquals(60, settings.dropsPerHour());
        assertTrue(Files.exists(directory.resolve(LegacyJsonMigration.LEGACY_FILE_NAME + LegacyJsonMigration.BACKUP_SUFFIX)));
        assertFalse(Files.exists(directory.resolve(LegacyJsonMigration.LEGACY_FILE_NAME)));
        assertEquals(List.of(), warnings);
    }

    @Test
    void existingNewFileIsNeverReplaced() throws IOException {
        writeLegacy("{\"drop_chance_per_minute\": 1.0}");
        Files.writeString(directory.resolve(SettingsFile.FILE_NAME), "[drops]\nper_hour = 7\n");
        assertFalse(LegacyJsonMigration.migrate(directory, warnings::add));
        assertTrue(Files.exists(directory.resolve(LegacyJsonMigration.LEGACY_FILE_NAME)));
    }

    @Test
    void brokenLegacyFileIsLeftAlone() throws IOException {
        writeLegacy("{not json");
        assertFalse(LegacyJsonMigration.migrate(directory, warnings::add));
        assertEquals(1, warnings.size());
        assertTrue(Files.exists(directory.resolve(LegacyJsonMigration.LEGACY_FILE_NAME)));
    }
}
