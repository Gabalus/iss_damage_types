package org.gabalus.iss_damage_types.stun;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.gabalus.iss_damage_types.Config;
import org.gabalus.iss_damage_types.Iss_damage_types;
import org.gabalus.iss_damage_types.attr.ModAttributes;
import org.gabalus.iss_damage_types.attr.AttributesUtil;
import org.gabalus.iss_damage_types.damage.DamageTags;
import org.gabalus.iss_damage_types.network.Network;
import org.gabalus.iss_damage_types.network.StunGaugeS2C;

import static org.gabalus.iss_damage_types.stun.GaugeStore.*;
import static org.gabalus.iss_damage_types.stun.NBTKeys.*;
import static org.gabalus.iss_damage_types.stun.Thresholds.computeThreshold;

@EventBusSubscriber(modid = Iss_damage_types.MOD_ID)
public final class StunOnDamage {
    private StunOnDamage(){}

    private static double consumeRecentPlayerElementalAdd(LivingEntity target, Entity attacker, long now) {
        var nbt = target.getPersistentData();
        if (nbt.getInt(LAST_ELEM_ATTACKER) != attacker.getId()) return 0.0D;
        if (nbt.getLong(LAST_ELEM_TICK) != now) return 0.0D;
        double v = nbt.getDouble(LAST_ELEM_ADD);
        nbt.putDouble(LAST_ELEM_ADD, 0.0D);
        return Math.max(0.0D, v);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyOnPost(LivingDamageEvent.Post e) {
        LivingEntity target = e.getEntity();
        var source = e.getSource();
        if (target == null || target.level().isClientSide) return;

        long now = target.level().getGameTime();

        // player gauge handled in ShieldBlockHandler (on block only)
        if (target instanceof net.minecraft.world.entity.player.Player) {
            if (target.getPersistentData().getLong(BLOCK_TICK) == now) {
                sync(target, getGauge(target), computeThreshold(target));
            }
            return;
        }

        // entities: only physical damage contributes
        if (!DamageTags.isMinecraftPhysical(source)) return;

        if (now < target.getPersistentData().getLong(STUN_CD)) return;

        double dmg = e.getNewDamage();
        if (dmg <= 0.0D) return;

        Entity attackerEnt = source.getEntity();
        double subtractElem = attackerEnt != null ? consumeRecentPlayerElementalAdd(target, attackerEnt, now) : 0.0D;
        double add = Math.max(0.0D, dmg - subtractElem);
        if (add <= 0.0D) return;

        double threshold = computeThreshold(target);
        if (threshold <= 0.0D) return;

        double g = Math.min(threshold, getGauge(target) + add);
        markHitNow(target); // start/refresh grace window

        if (g >= threshold) {
            double potency = attackerEnt instanceof LivingEntity le ? AttributesUtil.safeValue(le, ModAttributes.STUN_POTENCY, 0.0D) : 0.0D;
            double resist  = AttributesUtil.safeValue(target, ModAttributes.STUN_RESISTANCE, 0.0D);
            double duration = Config.stunMinDurationTicks * (1.0D + Config.stunOverkillScale);
            duration *= (1.0D + potency);
            duration *= (1.0D - resist);

            int ticks = (int)Math.max(0, Math.min(Config.stunMaxDurationTicks, Math.round(duration)));
            if (ticks > 0) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 10, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,     ticks, 10, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,         ticks,  2, false, true, true));
            }

            setGauge(target, 0.0D);
            target.getPersistentData().putLong(STUN_CD, now + Config.stunCooldownTicks);
            sync(target, 0.0D, threshold);
        } else {
            setGauge(target, g);
            sync(target, g, threshold);
        }
    }

    @SubscribeEvent
    public static void tick(EntityTickEvent.Post e) {
        var ent = e.getEntity();
        if (!(ent instanceof LivingEntity le)) return;
        if (le.level().isClientSide) return;

        long now = le.level().getGameTime();
        double gauge = getGauge(le);

        boolean periodic = (now % Config.stunGaugeSyncIntervalTicks == 0);

        // 4s (configurable) grace: no decay until delay after last hit
        if (gauge > 0.0D) {
            long sinceHit = now - lastHit(le);
            if (sinceHit < Config.stunGaugeDecayDelayTicks) {
                // keep LAST_UPDATE fresh to avoid a big drop later
                le.getPersistentData().putLong(LAST_UPDATE, now);
                if (periodic) sync(le, gauge, computeThreshold(le));
                return;
            }
        }

        // normal decay
        long last = lastUpdate(le);
        long dt = Math.max(1, now - last);
        double decayPerTick = Config.stunGaugeDecayPerSecond / 20.0D;
        double newGauge = Math.max(0.0D, gauge - decayPerTick * dt);

        boolean changed = (newGauge != gauge);
        if (changed) setGauge(le, newGauge);

        if (changed || periodic) sync(le, getGauge(le), computeThreshold(le));
    }

    private static void sync(LivingEntity target, double gauge, double threshold) {
        if (target.level().isClientSide) return;
        var pkt = new StunGaugeS2C(target.getId(), gauge, threshold);
        if (target instanceof net.minecraft.server.level.ServerPlayer sp) {
            Network.sendTo(sp, pkt);
        }
        Network.sendTracking(target, pkt);
    }
}
