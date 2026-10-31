package com.auranite.keepersofthestonesthree.keepersofthestonesiii.handler;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.component.Kots3DataComponents;
import com.auranite.keepersofthestonesthree.keepersofthestonesiii.effect.Kots3Effects;
import com.auranite.keepersofthestonesthree.keepersofthestonesiii.item.Kots3Items;
import com.auranite.keepersofthestonesthree.keepersofthestonesiii.item.custom.KeeperStoneItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/**
 * Server-side logic of the "Keeper Master" mechanic:
 * <ul>
 *   <li>activating a stone grants the Master effect for 10 minutes and equips
 *       the master set (fire sword + fire armor);</li>
 *   <li>stone-issued items (and the active stone itself) are locked in their slots;</li>
 *   <li>on death an active stone is returned to the player's inventory;</li>
 *   <li>when the effect expires or the stone is deactivated, the stone goes on recharge.</li>
 * </ul>
 */
@EventBusSubscriber(modid = "kots")
public class StoneMasterHandler {

    /** Deactivates every active stone the player carries (triggered by the U key packet). */
    public static void deactivateAllStones(Player player) {
        if (player.level().isClientSide)
            return;
        Inventory inv = player.getInventory();
        boolean found = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof KeeperStoneItem && KeeperStoneItem.isActive(stack)) {
                KeeperStoneItem.deactivate(player.level(), player, stack);
                found = true;
            }
        }
        if (found) {
            clearMasterEquipment(player);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.0f, 0.5f);
            player.displayClientMessage(Component.translatable("msg.kots.stone.deactivated")
                    .withStyle(ChatFormatting.GOLD), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        Inventory inv = player.getInventory();
        long gameTime = player.level().getGameTime();
        boolean hasActiveStone = false;
        boolean expired = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!(stack.getItem() instanceof KeeperStoneItem))
                continue;
            if (KeeperStoneItem.isActive(stack)) {
                stack.remove(Kots3DataComponents.STONE_RECHARGE_UNTIL.get());
                if (!player.hasEffect(Kots3Effects.FIRE_MASTER.get())) {
                    // The 10-minute master effect ran out -> stone switches to recharge.
                    stack.remove(Kots3DataComponents.STONE_ACTIVE.get());
                    KeeperStoneItem.startRecharge(stack, gameTime);
                    expired = true;
                } else {
                    hasActiveStone = true;
                }
            } else {
                Long until = stack.get(Kots3DataComponents.STONE_RECHARGE_UNTIL.get());
                if (until != null && until <= gameTime) {
                    // Recharge finished: the stone is ready for activation again.
                    stack.remove(Kots3DataComponents.STONE_RECHARGE_UNTIL.get());
                }
            }
        }

        if (hasActiveStone) {
            grantAndLockMasterEquipment(player);
        } else if (hasMasterEquipment(player)) {
            clearMasterEquipment(player);
        }

        if (expired) {
            player.displayClientMessage(Component.translatable("msg.kots.stone.expired")
                    .withStyle(ChatFormatting.GOLD), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.0f, 0.7f);
        }
    }

    /**
     * On death the active stone is never lost: it stays in the player's inventory,
     * gets deactivated and immediately switched into its recharge phase.
     */
    @SubscribeEvent
    public static void onDeathDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof KeeperStoneItem && KeeperStoneItem.isActive(stack)) {
                stack.remove(Kots3DataComponents.STONE_ACTIVE.get());
                KeeperStoneItem.startRecharge(stack, player.level().getGameTime());
            }
        }
    }

    /**
     * Slot locking: any click (pickup, quick-move, number swap, drag, offhand swap)
     * that touches a locked stack is cancelled server-side. Locked stacks can neither
     * leave their slot nor be swapped with something else.
     */
    @SubscribeEvent
    public static void onContainerClick(PlayerContainerEvent.Click event) {
        ServerPlayer player = event.getEntity();
        Slot slot = findSlot(player, event.getContainerId(), event.getSlotId());
        if (slot == null)
            return;
        ItemStack slotStack = slot.getItem();
        ItemStack cursor = player.containerMenu.getCarried();
        boolean slotLocked = isLockedStack(player, slotStack);
        boolean cursorLocked = isLockedStack(player, cursor);
        if (slotLocked || cursorLocked) {
            // Pure external drop into an empty, unlocked slot with an unlocked cursor
            // never reaches this branch; anything involving a locked stack is refused.
            event.setCanceled(true);
        }
    }

    /** Cancel dropping locked equipment / active stones with the Q key. */
    @SubscribeEvent
    public static void onDropItem(PlayerEvent.Drop event) {
        if (event.getEntity() instanceof ServerPlayer player && isLockedStack(player, event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    private static Slot findSlot(ServerPlayer player, int containerId, int slotId) {
        AbstractContainerMenu menu = containerId == 0
                ? player.inventoryMenu
                : (player.containerMenu != null
                        && player.containerMenu.containerId == containerId ? player.containerMenu : null);
        if (menu == null)
            return null;
        List<Slot> slots = menu.slots;
        return slotId >= 0 && slotId < slots.size() ? slots.get(slotId) : null;
    }

    /** Locking also applies to hotbar selection changes: re-grant happens each tick anyway. */
    private static boolean isLockedStack(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty())
            return false;
        if (stack.getItem() instanceof KeeperStoneItem && KeeperStoneItem.isActive(stack))
            return true;
        return isMasterGear(stack) && player.hasEffect(Kots3Effects.FIRE_MASTER.get());
    }

    /** True for equipment issued by an activated stone. */
    public static boolean isMasterGear(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        if (stack.is(Kots3Items.FIRE_SWORD.get()))
            return true;
        return stack.getItem() instanceof ArmorItem armor
                && armor.getMaterial() == com.auranite.keepersofthestonesthree.keepersofthestonesiii.effect.FireArmorItem.ARMOR_MATERIAL;
    }

    private static void grantAndLockMasterEquipment(ServerPlayer player) {
        Inventory inv = player.getInventory();
        int handSlot = inv.selected;
        if (!inv.getItem(handSlot).is(Kots3Items.FIRE_SWORD.get())) {
            inv.setItem(handSlot, new ItemStack(Kots3Items.FIRE_SWORD.get()));
        }
        setArmorIfMissing(player, EquipmentSlot.HEAD, Kots3Items.FIRE_ARMOR_HELMET.get());
        setArmorIfMissing(player, EquipmentSlot.CHEST, Kots3Items.FIRE_ARMOR_CHESTPLATE.get());
        setArmorIfMissing(player, EquipmentSlot.LEGS, Kots3Items.FIRE_ARMOR_LEGGINGS.get());
        setArmorIfMissing(player, EquipmentSlot.FEET, Kots3Items.FIRE_ARMOR_BOOTS.get());
    }

    private static void setArmorIfMissing(ServerPlayer player, EquipmentSlot slot, Item item) {
        if (!player.getItemBySlot(slot).is(item)) {
            player.setItemSlot(slot, new ItemStack(item));
        }
    }

    private static boolean hasMasterEquipment(Player player) {
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(Kots3Items.FIRE_ARMOR_HELMET.get())
                || player.getItemBySlot(EquipmentSlot.CHEST).is(Kots3Items.FIRE_ARMOR_CHESTPLATE.get())
                || player.getItemBySlot(EquipmentSlot.LEGS).is(Kots3Items.FIRE_ARMOR_LEGGINGS.get())
                || player.getItemBySlot(EquipmentSlot.FEET).is(Kots3Items.FIRE_ARMOR_BOOTS.get())) {
            return true;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(Kots3Items.FIRE_SWORD.get()))
                return true;
        }
        return false;
    }

    private static void clearMasterEquipment(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(Kots3Items.FIRE_SWORD.get()))
                inv.setItem(i, ItemStack.EMPTY);
        }
        removeArmorPiece(player, Kots3Items.FIRE_ARMOR_HELMET.get());
        removeArmorPiece(player, Kots3Items.FIRE_ARMOR_CHESTPLATE.get());
        removeArmorPiece(player, Kots3Items.FIRE_ARMOR_LEGGINGS.get());
        removeArmorPiece(player, Kots3Items.FIRE_ARMOR_BOOTS.get());
    }

    private static void removeArmorPiece(Player player, Item item) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (player.getItemBySlot(slot).is(item))
                player.setItemSlot(slot, ItemStack.EMPTY);
        }
    }
}
