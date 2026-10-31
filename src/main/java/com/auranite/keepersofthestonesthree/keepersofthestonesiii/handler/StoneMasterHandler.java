package com.auranite.keepersofthestonesthree.keepersofthestonesiii.handler;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.component.Kots3DataComponents;
import com.auranite.keepersofthestonesthree.keepersofthestonesiii.effect.FireArmorItem;
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
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

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

    /**
     * Remembered positions of the locked stacks per player:
     * identity hash of the ItemStack -> inventory slot index (see {@link #enforceLockedSlots}).
     */
    private static final Map<ServerPlayer, Map<Integer, Integer>> LOCKED_POSITIONS = new WeakHashMap<>();

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
            LOCKED_POSITIONS.remove(player);
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
                if (!player.hasEffect(Kots3Effects.fireMaster())) {
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
            LOCKED_POSITIONS.remove(player);
        }

        if (expired) {
            LOCKED_POSITIONS.remove(player);
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
        LOCKED_POSITIONS.remove(player);
    }

    /** True for equipment issued by an activated stone. */
    public static boolean isMasterGear(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        if (stack.is(Kots3Items.FIRE_SWORD.get()))
            return true;
        return stack.getItem() instanceof ArmorItem armor
                && armor.getMaterial() == FireArmorItem.ARMOR_MATERIAL;
    }

    /**
     * Slot locking implemented as server-side position enforcement: every tick we know
     * which stacks must be locked (the active stone and the issued master gear while the
     * effect runs). Their slots are remembered; if any interaction (click-move, drag,
     * number-key swap, Q-drop, offhand swap...) relocates or removes such a stack, it is
     * snapped back to its original slot on the very next tick - effectively blocking the
     * locked items from leaving their slots.
     */
    private static void enforceLockedSlots(ServerPlayer player) {
        Inventory inv = player.getInventory();
        boolean masterActive = player.hasEffect(Kots3Effects.fireMaster());
        Map<Integer, Integer> remembered = LOCKED_POSITIONS.computeIfAbsent(player, p -> new HashMap<>());

        // 1) Snap back: every remembered locked stack must sit in its remembered slot.
        remembered.values().removeIf(slot -> !inv.getItem(slot).isEmpty());
        remembered.entrySet().removeIf(e -> !isSameStack(inv.getItem(e.getValue()), e.getKey()));
        for (Map.Entry<Integer, Integer> e : remembered.entrySet()) {
            int id = e.getKey();
            int slot = e.getValue();
            int current = findStackById(inv, id);
            if (current >= 0 && current != slot) {
                // Move the stack back and restore whatever occupied the target slot.
                ItemStack displaced = inv.getItem(slot);
                inv.setItem(slot, inv.getItem(current));
                inv.setItem(current, displaced);
            } else if (current < 0) {
                // The stack left the inventory (e.g. dropped): give it another copy here.
                ItemStack replacement = replaceableStack(inv, id, slot);
                if (replacement != null) {
                    inv.setItem(slot, replacement);
                }
            }
        }

        // 2) Rebuild the lock map for the currently locked stacks.
        remembered.clear();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (isLockedStack(player, stack, masterActive)) {
                remembered.put(System.identityHashCode(stack), i);
            }
        }
    }

    /** The active stone may not leave the inventory at all; gear may only leave via death. */
    private static ItemStack replaceableStack(Inventory inv, int id, int slot) {
        ItemStack inSlot = inv.getItem(slot);
        if (inSlot.getItem() instanceof KeeperStoneItem) {
            return new ItemStack(inSlot.getItem());
        }
        if (isMasterGear(inSlot)) {
            return new ItemStack(inSlot.getItem());
        }
        return null;
    }

    private static int findStackById(Inventory inv, int id) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && System.identityHashCode(stack) == id)
                return i;
        }
        return -1;
    }

    private static boolean isSameStack(ItemStack stack, int id) {
        return !stack.isEmpty() && System.identityHashCode(stack) == id;
    }

    private static boolean isLockedStack(Player player, ItemStack stack, boolean masterActive) {
        if (stack.isEmpty())
            return false;
        if (stack.getItem() instanceof KeeperStoneItem && KeeperStoneItem.isActive(stack))
            return true;
        return masterActive && isMasterGear(stack);
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
        enforceLockedSlots(player);
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
