package org.gabalus.iss_damage_types.damage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.registries.Registries;

public final class DamageTags {
    private DamageTags(){}
    public static final TagKey<DamageType> PHYSICAL =
            TagKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath("iss_damage_types", "physical"));

    public static boolean isMinecraftPhysical(net.minecraft.world.damagesource.DamageSource src) {
        return src.is(PHYSICAL);
    }
}
