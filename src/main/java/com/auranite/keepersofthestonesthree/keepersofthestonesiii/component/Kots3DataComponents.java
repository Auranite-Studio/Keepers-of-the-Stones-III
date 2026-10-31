package com.auranite.keepersofthestonesthree.keepersofthestonesiii.component;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.KeepersOfTheStonesIIIMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom data components attached to elemental stones.
 * <p>
 * {@link #STONE_ACTIVE} is present while the stone is activated (master effect running).
 * {@link #STONE_RECHARGE_UNTIL} stores the game time (in world ticks) at which the stone
 * finishes recharging and can be activated again.
 */
public class Kots3DataComponents {
    public static final DeferredRegister<DataComponentType<?>> REGISTRY =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, KeepersOfTheStonesIIIMod.MODID);

    /** Marks a stone as currently active. Boolean-like component: true/false. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> STONE_ACTIVE =
            REGISTRY.register("stone_active",
                    () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());

    /** Game time (world time in ticks) when the recharge phase ends. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> STONE_RECHARGE_UNTIL =
            REGISTRY.register("stone_recharge_until",
                    () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
