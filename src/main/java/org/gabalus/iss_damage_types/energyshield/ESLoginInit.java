package org.gabalus.iss_damage_types.energyshield;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.gabalus.iss_damage_types.Config;
import org.gabalus.iss_damage_types.Iss_damage_types;
import org.gabalus.iss_damage_types.attr.ModAttributes;
import org.gabalus.iss_damage_types.network.Network;
import org.gabalus.iss_damage_types.network.SyncESPacket;

@EventBusSubscriber(modid = Iss_damage_types.MOD_ID, value = Dist.DEDICATED_SERVER)
public final class ESLoginInit {
    private static final String ES_CUR = "es_current";

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;

        var tag = sp.getPersistentData();
        boolean isNewPlayer = !tag.contains(ES_CUR);
        
        if (isNewPlayer) {
            // Set initial energy shield values from config
            sp.getAttribute(ModAttributes.ES_MAX).setBaseValue(Config.defaultESMax);
            sp.getAttribute(ModAttributes.ES_RECHARGE_RATE).setBaseValue(Config.defaultESRechargeRate);
            sp.getAttribute(ModAttributes.ES_RECHARGE_DELAY).setBaseValue(Config.defaultESRechargeDelay);
            sp.getAttribute(ModAttributes.ES_BREAK_THRESHOLD).setBaseValue(Config.defaultESBreakThreshold);
            sp.getAttribute(ModAttributes.ES_ON_KILL_GAIN).setBaseValue(Config.defaultESOnKillGain);
            
            // Initialize current ES to max
            tag.putDouble(ES_CUR, Config.defaultESMax);
        }

        double cur = tag.getDouble(ES_CUR);
        double max = sp.getAttributeValue(ModAttributes.ES_MAX);
        Network.sendTo(sp, new SyncESPacket(cur, max));
    }
}