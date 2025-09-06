package org.gabalus.iss_damage_types.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.gabalus.iss_damage_types.Iss_damage_types;
import org.gabalus.iss_damage_types.client.ClientGaugeCache;

public record StunGaugeS2C(int entityId, double gauge, double threshold) implements CustomPacketPayload {

    public static final Type<StunGaugeS2C> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Iss_damage_types.MOD_ID, "stun_gauge"));

    public static final StreamCodec<FriendlyByteBuf, StunGaugeS2C> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, StunGaugeS2C::entityId,
                    ByteBufCodecs.DOUBLE,  StunGaugeS2C::gauge,
                    ByteBufCodecs.DOUBLE,  StunGaugeS2C::threshold,
                    StunGaugeS2C::new
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final StunGaugeS2C pkt, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) return;
            ClientGaugeCache.update(pkt.entityId(), pkt.gauge(), pkt.threshold());
        });
    }
}