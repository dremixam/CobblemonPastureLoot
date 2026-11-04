package com.dremixam.pastureloot.platforms.fabric;

import com.dremixam.pastureloot.PastureLoot;
import com.dremixam.pastureloot.minecraft.PastureLootCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

/** Fabric entry point. */
public class PastureLootFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PastureLoot.init(FabricLoader.getInstance().getConfigDir());
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> PastureLootCommand.register(dispatcher));
    }
}
