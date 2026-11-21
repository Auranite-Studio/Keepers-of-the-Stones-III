package com.auranite.keepersofthestonesthree.effect;

import com.auranite.abloom.effect.BurnEffect;
import com.auranite.keepersofthestonesthree.KeepersOfTheStonesIIIMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Kots3Effects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, KeepersOfTheStonesIIIMod.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> IGNITER = REGISTRY.register("igniter", () -> new IgniterEffect(0xFF5500));


    // ==================== Auto-generated resonance display effects ====================
    // These effects are used solely for rendering resonance icons (textures)

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance != null) {
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance != null) {
        }
    }
}
