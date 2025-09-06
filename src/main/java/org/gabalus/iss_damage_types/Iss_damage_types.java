package org.gabalus.iss_damage_types;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Iss_damage_types.MOD_ID)
public class Iss_damage_types {
    public static final String MOD_ID = "iss_damage_types";
    public static boolean IS_RANDOM_DAMAGE_MOD_ENABLED = false;

    public Iss_damage_types(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModAttributes.ATTRIBUTES.register(modBus);
        NeoForge.EVENT_BUS.register(EventHandler.class);
    }
}
