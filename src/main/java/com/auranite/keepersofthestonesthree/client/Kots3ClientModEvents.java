package com.auranite.keepersofthestonesthree.client;

import com.auranite.keepersofthestonesthree.KeepersOfTheStonesIIIMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Клиентская инициализация мода.
 */
@EventBusSubscriber(modid = KeepersOfTheStonesIIIMod.MODID, value = Dist.CLIENT)
public class Kots3ClientModEvents {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        KeepersOfTheStonesIIIMod.LOGGER.info("KOTS3 client initialized");

        // Register client-side event handlers
        NeoForge.EVENT_BUS.register(new FlameBarRenderer());
    }
}
