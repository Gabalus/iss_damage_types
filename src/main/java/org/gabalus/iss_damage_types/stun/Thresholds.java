package org.gabalus.iss_damage_types.stun;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.gabalus.iss_damage_types.Config;
import org.gabalus.iss_damage_types.attr.ModAttributes;

public final class Thresholds {
    private Thresholds() {}

    // --- public API ---
    public static double threshold(LivingEntity e) {
        double basePct = dAttr(e, ModAttributes.STUN_BASE_THRESHOLD_PCT, Config.baseStunThresholdPct);
        double reduce  = dAttr(e, ModAttributes.STUN_THRESHOLD_REDUCTION, 0.0D);

        double th = e.getMaxHealth() * basePct * (1.0D - reduce);

        if (e instanceof Player p) {
            if (!hasShield(p)) return 0.0D;
            th *= dAttr(p, ModAttributes.STUN_SHIELD_MULT,      Config.shieldThresholdMultiplier);
            th *= dAttr(p, ModAttributes.STUN_SHIELD_ITEM_MULT, 1.0D);
        }
        return Math.max(0.0D, th);
    }

    public static double heavyThreshold(LivingEntity e) {
        double th = threshold(e);
        if (th <= 0) return 0;
        double mult = dAttr(e, ModAttributes.HEAVY_STUN_THRESHOLD_MULT, Config.heavyStunThresholdMult);
        return th * mult;
    }

    public static double decayPerTick(LivingEntity e) {
        return dAttr(e, ModAttributes.STUN_GAUGE_DECAY, Config.stunGaugeDecayPerSecond) / 20.0D;
    }

    public static int decayDelayTicks(LivingEntity e) {
        return iAttr(e, ModAttributes.STUN_GAUGE_DECAY_DELAY, Config.stunGaugeDecayDelayTicks);
    }

    public static int minDuration(LivingEntity e)   { return iAttr(e, ModAttributes.STUN_MIN_DURATION,   Config.stunMinDurationTicks); }
    public static int maxDuration(LivingEntity e)   { return iAttr(e, ModAttributes.STUN_MAX_DURATION,   Config.stunMaxDurationTicks); }
    public static int stunCooldown(LivingEntity e)  { return iAttr(e, ModAttributes.STUN_COOLDOWN,       Config.stunCooldownTicks); }
    public static double overkillScale(LivingEntity e) { return dAttr(e, ModAttributes.STUN_OVERKILL_SCALE, Config.stunOverkillScale); }

    public static boolean hasShield(LivingEntity e) {
        ItemStack main = e.getMainHandItem();
        ItemStack off  = e.getOffhandItem();
        return (main.getItem() instanceof ShieldItem) || (off.getItem() instanceof ShieldItem);
    }

    // --- attr helpers ---
    public static double dAttr(LivingEntity e, DeferredHolder<Attribute, Attribute> attr, double fallback) {
        var inst = e.getAttribute(attr);
        return inst != null ? inst.getValue() : fallback;
    }
    public static int iAttr(LivingEntity e, DeferredHolder<Attribute, Attribute> attr, int fallback) {
        return (int)Math.round(dAttr(e, attr, (double)fallback));
    }
}