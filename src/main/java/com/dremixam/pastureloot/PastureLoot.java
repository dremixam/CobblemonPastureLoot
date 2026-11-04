package com.dremixam.pastureloot;

import com.dremixam.pastureloot.config.LegacyJsonMigration;
import com.dremixam.pastureloot.config.Settings;
import com.dremixam.pastureloot.config.SettingsFile;
import com.dremixam.pastureloot.loot.ItemFilter;
import com.dremixam.pastureloot.override.OverrideRule;
import com.dremixam.pastureloot.override.OverridesFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/** Common entry point, called by the entry point of each loader. */
public final class PastureLoot {

    public static final String MOD_ID = "pastureloot";
    public static final Logger LOGGER = LoggerFactory.getLogger("PastureLoot");

    private static Path configDirectory;
    private static Settings settings = Settings.DEFAULTS;
    private static ItemFilter itemFilter = Settings.DEFAULTS.itemFilter();
    private static List<OverrideRule> overrides = List.of();
    private static List<OverrideRule> rateOverrides = List.of();

    private PastureLoot() {
    }

    /** Loads the settings and the overrides from the config directory of the loader, migrating the 1.0.x file first. */
    public static void init(Path directory) {
        configDirectory = directory;
        reload(warning -> { });
    }

    /** Reads both files again. Every warning goes to the log and to the given consumer. */
    public static void reload(Consumer<String> warnings) {
        Consumer<String> allWarnings = warning -> {
            LOGGER.warn(warning);
            warnings.accept(warning);
        };
        loadSettings(allWarnings);
        loadOverrides(allWarnings);
    }

    private static void loadSettings(Consumer<String> warnings) {
        try {
            if (LegacyJsonMigration.migrate(configDirectory, warnings)) {
                LOGGER.info("{} was migrated to {}", LegacyJsonMigration.LEGACY_FILE_NAME, SettingsFile.FILE_NAME);
            }
            settings = SettingsFile.load(configDirectory.resolve(SettingsFile.FILE_NAME), warnings);
        } catch (IOException exception) {
            LOGGER.error("Cannot load {}", SettingsFile.FILE_NAME, exception);
            warnings.accept("Cannot load " + SettingsFile.FILE_NAME + ", default settings are used: " + exception.getMessage());
            settings = Settings.DEFAULTS;
        }
        itemFilter = settings.itemFilter();
    }

    private static void loadOverrides(Consumer<String> warnings) {
        try {
            overrides = OverridesFile.load(configDirectory.resolve(OverridesFile.FILE_NAME), warnings);
        } catch (IOException exception) {
            LOGGER.error("Cannot load {}", OverridesFile.FILE_NAME, exception);
            warnings.accept("Cannot load " + OverridesFile.FILE_NAME + ", no override is used: " + exception.getMessage());
            overrides = List.of();
        }
        rateOverrides = overrides.stream().filter(OverrideRule::changesRate).toList();
        LOGGER.info("{} override rules loaded", overrides.size());
    }

    public static Settings settings() {
        return settings;
    }

    public static ItemFilter itemFilter() {
        return itemFilter;
    }

    /** All override rules, in file order. */
    public static List<OverrideRule> overrides() {
        return overrides;
    }

    /** The override rules that change the drop rate, in file order. */
    public static List<OverrideRule> rateOverrides() {
        return rateOverrides;
    }
}
