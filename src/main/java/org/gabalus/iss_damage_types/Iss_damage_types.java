package org.gabalus.iss_damage_types;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.gabalus.iss_damage_types.attr.ModAttributes;

@Mod(Iss_damage_types.MOD_ID)
public class Iss_damage_types {
    public static final String MOD_ID = "iss_damage_types";
    public static boolean IS_RANDOM_DAMAGE_MOD_ENABLED = false;

    public Iss_damage_types(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModAttributes.ATTRIBUTES.register(modBus);
    }

    public static final TagKey<DamageType> IS_ELECTRIC =
            TagKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath("iss_damage_types", "is_electric"));
}
