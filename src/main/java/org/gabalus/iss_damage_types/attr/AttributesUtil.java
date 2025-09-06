package org.gabalus.iss_damage_types.attr;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class AttributesUtil {
    private AttributesUtil(){}

    public static double safeValue(LivingEntity ent,
                                   DeferredHolder<Attribute, Attribute> attr,
                                   double def) {
        if (attr ==null) return def;
        var inst = ent.getAttribute(attr);
        return inst != null ? inst.getValue() : def;
    }
}
