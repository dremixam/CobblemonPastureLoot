package com.dremixam.pastureloot.platforms.fabric;

import com.dremixam.pastureloot.PastureLoot;
import net.fabricmc.api.ModInitializer;

/** Fabric entry point. */
public class PastureLootFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PastureLoot.init();
    }
}
