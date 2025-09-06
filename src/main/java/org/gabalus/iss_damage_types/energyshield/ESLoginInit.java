package org.gabalus.iss_damage_types.energyshield;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
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
        if (!tag.contains(ES_CUR)) {
            tag.putDouble(ES_CUR, 0.0);
        }

        double cur = tag.getDouble(ES_CUR);
        double max = sp.getAttributeValue(ModAttributes.ES_MAX);
        Network.sendTo(sp, new SyncESPacket(cur, max));
    }
}