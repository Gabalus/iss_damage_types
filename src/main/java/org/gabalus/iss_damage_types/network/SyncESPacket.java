package org.gabalus.iss_damage_types.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.gabalus.iss_damage_types.Iss_damage_types;

public record SyncESPacket(double cur, double max) implements CustomPacketPayload {

    public static final Type<SyncESPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Iss_damage_types.MOD_ID, "sync_es"));

    public static final StreamCodec<FriendlyByteBuf, SyncESPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, SyncESPacket::cur,
                    ByteBufCodecs.DOUBLE, SyncESPacket::max,
                    SyncESPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final SyncESPacket pkt, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var mc = net.minecraft.client.Minecraft.getInstance();
            var p = mc.player;
            if (p != null) {
                p.getPersistentData().putDouble("es_current_client", pkt.cur());
                p.getPersistentData().putDouble("es_max_client", pkt.max());
            }
        });
    }
}