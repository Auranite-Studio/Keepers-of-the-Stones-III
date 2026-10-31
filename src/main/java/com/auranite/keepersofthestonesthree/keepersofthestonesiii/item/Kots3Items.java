package com.auranite.keepersofthestonesthree.keepersofthestonesiii.item;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.KeepersOfTheStonesIIIMod;
import com.auranite.keepersofthestonesthree.keepersofthestonesiii.effect.FireArmorItem;
import com.auranite.keepersofthestonesthree.keepersofthestonesiii.effect.FireSwordItem;
import com.auranite.keepersofthestonesthree.keepersofthestonesiii.item.custom.KeeperStoneItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers every item of the mod: elemental stones, recharge runes,
 * the Keeper Book and the fire equipment set.
 */
public class Kots3Items {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(KeepersOfTheStonesIIIMod.MODID);

	public static final DeferredItem<Item> FIRE_STONE;
	public static final DeferredItem<Item> AIR_STONE;
	public static final DeferredItem<Item> AMBER_STONE;
	public static final DeferredItem<Item> ANIMALS_STONE;
	public static final DeferredItem<Item> BLOOD_STONE;
	public static final DeferredItem<Item> BLUE_FLAME_STONE;
	public static final DeferredItem<Item> COLORS_STONE;
	public static final DeferredItem<Item> CREATION_STONE;
	public static final DeferredItem<Item> CRYSTAL_STONE;
	public static final DeferredItem<Item> DARKNESS_STONE;
	public static final DeferredItem<Item> DESTRUCTION_STONE;
	public static final DeferredItem<Item> DRAGON_STONE;
	public static final DeferredItem<Item> EARTH_STONE;
	public static final DeferredItem<Item> ENERGY_STONE;
	public static final DeferredItem<Item> ETHER_STONE;
	public static final DeferredItem<Item> EXPLOSION_STONE;
	public static final DeferredItem<Item> FORM_STONE;
	public static final DeferredItem<Item> GOLDEN_DUST_STONE;
	public static final DeferredItem<Item> GRAVITY_STONE;
	public static final DeferredItem<Item> HEAT_STONE;
	public static final DeferredItem<Item> ICE_STONE;
	public static final DeferredItem<Item> LAVA_STONE;
	public static final DeferredItem<Item> LIGHT_STONE;
	public static final DeferredItem<Item> LIGHTNING_STONE;
	public static final DeferredItem<Item> MAGNET_STONE;
	public static final DeferredItem<Item> MERCURY_STONE;
	public static final DeferredItem<Item> METAL_STONE;
	public static final DeferredItem<Item> MIND_STONE;
	public static final DeferredItem<Item> MIST_STONE;
	public static final DeferredItem<Item> MOON_STONE;
	public static final DeferredItem<Item> MUSHROOMS_STONE;
	public static final DeferredItem<Item> MUSIC_STONE;
	public static final DeferredItem<Item> OCEAN_STONE;
	public static final DeferredItem<Item> PLAGUE_STONE;
	public static final DeferredItem<Item> PLANTS_STONE;
	public static final DeferredItem<Item> POISON_STONE;
	public static final DeferredItem<Item> RAIN_STONE;
	public static final DeferredItem<Item> RECHARGE_STONE;
	public static final DeferredItem<Item> RECHARGING_STONE;
	public static final DeferredItem<Item> SAND_STONE;
	public static final DeferredItem<Item> SHADOW_STONE;
	public static final DeferredItem<Item> SHOCKWAVE_STONE;
	public static final DeferredItem<Item> SMOKE_STONE;
	public static final DeferredItem<Item> SOUND_STONE;
	public static final DeferredItem<Item> SPACE_STONE;
	public static final DeferredItem<Item> SPEED_STONE;
	public static final DeferredItem<Item> SPIRIT_STONE;
	public static final DeferredItem<Item> SUN_STONE;
	public static final DeferredItem<Item> TEARS_STONE;
	public static final DeferredItem<Item> TECHNOLOGY_STONE;
	public static final DeferredItem<Item> TELEPORTATION_STONE;
	public static final DeferredItem<Item> TIME_STONE;
	public static final DeferredItem<Item> TORNADO_STONE;
	public static final DeferredItem<Item> VACUUM_STONE;
	public static final DeferredItem<Item> WATER_STONE;
	public static final DeferredItem<Item> REDUCED_STONE_RECHARGE_TIME_RUNE_1;
	public static final DeferredItem<Item> REDUCED_STONE_RECHARGE_TIME_RUNE_2;
	public static final DeferredItem<Item> REDUCED_STONE_RECHARGE_TIME_RUNE_3;
	public static final DeferredItem<Item> KEEPER_BOOK;
	public static final DeferredItem<Item> FIRE_SWORD;
	public static final DeferredItem<Item> FIRE_ARMOR_HELMET;
	public static final DeferredItem<Item> FIRE_ARMOR_CHESTPLATE;
	public static final DeferredItem<Item> FIRE_ARMOR_LEGGINGS;
	public static final DeferredItem<Item> FIRE_ARMOR_BOOTS;

