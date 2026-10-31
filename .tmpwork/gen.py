import os, json, glob

os.chdir('/workspace')

texdir = 'src/main/resources/assets/kots/textures/item'
stones = sorted(f[:-4] for f in os.listdir(texdir) if f.endswith('_stone.png'))
runes = ['reduced_stone_recharge_time_rune_1', 'reduced_stone_recharge_time_rune_2', 'reduced_stone_recharge_time_rune_3']
others = ['keeper_book', 'fire_sword', 'fire_armor_helmet', 'fire_armor_chestplate', 'fire_armor_leggings', 'fire_armor_boots']
all_items = ['fire_stone'] + [s for s in stones if s != 'fire_stone'] + runes + others

fields = "\n".join("\tpublic static final DeferredItem<Item> %s;" % n.upper() for n in all_items)
regs = "\n".join('\t\t%s = REGISTRY.register("%s", () -> new Item(STONE_PROPS));' % (n.upper(), n) for n in stones)
rs = []
for i in (1, 2, 3):
    rs.append('\t\tREDUCED_STONE_RECHARGE_TIME_RUNE_%d = REGISTRY.register("reduced_stone_recharge_time_rune_%d", () -> new Item(RUNE_PROPS));' % (i, i))
rs.append('\t\tKEEPER_BOOK = REGISTRY.register("keeper_book", () -> new Item(BOOK_PROPS));')
rs.append('\t\tFIRE_SWORD = REGISTRY.register("fire_sword", FireSwordItem::new);')
armor = [('FIRE_ARMOR_HELMET', 'fire_armor_helmet', 'Helmet'), ('FIRE_ARMOR_CHESTPLATE', 'fire_armor_chestplate', 'Chestplate'),
         ('FIRE_ARMOR_LEGGINGS', 'fire_armor_leggings', 'Leggings'), ('FIRE_ARMOR_BOOTS', 'fire_armor_boots', 'Boots')]
for f_, idn, t in armor:
    rs.append('\t\t%s = REGISTRY.register("%s", FireArmorItem.%s::new);' % (f_, idn, t))
regs_special = "\n".join(rs)

java = """package com.auranite.keepersofthestonesthree.keepersofthestonesiii.item;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.KeepersOfTheStonesIIIMod;
import com.auranite.keepersofthestonesthree.keepersofthestonesiii.effect.FireArmorItem;
import com.auranite.keepersofthestonesthree.keepersofthestonesiii.effect.FireSwordItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers every item of the mod: elemental stones, recharge runes,
 * the Keeper Book and the fire equipment set.
 */
public class Kots3Items {
\tpublic static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(KeepersOfTheStonesIIIMod.MODID);

""" + fields + """

\tprivate static final Item.Properties STONE_PROPS = new Item.Properties()
\t\t\t.stacksTo(1)
\t\t\t.rarity(Rarity.RARE);

\tprivate static final Item.Properties RUNE_PROPS = new Item.Properties()
\t\t\t.stacksTo(16)
\t\t\t.rarity(Rarity.UNCOMMON);

\tprivate static final Item.Properties BOOK_PROPS = new Item.Properties()
\t\t\t.stacksTo(1)
\t\t\t.rarity(Rarity.EPIC);

\tstatic {
""" + regs + """
""" + regs_special + """
\t}
}
"""
open('src/main/java/com/auranite/keepersofthestonesthree/keepersofthestonesiii/item/Kots3Items.java', 'w').write(java)

accepts = "\n".join("\t\t\t\t\t\ttabData.accept(Kots3Items.%s.get());" % n.upper() for n in all_items)
tabs = """package com.auranite.keepersofthestonesthree.keepersofthestonesiii.item;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.KeepersOfTheStonesIIIMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Kots3Tabs {
\tpublic static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KeepersOfTheStonesIIIMod.MODID);

\tpublic static final DeferredHolder<CreativeModeTab, CreativeModeTab> KOTS_TAB = REGISTRY.register("stones",
\t\t\t() -> CreativeModeTab.builder()
\t\t\t\t\t.title(Component.translatable("item_group.kots.stones"))
\t\t\t\t\t.icon(() -> new ItemStack(Kots3Items.FIRE_STONE.get()))
\t\t\t\t\t.displayItems((parameters, tabData) -> {
""" + accepts + """
\t\t\t\t\t})
\t\t\t\t\t.build());
}
"""
open('src/main/java/com/auranite/keepersofthestonesthree/keepersofthestonesiii/item/Kots3Tabs.java', 'w').write(tabs)

# ---- verification ----
en = json.load(open('src/main/resources/assets/kots/lang/en_us.json', encoding='utf-8'))
ru = json.load(open('src/main/resources/assets/kots/lang/ru_ru.json', encoding='utf-8'))
print("items:", len(all_items))
print("missing lang en:", [n for n in all_items if 'item.kots.' + n not in en])
print("missing lang ru:", [n for n in all_items if 'item.kots.' + n not in ru])
print("missing models:", [n for n in all_items if not os.path.exists('src/main/resources/assets/kots/models/item/%s.json' % n)])
print("missing textures:", [n for n in all_items if not os.path.exists('%s/%s.png' % (texdir, n))])
bad = []
for f in glob.glob('src/**/*.json', recursive=True):
    raw = open(f, 'rb').read()
    try:
        json.loads(raw.decode('utf-8'))
    except Exception as e:
        bad.append((f, str(e)[:60]))
print("invalid json:", bad)
