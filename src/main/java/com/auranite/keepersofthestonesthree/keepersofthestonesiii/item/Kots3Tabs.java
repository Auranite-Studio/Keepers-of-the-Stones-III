package com.auranite.keepersofthestonesthree.keepersofthestonesiii.item;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.KeepersOfTheStonesIIIMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Kots3Tabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KeepersOfTheStonesIIIMod.MODID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KOTS_TAB = REGISTRY.register("stones",
			() -> CreativeModeTab.builder()
					.title(Component.translatable("item_group.kots.stones"))
					.icon(() -> new ItemStack(Kots3Items.FIRE_STONE.get()))
					.displayItems((parameters, tabData) -> {
						tabData.accept(Kots3Items.FIRE_STONE.get());
						tabData.accept(Kots3Items.AIR_STONE.get());
						tabData.accept(Kots3Items.AMBER_STONE.get());
						tabData.accept(Kots3Items.ANIMALS_STONE.get());
						tabData.accept(Kots3Items.BLOOD_STONE.get());
						tabData.accept(Kots3Items.BLUE_FLAME_STONE.get());
						tabData.accept(Kots3Items.COLORS_STONE.get());
						tabData.accept(Kots3Items.CREATION_STONE.get());
						tabData.accept(Kots3Items.CRYSTAL_STONE.get());
						tabData.accept(Kots3Items.DARKNESS_STONE.get());
						tabData.accept(Kots3Items.DESTRUCTION_STONE.get());
						tabData.accept(Kots3Items.DRAGON_STONE.get());
						tabData.accept(Kots3Items.EARTH_STONE.get());
						tabData.accept(Kots3Items.ENERGY_STONE.get());
						tabData.accept(Kots3Items.ETHER_STONE.get());
						tabData.accept(Kots3Items.EXPLOSION_STONE.get());
						tabData.accept(Kots3Items.FORM_STONE.get());
						tabData.accept(Kots3Items.GOLDEN_DUST_STONE.get());
						tabData.accept(Kots3Items.GRAVITY_STONE.get());
						tabData.accept(Kots3Items.HEAT_STONE.get());
						tabData.accept(Kots3Items.ICE_STONE.get());
						tabData.accept(Kots3Items.LAVA_STONE.get());
						tabData.accept(Kots3Items.LIGHT_STONE.get());
						tabData.accept(Kots3Items.LIGHTNING_STONE.get());
						tabData.accept(Kots3Items.MAGNET_STONE.get());
						tabData.accept(Kots3Items.MERCURY_STONE.get());
						tabData.accept(Kots3Items.METAL_STONE.get());
						tabData.accept(Kots3Items.MIND_STONE.get());
						tabData.accept(Kots3Items.MIST_STONE.get());
						tabData.accept(Kots3Items.MOON_STONE.get());
						tabData.accept(Kots3Items.MUSHROOMS_STONE.get());
						tabData.accept(Kots3Items.MUSIC_STONE.get());
						tabData.accept(Kots3Items.OCEAN_STONE.get());
						tabData.accept(Kots3Items.PLAGUE_STONE.get());
						tabData.accept(Kots3Items.PLANTS_STONE.get());
						tabData.accept(Kots3Items.POISON_STONE.get());
						tabData.accept(Kots3Items.RAIN_STONE.get());
						tabData.accept(Kots3Items.RECHARGE_STONE.get());
						tabData.accept(Kots3Items.RECHARGING_STONE.get());
						tabData.accept(Kots3Items.SAND_STONE.get());
						tabData.accept(Kots3Items.SHADOW_STONE.get());
						tabData.accept(Kots3Items.SHOCKWAVE_STONE.get());
						tabData.accept(Kots3Items.SMOKE_STONE.get());
						tabData.accept(Kots3Items.SOUND_STONE.get());
						tabData.accept(Kots3Items.SPACE_STONE.get());
						tabData.accept(Kots3Items.SPEED_STONE.get());
						tabData.accept(Kots3Items.SPIRIT_STONE.get());
						tabData.accept(Kots3Items.SUN_STONE.get());
						tabData.accept(Kots3Items.TEARS_STONE.get());
						tabData.accept(Kots3Items.TECHNOLOGY_STONE.get());
						tabData.accept(Kots3Items.TELEPORTATION_STONE.get());
						tabData.accept(Kots3Items.TIME_STONE.get());
						tabData.accept(Kots3Items.TORNADO_STONE.get());
						tabData.accept(Kots3Items.VACUUM_STONE.get());
						tabData.accept(Kots3Items.WATER_STONE.get());
						tabData.accept(Kots3Items.REDUCED_STONE_RECHARGE_TIME_RUNE_1.get());
						tabData.accept(Kots3Items.REDUCED_STONE_RECHARGE_TIME_RUNE_2.get());
						tabData.accept(Kots3Items.REDUCED_STONE_RECHARGE_TIME_RUNE_3.get());
						tabData.accept(Kots3Items.KEEPER_BOOK.get());
						tabData.accept(Kots3Items.FIRE_SWORD.get());
						tabData.accept(Kots3Items.FIRE_ARMOR_HELMET.get());
						tabData.accept(Kots3Items.FIRE_ARMOR_CHESTPLATE.get());
						tabData.accept(Kots3Items.FIRE_ARMOR_LEGGINGS.get());
						tabData.accept(Kots3Items.FIRE_ARMOR_BOOTS.get());
					})
					.build());
}
