
package com.auranite.keepersofthestonesthree.keepersofthestonesiii.item;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.KeepersOfTheStonesIIIMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Kots3Items {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(KeepersOfTheStonesIIIMod.MODID);

    public static final DeferredItem<Item> FIRE_STONE;
    public static final DeferredItem<Item> FIRE_SWORD;
    public static final DeferredItem<Item> FIRE_ARMOR_HELMET;
    public static final DeferredItem<Item> FIRE_ARMOR_CHESTPLATE;
    public static final DeferredItem<Item> FIRE_ARMOR_LEGGINGS;
    public static final DeferredItem<Item> FIRE_ARMOR_BOOTS;

    private static final Item.Properties STONE_PROPS = new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.RARE);

    static {
        FIRE_STONE     = REGISTRY.register("fire_stone", () -> new Item(STONE_PROPS));
        FIRE_SWORD = REGISTRY.register("fire_sword", FireSwordItem::new);
        FIRE_ARMOR_HELMET = REGISTRY.register("fire_armor_helmet", FireArmorItem.Helmet::new);
        FIRE_ARMOR_CHESTPLATE = REGISTRY.register("fire_armor_chestplate", FireArmorItem.Chestplate::new);
        FIRE_ARMOR_LEGGINGS = REGISTRY.register("fire_armor_leggings", FireArmorItem.Leggings::new);
        FIRE_ARMOR_BOOTS = REGISTRY.register("fire_armor_boots", FireArmorItem.Boots::new);
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
        return block(block, new Item.Properties());
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
        return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }
}
