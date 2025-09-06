package org.gabalus.iss_damage_types.client.stun;

import java.util.concurrent.ConcurrentHashMap;

public final class ClientGaugeCache {
    private static final class G { double g, th; long t; }
    private static final ConcurrentHashMap<Integer, G> MAP = new ConcurrentHashMap<>();

    public static void update(int id, double gauge, double threshold) {
        var v = MAP.computeIfAbsent(id, k -> new G());
        v.g = gauge; v.th = threshold; v.t = System.currentTimeMillis();
    }
    public static double gauge(int id)     { var v = MAP.get(id); return v == null ? 0.0D : v.g; }
    public static double threshold(int id) { var v = MAP.get(id); return v == null ? 0.0D : v.th; }
}