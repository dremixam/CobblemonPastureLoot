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

/** Common entry point, called by the entry point of each loader. */
public final class PastureLoot {

    public static final String MOD_ID = "pastureloot";
    public static final Logger LOGGER = LoggerFactory.getLogger("PastureLoot");

    private static Settings settings = Settings.DEFAULTS;
    private static ItemFilter itemFilter = Settings.DEFAULTS.itemFilter();
    private static List<OverrideRule> overrides = List.of();
    private static List<OverrideRule> rateOverrides = List.of();

    private PastureLoot() {
    }

    /** Loads the settings and the overrides from the config directory of the loader, migrating the 1.0.x file first. */
    public static void init(Path configDirectory) {
        loadSettings(configDirectory);
        loadOverrides(configDirectory);
    }

    private static void loadSettings(Path configDirectory) {
        try {
            if (LegacyJsonMigration.migrate(configDirectory, LOGGER::warn)) {
                LOGGER.info("{} was migrated to {}", LegacyJsonMigration.LEGACY_FILE_NAME, SettingsFile.FILE_NAME);
            }
            settings = SettingsFile.load(configDirectory.resolve(SettingsFile.FILE_NAME), LOGGER::warn);
        } catch (IOException exception) {
            LOGGER.error("Cannot load {}, default settings are used", SettingsFile.FILE_NAME, exception);
            settings = Settings.DEFAULTS;
        }
        itemFilter = settings.itemFilter();
    }

    private static void loadOverrides(Path configDirectory) {
        try {
            overrides = OverridesFile.load(configDirectory.resolve(OverridesFile.FILE_NAME), LOGGER::warn);
        } catch (IOException exception) {
            LOGGER.error("Cannot load {}, no override is used", OverridesFile.FILE_NAME, exception);
            overrides = List.of();
        }
        rateOverrides = overrides.stream().filter(OverrideRule::changesRate).toList();
        if (!overrides.isEmpty()) {
            LOGGER.info("{} override rules loaded", overrides.size());
        }
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
