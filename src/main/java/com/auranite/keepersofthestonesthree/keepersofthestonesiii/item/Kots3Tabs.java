package com.auranite.keepersofthestonesthree.keepersofthestonesiii.item;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.KeepersOfTheStonesIIIMod;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

public class Kots3Tabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KeepersOfTheStonesIIIMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KOTS_TAB = REGISTRY.register("stones",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.kots.stones"))
                    .icon(() -> new ItemStack(Kots3Items.FIRE_STONE.get()))
                    .displayItems((parameters, tabData) -> {
                        tabData.accept(Kots3Items.FIRE_STONE.get());
                    })
                    .build());
}
