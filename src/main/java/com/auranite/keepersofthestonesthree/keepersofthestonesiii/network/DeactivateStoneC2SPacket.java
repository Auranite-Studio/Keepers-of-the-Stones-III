package com.auranite.keepersofthestonesthree.keepersofthestonesiii.network;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.KeepersOfTheStonesIIIMod;
import com.auranite.keepersofthestonesthree.keepersofthestonesiii.handler.StoneMasterHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent by the client when the player presses the "Deactivate stone" key (U by default).
 * The server then deactivates every active stone in the player's inventory.
 */
public record DeactivateStoneC2SPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(KeepersOfTheStonesIIIMod.MODID, "deactivate_stone");
    public static final Type<DeactivateStoneC2SPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, DeactivateStoneC2SPacket> STREAM_CODEC =
            StreamCodec.unit(new DeactivateStoneC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DeactivateStoneC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof Player player) {
                StoneMasterHandler.deactivateAllStones(player);
            }
        });
    }
}
