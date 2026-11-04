package com.dremixam.pastureloot.config;

import com.dremixam.pastureloot.loot.DropRate;
import com.dremixam.pastureloot.loot.ItemFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsFileTest {

    @TempDir
    Path directory;

    private final List<String> warnings = new ArrayList<>();

    private Path file() {
        return directory.resolve(SettingsFile.FILE_NAME);
    }

    private Settings loadText(String text) throws IOException {
        Files.writeString(file(), text, StandardCharsets.UTF_8);
        return SettingsFile.load(file(), warnings::add);
    }

    @Test
    void missingFileIsWrittenWithDefaults() throws IOException {
        Settings first = SettingsFile.load(file(), warnings::add);
        assertEquals(Settings.DEFAULTS, first);
        assertTrue(Files.exists(file()));
        assertEquals(Settings.DEFAULTS, SettingsFile.load(file(), warnings::add));
        assertEquals(List.of(), warnings);
    }

    @Test
    void writtenFileIsCommented() throws IOException {
        SettingsFile.load(file(), warnings::add);
        String text = Files.readString(file(), StandardCharsets.UTF_8);
        assertTrue(text.contains("# Average number of drops per hour"), text);
        assertTrue(text.contains("[filter]"), text);
    }

    @Test
    void writtenSettingsAreReadBack() throws IOException {
        Settings custom = new Settings(12.5, List.of("minecraft:snowball"), ItemFilter.Mode.WHITELIST, 0, 0);
        SettingsFile.write(file(), custom);
        assertEquals(custom, SettingsFile.load(file(), warnings::add));
    }

    @Test
    void missingValuesTakeTheirDefaultSilently() throws IOException {
        Settings settings = loadText("[drops]\nper_hour = 6\n");
        assertEquals(6, settings.dropsPerHour());
        assertEquals(Settings.DEFAULT_FILTER_ITEMS, settings.filterItems());
        assertEquals(List.of(), warnings);
    }

    @Test
    void outOfBoundsValuesAreClampedWithAWarning() throws IOException {
        Settings settings = loadText("[drops]\nper_hour = 1000000\n[anti_lag]\nmax_items_nearby = -4\n");
        assertEquals(DropRate.MAX_DROPS_PER_HOUR, settings.dropsPerHour());
        assertEquals(0, settings.maxItemsNearby());
        assertEquals(2, warnings.size());
    }

    @Test
    void wrongTypesTakeTheirDefaultWithAWarning() throws IOException {
        Settings settings = loadText("[drops]\nper_hour = \"a lot\"\n[filter]\nmode = \"greylist\"\nitems = [1, 2]\n");
        assertEquals(Settings.DEFAULT_DROPS_PER_HOUR, settings.dropsPerHour());
        assertEquals(ItemFilter.Mode.BLACKLIST, settings.filterMode());
        assertEquals(Settings.DEFAULT_FILTER_ITEMS, settings.filterItems());
        assertEquals(3, warnings.size());
    }

    @Test
    void unreadableFileGivesDefaultsAndIsKept() throws IOException {
        String broken = "[drops\nper_hour = 6\n";
        Settings settings = loadText(broken);
        assertEquals(Settings.DEFAULTS, settings);
        assertEquals(1, warnings.size());
        assertEquals(broken, Files.readString(file(), StandardCharsets.UTF_8));
    }
}
