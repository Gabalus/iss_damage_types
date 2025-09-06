package org.gabalus.iss_damage_types.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.gabalus.iss_damage_types.Iss_damage_types;

@EventBusSubscriber(modid = Iss_damage_types.MOD_ID)
public final class Network {
    private static final String PROTOCOL = "1";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(Iss_damage_types.MOD_ID).versioned(PROTOCOL);
        registrar.playToClient(StunGaugeS2C.TYPE, StunGaugeS2C.CODEC, StunGaugeS2C::handle);
    }

    // sending helpers (optional)
    public static void sendTo(net.minecraft.server.level.ServerPlayer sp, net.minecraft.network.protocol.common.custom.CustomPacketPayload p) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, p);
    }
    public static void sendTracking(net.minecraft.world.entity.Entity entity, net.minecraft.network.protocol.common.custom.CustomPacketPayload p) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(entity, p);
    }
}