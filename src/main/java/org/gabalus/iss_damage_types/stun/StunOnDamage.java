package org.gabalus.iss_damage_types.stun;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.gabalus.iss_damage_types.Config;
import org.gabalus.iss_damage_types.Iss_damage_types;
import org.gabalus.iss_damage_types.network.Network;
import org.gabalus.iss_damage_types.network.StunGaugeS2C;

public final class StunOnDamage {

    // Tag your datapack with iss_damage_types:physical listing generic, arrow, trident, etc.
    private static final TagKey<DamageType> PHYSICAL = TagKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Iss_damage_types.MOD_ID, "physical")
    );

    private static boolean isMinecraftPhysical(DamageSource src) {
        return src.is(PHYSICAL);
    }

    // ------------- (A) if you add elemental in Pre, record how much the PLAYER added -------------
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDamagePreMarkElemental(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre e) {
        // If you already implemented the elemental add elsewhere, keep that code there.
        // Just make sure you finish with:
        // markPlayerElementalAdd(e.getEntity(), attackerEntity, amountAdded);
    }

    private static void markPlayerElementalAdd(LivingEntity target, Entity attacker, double elemAdded) {
        if (elemAdded <= 0 || !(attacker instanceof Player)) return;
        var nbt = target.getPersistentData();
        nbt.putDouble(NBTKeys.ELEM_ADD, elemAdded);
        nbt.putLong(NBTKeys.ELEM_TICK, target.level().getGameTime());
        nbt.putInt (NBTKeys.ELEM_ATTACKER, attacker.getId());
    }

    private static double consumeRecentPlayerElementalAdd(LivingEntity target, Entity attacker, long now) {
        if (!(attacker instanceof Player)) return 0.0D;
        var nbt = target.getPersistentData();
        long when = nbt.getLong(NBTKeys.ELEM_TICK);
        if (when != now) return 0.0D;
        if (nbt.getInt(NBTKeys.ELEM_ATTACKER) != attacker.getId()) return 0.0D;
        double v = nbt.getDouble(NBTKeys.ELEM_ADD);
        nbt.putDouble(NBTKeys.ELEM_ADD, 0.0D);
        return Math.max(0.0D, v);
    }

    // ------------- (B) build the stun gauge only from physical, subtracting player's elemental -------------
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamagePostGrowGauge(LivingDamageEvent.Post e) {
        LivingEntity target = e.getEntity();
        if (target == null || target.level().isClientSide) return;

        DamageSource source = e.getSource();
        Entity attackerEnt = source.getEntity();

        // Players only build their gauge while a shield is equipped (PoE2-like)
        boolean isPlayer = target instanceof Player;
        if (isPlayer && !Thresholds.hasShield(target)) return;

        // Only Minecraft "physical" (generic/arrow/trident/etc.)
        if (!isMinecraftPhysical(source)) return;

        double dmg = e.getNewDamage();
        if (dmg <= 0.0D) return;

        long now = target.level().getGameTime();

        // subtract elemental the PLAYER attacker added this same tick
        double minusElem = attackerEnt != null ? consumeRecentPlayerElementalAdd(target, attackerEnt, now) : 0.0D;
        double dmgForGauge = Math.max(0.0D, dmg - minusElem);
        if (dmgForGauge <= 0.0D) return;

        double th = Thresholds.threshold(target);
        if (th <= 0.0D) return;

        // grow + remember last hit time; sync HUD
        double g = GaugeStore.addClampedAndMark(target, dmgForGauge, th);
        syncGauge(target, g, th);

        // full -> stun
        if (g >= th) {
            double duration = Thresholds.minDuration(target) * (1.0D + Thresholds.overkillScale(target));
            // attacker potency / target resist (optional if you use those attrs)
            // duration *= (1.0 + potency);
            // duration *= (1.0 - resist);

            int ticks = Math.max(0, Math.min(Thresholds.maxDuration(target), (int)Math.round(duration)));
            if (ticks > 0) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 10, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,     ticks, 10, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,         ticks,  2, false, true, true));
            }

            GaugeStore.setGauge(target, 0.0D);
            target.getPersistentData().putLong(NBTKeys.STUN_COOLDOWN, now + Thresholds.stunCooldown(target));
            syncGauge(target, 0.0D, th);
        }
    }

    // --- periodic decay with 4s (attr-driven) no-hit delay ---
    @SubscribeEvent
    public static void onEntityTick(final net.neoforged.neoforge.event.tick.EntityTickEvent.Post e) {
        if (!(e.getEntity() instanceof LivingEntity le) || le.level().isClientSide) return;

        double g = GaugeStore.getGauge(le);
        if (g <= 0.0D) return;

        long now = le.level().getGameTime();
        int delay = Thresholds.decayDelayTicks(le);

        if (now - GaugeStore.lastHit(le) < delay) {
            // still in grace; keep HUD responsive
            if (now % Config.stunGaugeSyncIntervalTicks == 0) {
                syncGauge(le, g, Thresholds.threshold(le));
            }
            return;
        }

        long last = GaugeStore.lastUpdate(le);
        long dt = Math.max(1, now - last);

        double ng = Math.max(0.0D, g - Thresholds.decayPerTick(le) * dt);
        if (ng != g) GaugeStore.setGauge(le, ng);

        if (ng != g || now % Config.stunGaugeSyncIntervalTicks == 0) {
            syncGauge(le, ng, Thresholds.threshold(le));
        }
    }

    // small helper, uses your existing packet utilities
    private static void syncGauge(LivingEntity who, double gauge, double threshold) {
        if (who.level().isClientSide) return;
        var pkt = new StunGaugeS2C(who.getId(), gauge, threshold);
        Network.sendTracking(who, pkt);
        if (who instanceof net.minecraft.server.level.ServerPlayer sp) {
            Network.sendTo(sp, pkt);
        }
    }
}
