package org.gabalus.iss_damage_types.stun;

import net.minecraft.world.entity.LivingEntity;

public final class GaugeStore {
    private GaugeStore() {}

    public static double getGauge(LivingEntity e) {
        return e.getPersistentData().getDouble(NBTKeys.GAUGE);
    }
    public static void setGauge(LivingEntity e, double v) {
        e.getPersistentData().putDouble(NBTKeys.GAUGE, Math.max(0.0D, v));
        e.getPersistentData().putLong(NBTKeys.LAST_UPDATE, e.level().getGameTime());
    }
    public static long lastUpdate(LivingEntity e) {
        return e.getPersistentData().getLong(NBTKeys.LAST_UPDATE);
    }

    public static long lastHit(LivingEntity e) {
        return e.getPersistentData().getLong(NBTKeys.LAST_HIT_TICK);
    }
    public static void markHitNow(LivingEntity e) {
        e.getPersistentData().putLong(NBTKeys.LAST_HIT_TICK, e.level().getGameTime());
    }

    public static double addClampedAndMark(LivingEntity e, double add, double threshold) {
        if (add <= 0 || threshold <= 0) return getGauge(e);
        double g = Math.min(threshold, getGauge(e) + add);
        setGauge(e, g);
        markHitNow(e);
        return g;
    }

    public static double getHeavy(LivingEntity e) {
        return e.getPersistentData().getDouble(NBTKeys.HEAVY_GAUGE);
    }
    public static void setHeavy(LivingEntity e, double v) {
        e.getPersistentData().putDouble(NBTKeys.HEAVY_GAUGE, Math.max(0.0D, v));
        e.getPersistentData().putLong(NBTKeys.HEAVY_LAST, e.level().getGameTime());
    }
    public static long lastHeavyUpdate(LivingEntity e) {
        return e.getPersistentData().getLong(NBTKeys.HEAVY_LAST);
    }
}