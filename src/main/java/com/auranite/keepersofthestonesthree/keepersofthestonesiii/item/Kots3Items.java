
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

    private static final Item.Properties STONE_PROPS = new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.RARE);

    static {
        FIRE_STONE     = REGISTRY.register("fire_stone", () -> new Item(STONE_PROPS));
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
        return block(block, new Item.Properties());
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
        return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }
}
