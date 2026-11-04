package com.dremixam.pastureloot.platforms.fabric;

import com.dremixam.pastureloot.PastureLoot;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** Fabric entry point. */
public class PastureLootFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PastureLoot.init(FabricLoader.getInstance().getConfigDir());
    }
}
