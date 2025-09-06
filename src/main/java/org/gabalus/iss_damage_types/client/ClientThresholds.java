package org.gabalus.iss_damage_types.client;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.gabalus.iss_damage_types.Config;
import org.gabalus.iss_damage_types.ModAttributes;

@OnlyIn(Dist.CLIENT)
public class ClientThresholds {

    public static double playerThreshold(Player p) {
        if (p == null) return 0.0D;
        if (!hasShieldEquipped(p)) return 0.0D;

        double basePct = Config.baseStunThresholdPct;
        double reduce  = safeValue(p, ModAttributes.STUN_THRESHOLD_REDUCTION, 0.0D);
        double th = p.getMaxHealth() * basePct * (1.0D - reduce);
        th *= Config.shieldThresholdMultiplier;

        th *= safeValue(p, ModAttributes.STUN_SHIELD_ITEM_MULT, 1.0D);

        return Math.max(0.0D, th);
    }

    public static double entityThreshold(LivingEntity e) {
        if (e == null) return 0.0D;
        if (e instanceof Player) return playerThreshold((Player)e);

        double basePct = Config.baseStunThresholdPct;
        double reduce  = safeValue(e, ModAttributes.STUN_THRESHOLD_REDUCTION, 0.0D);
        double th = e.getMaxHealth() * basePct * (1.0D - reduce);
        return th;
    }

    public static float playerFillRatio(Player p) {
        double max = p.getMaxHealth();
        double th  = playerThreshold(p);
        return (float)Math.max(0.0D, Math.min(1.0D, th / Math.max(1.0D, max)));
    }
    public static float entityFillRatio(LivingEntity e) {
        double max = e.getMaxHealth();
        double th  = entityThreshold(e);
        return (float)Math.max(0.0D, Math.min(1.0D, th / Math.max(1.0D, max)));
    }

    private static double safeValue(LivingEntity ent, DeferredHolder<Attribute, Attribute> attr, double def) {
        var inst = ent.getAttribute(attr);
        return inst != null ? inst.getValue() : def;
    }

    private static boolean hasShieldEquipped(LivingEntity ent) {
        ItemStack main = ent.getMainHandItem();
        ItemStack off  = ent.getOffhandItem();
        return (main.getItem() instanceof ShieldItem) || (off.getItem() instanceof ShieldItem);
    }
}
