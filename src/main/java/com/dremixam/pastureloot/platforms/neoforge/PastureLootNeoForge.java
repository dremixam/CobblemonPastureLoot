package com.dremixam.pastureloot.platforms.neoforge;

import com.dremixam.pastureloot.PastureLoot;
import com.dremixam.pastureloot.minecraft.PastureLootCommand;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** NeoForge entry point. */
@Mod(PastureLoot.MOD_ID)
public class PastureLootNeoForge {

    public PastureLootNeoForge() {
        PastureLoot.init(FMLPaths.CONFIGDIR.get());
        NeoForge.EVENT_BUS.addListener(
                (RegisterCommandsEvent event) -> PastureLootCommand.register(event.getDispatcher()));
    }
}
