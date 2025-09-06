package org.gabalus.iss_damage_types.stun;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.gabalus.iss_damage_types.Config;
import org.gabalus.iss_damage_types.network.Network;
import org.gabalus.iss_damage_types.network.StunGaugeS2C;

public final class ShieldBlockHandler {

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent e) {
        LivingEntity tgt = e.getEntity();
        if (tgt.level().isClientSide) return;

        long now = tgt.level().getGameTime();
        var nbt = tgt.getPersistentData();
        nbt.putLong(NBTKeys.BLOCK_TICK, now);
        nbt.putDouble(NBTKeys.BLOCK_AMOUNT, Math.max(0.0D, e.getBlockedDamage()));

        if (!(tgt instanceof Player p) || !p.isBlocking()) return;

        double add = Math.max(0.0D, e.getBlockedDamage());

        // normal gauge rises so HUD reacts
        double th = Thresholds.threshold(tgt);
        if (th > 0 && add > 0) {
            double g = GaugeStore.addClampedAndMark(tgt, add, th);
            syncGauge(tgt, g, th);
        }

        // heavy gauge (builds while blocking)
        double hTh = Thresholds.heavyThreshold(tgt);
        if (hTh <= 0.0D || add <= 0.0D) return;
        double hg = Math.min(hTh, GaugeStore.getHeavy(tgt) + add);

        if (hg >= hTh) {
            int ticks = Thresholds.iAttr(tgt, org.gabalus.iss_damage_types.attr.ModAttributes.HEAVY_STUN_DURATION, Config.heavyStunDurationTicks);
            if (ticks > 0) {
                tgt.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 10, false, true, true));
                tgt.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,     ticks, 10, false, true, true));
                tgt.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,         ticks,  2, false, true, true));
            }
            GaugeStore.setHeavy(tgt, 0.0D);
        } else {
            GaugeStore.setHeavy(tgt, hg);
        }
    }

    private static void syncGauge(LivingEntity who, double gauge, double threshold) {
        if (who.level().isClientSide) return;
        var pkt = new StunGaugeS2C(who.getId(), gauge, threshold);
        Network.sendTracking(who, pkt);
        if (who instanceof net.minecraft.server.level.ServerPlayer sp) Network.sendTo(sp, pkt);
    }
}