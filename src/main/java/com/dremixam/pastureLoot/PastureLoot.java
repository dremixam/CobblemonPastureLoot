package com.dremixam.pastureLoot;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PastureLoot implements ModInitializer {

    public static PastureLoot INSTANCE;
    public static final Logger LOGGER = LoggerFactory.getLogger(PastureLoot.class);

    private Config config;

    @Override
    public void onInitialize() {
        INSTANCE = this;
        config = Config.load();
    }

    public Config getConfig() {
        return config;
    }
}
