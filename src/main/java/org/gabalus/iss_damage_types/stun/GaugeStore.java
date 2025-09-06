package org.gabalus.iss_damage_types.stun;

import net.minecraft.world.entity.LivingEntity;

public final class GaugeStore {
    private GaugeStore(){}

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

    public static void markHitNow(LivingEntity e) {
        e.getPersistentData().putLong(NBTKeys.LAST_HIT, e.level().getGameTime());
    }
    public static long lastHit(LivingEntity e) {
        return e.getPersistentData().getLong(NBTKeys.LAST_HIT);
    }
}
