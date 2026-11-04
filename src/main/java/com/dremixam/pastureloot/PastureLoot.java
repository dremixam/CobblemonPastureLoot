package com.dremixam.pastureloot;

import com.dremixam.pastureloot.config.LegacyJsonMigration;
import com.dremixam.pastureloot.config.Settings;
import com.dremixam.pastureloot.config.SettingsFile;
import com.dremixam.pastureloot.loot.ItemFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/** Common entry point, called by the entry point of each loader. */
public final class PastureLoot {

    public static final String MOD_ID = "pastureloot";
    public static final Logger LOGGER = LoggerFactory.getLogger("PastureLoot");

    private static Settings settings = Settings.DEFAULTS;
    private static ItemFilter itemFilter = Settings.DEFAULTS.itemFilter();

    private PastureLoot() {
    }

    /** Loads the settings from the config directory of the loader, migrating the 1.0.x file first. */
    public static void init(Path configDirectory) {
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

    public static Settings settings() {
        return settings;
    }

    public static ItemFilter itemFilter() {
        return itemFilter;
    }
}
