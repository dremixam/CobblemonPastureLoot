package com.dremixam.pastureloot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common entry point, called by the entry point of each loader. */
public final class PastureLoot {

    public static final String MOD_ID = "pastureloot";
    public static final Logger LOGGER = LoggerFactory.getLogger("PastureLoot");

    private static Config config;

    private PastureLoot() {
    }

    public static void init() {
        config = Config.load();
    }

    public static Config config() {
        return config;
    }
}
