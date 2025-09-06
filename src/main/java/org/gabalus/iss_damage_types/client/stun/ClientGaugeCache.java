package org.gabalus.iss_damage_types.client.stun;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientGaugeCache {
    private static final Map<Integer, Double> GAUGE = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> THRESH = new ConcurrentHashMap<>();

    public static void update(int entityId, double gauge, double threshold) {
        GAUGE.put(entityId, gauge);
        THRESH.put(entityId, threshold);
    }
    public static double gauge(int id)    { return GAUGE.getOrDefault(id, 0.0D); }
    public static double threshold(int id){ return THRESH.getOrDefault(id, 0.0D); }
}