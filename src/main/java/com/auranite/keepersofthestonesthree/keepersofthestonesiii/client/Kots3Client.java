package com.auranite.keepersofthestonesthree.keepersofthestonesiii.client;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.network.DeactivateStoneC2SPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side key binding: pressing U deactivates the active stone
 * (sends {@link DeactivateStoneC2SPacket} to the server).
 */
public final class Kots3Client {
    private Kots3Client() {}

    public static final KeyMapping DEACTIVATE_STONE_KEY = new KeyMapping(
            "key.kots.deactivate_stone",   // translation key
            InputConstants.KEY,
            GLFW.GLFW_KEY_U,
            "key.categories.kots");        // translation key for the category

    @EventBusSubscriber(value = Dist.CLIENT, modid = "kots")
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(DEACTIVATE_STONE_KEY);
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = "kots")
    public static class GameBusEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            while (DEACTIVATE_STONE_KEY.consumeClick()) {
                PacketDistributor.sendToServer(new DeactivateStoneC2SPacket());
            }
        }
    }
}
