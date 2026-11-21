package com.auranite.keepersofthestonesthree.network;

import com.auranite.keepersofthestonesthree.KeepersOfTheStonesIIIMod;
import com.auranite.keepersofthestonesthree.client.ClientFlamePointsStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Пакет синхронизации очков Пламени от сервера к клиенту.
 */
public record FlamePointsSyncPacket(byte flamePoints) implements CustomPacketPayload {

    public static final Type<FlamePointsSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(KeepersOfTheStonesIIIMod.MODID, "flame_points_sync"));

    public static final StreamCodec<FriendlyByteBuf, FlamePointsSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FlamePointsSyncPacket decode(FriendlyByteBuf buf) {
            return new FlamePointsSyncPacket(buf.readByte());
        }

        @Override
        public void encode(FriendlyByteBuf buf, FlamePointsSyncPacket msg) {
            buf.writeByte(msg.flamePoints());
        }
    };

    public static void handle(FlamePointsSyncPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientFlamePointsStorage.setFlamePoints(msg.flamePoints() & 0xFF);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
