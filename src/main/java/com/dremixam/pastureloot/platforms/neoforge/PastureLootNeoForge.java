package com.dremixam.pastureloot.platforms.neoforge;

import com.dremixam.pastureloot.PastureLoot;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

/** NeoForge entry point. */
@Mod(PastureLoot.MOD_ID)
public class PastureLootNeoForge {

    public PastureLootNeoForge() {
        PastureLoot.init(FMLPaths.CONFIGDIR.get());
    }
}
