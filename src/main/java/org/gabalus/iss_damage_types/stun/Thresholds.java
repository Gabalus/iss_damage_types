package org.gabalus.iss_damage_types.stun;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.gabalus.iss_damage_types.Config;
import org.gabalus.iss_damage_types.attr.ModAttributes;
import org.gabalus.iss_damage_types.attr.AttributesUtil;

public final class Thresholds {
    private Thresholds(){}

    public static double computeThreshold(LivingEntity target) {
        double basePct = Config.baseStunThresholdPct;
        double reduce  = AttributesUtil.safeValue(target, ModAttributes.STUN_THRESHOLD_REDUCTION, 0.0D);
        double threshold = target.getMaxHealth() * basePct * (1.0D - reduce);

        if (target instanceof Player p) {
            if (!hasShieldEquipped(p)) return 0.0D;
            threshold *= Config.shieldThresholdMultiplier;
            threshold *= AttributesUtil.safeValue(p, ModAttributes.STUN_SHIELD_ITEM_MULT, 1.0D);
        }
        return Math.max(0.0D, threshold);
    }

    public static boolean hasShieldEquipped(LivingEntity ent) {
        ItemStack main = ent.getMainHandItem();
        ItemStack off  = ent.getOffhandItem();
        return (main.getItem() instanceof ShieldItem) || (off.getItem() instanceof ShieldItem);
    }
}
