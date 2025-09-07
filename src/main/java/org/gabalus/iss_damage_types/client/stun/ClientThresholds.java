package org.gabalus.iss_damage_types.client.stun;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.gabalus.iss_damage_types.Config;
import org.gabalus.iss_damage_types.attr.ModAttributes;

public final class ClientThresholds {

    public static double playerThreshold(Player p) {
        if (p == null) return 0.0D;
        if (!hasShield(p)) return 0.0D;

        double basePct = safe(p, ModAttributes.STUN_BASE_THRESHOLD_PCT, Config.baseStunThresholdPct);
        double reduce  = safe(p, ModAttributes.STUN_THRESHOLD_REDUCTION, 0.0D);
        double th = p.getMaxHealth() * basePct * (1.0D - reduce);

        th *= safe(p, ModAttributes.STUN_SHIELD_MULT,      Config.shieldThresholdMultiplier);
        th *= safe(p, ModAttributes.STUN_SHIELD_ITEM_MULT, 1.0D);
        return Math.max(0.0D, th);
    }

    public static double entityThreshold(LivingEntity e) {
        if (e == null) return 0.0D;
        if (e instanceof Player p) return playerThreshold(p);

        double basePct = safe(e, ModAttributes.STUN_BASE_THRESHOLD_PCT, Config.baseStunThresholdPct);
        double reduce  = safe(e, ModAttributes.STUN_THRESHOLD_REDUCTION, 0.0D);
        return Math.max(0.0D, e.getMaxHealth() * basePct * (1.0D - reduce));
    }

    private static double safe(LivingEntity e, DeferredHolder<Attribute, Attribute> attr, double def) {
        var inst = e.getAttribute(attr);
        return inst != null ? inst.getValue() : def;
    }
    private static boolean hasShield(LivingEntity e) {
        ItemStack main = e.getMainHandItem(), off = e.getOffhandItem();
        return (main.getItem() instanceof ShieldItem) || (off.getItem() instanceof ShieldItem);
    }
}