	private static final Item.Properties STONE_PROPS = new Item.Properties()
			.stacksTo(1)
			.rarity(Rarity.RARE);

	private static final Item.Properties RUNE_PROPS = new Item.Properties()
			.stacksTo(16)
			.rarity(Rarity.UNCOMMON);

	private static final Item.Properties BOOK_PROPS = new Item.Properties()
			.stacksTo(1)
			.rarity(Rarity.EPIC);

	static {
		AIR_STONE = REGISTRY.register("air_stone", () -> new KeeperStoneItem(STONE_PROPS));
		AMBER_STONE = REGISTRY.register("amber_stone", () -> new KeeperStoneItem(STONE_PROPS));
		ANIMALS_STONE = REGISTRY.register("animals_stone", () -> new KeeperStoneItem(STONE_PROPS));
		BLOOD_STONE = REGISTRY.register("blood_stone", () -> new KeeperStoneItem(STONE_PROPS));
		BLUE_FLAME_STONE = REGISTRY.register("blue_flame_stone", () -> new KeeperStoneItem(STONE_PROPS));
		COLORS_STONE = REGISTRY.register("colors_stone", () -> new KeeperStoneItem(STONE_PROPS));
		CREATION_STONE = REGISTRY.register("creation_stone", () -> new KeeperStoneItem(STONE_PROPS));
		CRYSTAL_STONE = REGISTRY.register("crystal_stone", () -> new KeeperStoneItem(STONE_PROPS));
		DARKNESS_STONE = REGISTRY.register("darkness_stone", () -> new KeeperStoneItem(STONE_PROPS));
		DESTRUCTION_STONE = REGISTRY.register("destruction_stone", () -> new KeeperStoneItem(STONE_PROPS));
		DRAGON_STONE = REGISTRY.register("dragon_stone", () -> new KeeperStoneItem(STONE_PROPS));
		EARTH_STONE = REGISTRY.register("earth_stone", () -> new KeeperStoneItem(STONE_PROPS));
		ENERGY_STONE = REGISTRY.register("energy_stone", () -> new KeeperStoneItem(STONE_PROPS));
		ETHER_STONE = REGISTRY.register("ether_stone", () -> new KeeperStoneItem(STONE_PROPS));
		EXPLOSION_STONE = REGISTRY.register("explosion_stone", () -> new KeeperStoneItem(STONE_PROPS));
		FIRE_STONE = REGISTRY.register("fire_stone", () -> new KeeperStoneItem(STONE_PROPS));
		FORM_STONE = REGISTRY.register("form_stone", () -> new KeeperStoneItem(STONE_PROPS));
		GOLDEN_DUST_STONE = REGISTRY.register("golden_dust_stone", () -> new KeeperStoneItem(STONE_PROPS));
		GRAVITY_STONE = REGISTRY.register("gravity_stone", () -> new KeeperStoneItem(STONE_PROPS));
		HEAT_STONE = REGISTRY.register("heat_stone", () -> new KeeperStoneItem(STONE_PROPS));
		ICE_STONE = REGISTRY.register("ice_stone", () -> new KeeperStoneItem(STONE_PROPS));
		LAVA_STONE = REGISTRY.register("lava_stone", () -> new KeeperStoneItem(STONE_PROPS));
		LIGHT_STONE = REGISTRY.register("light_stone", () -> new KeeperStoneItem(STONE_PROPS));
		LIGHTNING_STONE = REGISTRY.register("lightning_stone", () -> new KeeperStoneItem(STONE_PROPS));
		MAGNET_STONE = REGISTRY.register("magnet_stone", () -> new KeeperStoneItem(STONE_PROPS));
		MERCURY_STONE = REGISTRY.register("mercury_stone", () -> new KeeperStoneItem(STONE_PROPS));
		METAL_STONE = REGISTRY.register("metal_stone", () -> new KeeperStoneItem(STONE_PROPS));
		MIND_STONE = REGISTRY.register("mind_stone", () -> new KeeperStoneItem(STONE_PROPS));
		MIST_STONE = REGISTRY.register("mist_stone", () -> new KeeperStoneItem(STONE_PROPS));
		MOON_STONE = REGISTRY.register("moon_stone", () -> new KeeperStoneItem(STONE_PROPS));
		MUSHROOMS_STONE = REGISTRY.register("mushrooms_stone", () -> new KeeperStoneItem(STONE_PROPS));
		MUSIC_STONE = REGISTRY.register("music_stone", () -> new KeeperStoneItem(STONE_PROPS));
		OCEAN_STONE = REGISTRY.register("ocean_stone", () -> new KeeperStoneItem(STONE_PROPS));
		PLAGUE_STONE = REGISTRY.register("plague_stone", () -> new KeeperStoneItem(STONE_PROPS));
		PLANTS_STONE = REGISTRY.register("plants_stone", () -> new KeeperStoneItem(STONE_PROPS));
		POISON_STONE = REGISTRY.register("poison_stone", () -> new KeeperStoneItem(STONE_PROPS));
		RAIN_STONE = REGISTRY.register("rain_stone", () -> new KeeperStoneItem(STONE_PROPS));
		RECHARGE_STONE = REGISTRY.register("recharge_stone", () -> new KeeperStoneItem(STONE_PROPS));
		RECHARGING_STONE = REGISTRY.register("recharging_stone", () -> new KeeperStoneItem(STONE_PROPS));
		SAND_STONE = REGISTRY.register("sand_stone", () -> new KeeperStoneItem(STONE_PROPS));
		SHADOW_STONE = REGISTRY.register("shadow_stone", () -> new KeeperStoneItem(STONE_PROPS));
		SHOCKWAVE_STONE = REGISTRY.register("shockwave_stone", () -> new KeeperStoneItem(STONE_PROPS));
		SMOKE_STONE = REGISTRY.register("smoke_stone", () -> new KeeperStoneItem(STONE_PROPS));
		SOUND_STONE = REGISTRY.register("sound_stone", () -> new KeeperStoneItem(STONE_PROPS));
		SPACE_STONE = REGISTRY.register("space_stone", () -> new KeeperStoneItem(STONE_PROPS));
		SPEED_STONE = REGISTRY.register("speed_stone", () -> new KeeperStoneItem(STONE_PROPS));
		SPIRIT_STONE = REGISTRY.register("spirit_stone", () -> new KeeperStoneItem(STONE_PROPS));
		SUN_STONE = REGISTRY.register("sun_stone", () -> new KeeperStoneItem(STONE_PROPS));
		TEARS_STONE = REGISTRY.register("tears_stone", () -> new KeeperStoneItem(STONE_PROPS));
		TECHNOLOGY_STONE = REGISTRY.register("technology_stone", () -> new KeeperStoneItem(STONE_PROPS));
		TELEPORTATION_STONE = REGISTRY.register("teleportation_stone", () -> new KeeperStoneItem(STONE_PROPS));
		TIME_STONE = REGISTRY.register("time_stone", () -> new KeeperStoneItem(STONE_PROPS));
		TORNADO_STONE = REGISTRY.register("tornado_stone", () -> new KeeperStoneItem(STONE_PROPS));
		VACUUM_STONE = REGISTRY.register("vacuum_stone", () -> new KeeperStoneItem(STONE_PROPS));
		WATER_STONE = REGISTRY.register("water_stone", () -> new KeeperStoneItem(STONE_PROPS));
		REDUCED_STONE_RECHARGE_TIME_RUNE_1 = REGISTRY.register("reduced_stone_recharge_time_rune_1", () -> new Item(RUNE_PROPS));
		REDUCED_STONE_RECHARGE_TIME_RUNE_2 = REGISTRY.register("reduced_stone_recharge_time_rune_2", () -> new Item(RUNE_PROPS));
		REDUCED_STONE_RECHARGE_TIME_RUNE_3 = REGISTRY.register("reduced_stone_recharge_time_rune_3", () -> new Item(RUNE_PROPS));
		KEEPER_BOOK = REGISTRY.register("keeper_book", () -> new Item(BOOK_PROPS));
		FIRE_SWORD = REGISTRY.register("fire_sword", FireSwordItem::new);
		FIRE_ARMOR_HELMET = REGISTRY.register("fire_armor_helmet", FireArmorItem.Helmet::new);
		FIRE_ARMOR_CHESTPLATE = REGISTRY.register("fire_armor_chestplate", FireArmorItem.Chestplate::new);
		FIRE_ARMOR_LEGGINGS = REGISTRY.register("fire_armor_leggings", FireArmorItem.Leggings::new);
		FIRE_ARMOR_BOOTS = REGISTRY.register("fire_armor_boots", FireArmorItem.Boots::new);
	}
}
