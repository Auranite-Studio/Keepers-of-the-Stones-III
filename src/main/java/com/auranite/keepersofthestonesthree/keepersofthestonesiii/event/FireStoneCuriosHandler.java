package com.auranite.keepersofthestonesthree.keepersofthestonesiii.event;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.item.Kots3Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Collection;
import java.util.Optional;

public class FireStoneCuriosHandler {

    private static final String TAG_FIRE_SET_GRANTED = "Kots3FireSetGranted";
    private static final String TAG_ORIGINAL_ARMOR = "Kots3OriginalArmor";

    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final String[] SLOT_NAMES = {"Head", "Chest", "Legs", "Feet"};

    // ==================== PREVENT SWORD DROP ====================

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        ItemEntity droppedItem = event.getEntity();
        ItemStack stack = droppedItem.getItem();

        if (stack.getItem() == Kots3Items.FIRE_SWORD.get()) {
            event.setCanceled(true);
            droppedItem.discard();

            if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
                serverPlayer.getInventory().add(stack);
            }
        }
    }

    // ==================== PLAYER DEATH HANDLER ====================

    @SubscribeEvent
    public void onPlayerDeath(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (serverPlayer.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        CompoundTag data = serverPlayer.getPersistentData();
        boolean hasStoneEquipped = hasFireStoneEquipped(serverPlayer);
        boolean hasOriginalArmorSaved = data.contains(TAG_ORIGINAL_ARMOR);

        if (hasFireArmorEquipped(serverPlayer)) {
            Collection<ItemEntity> droppedItems = event.getDrops();
            droppedItems.removeIf(item -> isFireArmor(item.getItem()));
        }

        if (hasStoneEquipped) {
            dropFireStone(serverPlayer, event.getDrops());
        }

        if (hasOriginalArmorSaved) {
            CompoundTag armorTag = data.getCompound(TAG_ORIGINAL_ARMOR);
            for (int i = 0; i < SLOT_NAMES.length; i++) {
                if (armorTag.contains(SLOT_NAMES[i])) {
                    CompoundTag stackTag = armorTag.getCompound(SLOT_NAMES[i]);
                    ItemStack originalStack = ItemStack.parse(serverPlayer.level().registryAccess(), stackTag).orElse(ItemStack.EMPTY);
                    if (!originalStack.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(
                                serverPlayer.level(),
                                serverPlayer.getX() + (Math.random() - 0.5) * 0.5,
                                serverPlayer.getY() + 0.5,
                                serverPlayer.getZ() + (Math.random() - 0.5) * 0.5,
                                originalStack
                        );
                        event.getDrops().add(itemEntity);
                    }
                }
            }
        }

        removeFireArmorFromSlots(serverPlayer);
    }

    private void dropFireStone(ServerPlayer player, Collection<ItemEntity> drops) {
        Optional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(player);
        if (curios.isPresent()) {
            ICuriosItemHandler handler = curios.get();
            Optional<ICurioStacksHandler> stoneHandler = handler.getStacksHandler("stone");
            if (stoneHandler.isPresent()) {
                IDynamicStackHandler stacks = stoneHandler.get().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (stack.getItem() == Kots3Items.FIRE_STONE.get()) {
                        ItemEntity stoneDrop = new ItemEntity(
                                player.level(), player.getX(), player.getY(), player.getZ(), stack.copy()
                        );
                        drops.add(stoneDrop);
                        stacks.setStackInSlot(i, ItemStack.EMPTY);
                        break;
                    }
                }
            }
        }
    }

    private void removeFireArmorFromSlots(ServerPlayer player) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorStack = player.getItemBySlot(slot);
            if (isFireArmor(armorStack)) {
                player.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    // ==================== PLAYER DEATH/RESPAWN HANDLER ====================

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.getOriginal().level().isClientSide && event.isWasDeath() && event.getOriginal() instanceof ServerPlayer oldPlayer) {
            ServerPlayer newPlayer = (ServerPlayer) event.getEntity();

            CompoundTag oldData = oldPlayer.getPersistentData();
            CompoundTag newData = newPlayer.getPersistentData();

            if (oldData.contains(TAG_ORIGINAL_ARMOR)) {
                newData.put(TAG_ORIGINAL_ARMOR, oldData.getCompound(TAG_ORIGINAL_ARMOR).copy());
            }
            if (oldData.contains(TAG_FIRE_SET_GRANTED)) {
                newData.putBoolean(TAG_FIRE_SET_GRANTED, oldData.getBoolean(TAG_FIRE_SET_GRANTED));
            }

            if (oldPlayer.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                return;
            }
        }
    }

    // ==================== PLAYER LOGIN CHECK ====================

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (hasFireStoneEquipped(serverPlayer)) {
            CompoundTag data = serverPlayer.getPersistentData();
            boolean hasStoneFlag = data.getBoolean(TAG_FIRE_SET_GRANTED);
            boolean hasOriginalArmor = data.contains(TAG_ORIGINAL_ARMOR);

            if (hasStoneFlag && !hasOriginalArmor && hasFireArmorEquipped(serverPlayer)) {
                removeFireArmorFromSlots(serverPlayer);
                data.remove(TAG_FIRE_SET_GRANTED);
                data.remove(TAG_ORIGINAL_ARMOR);
            } else if (!hasStoneFlag) {
                grantFireSet(serverPlayer);
            }
        }
    }

    // ==================== TICK CHECK ====================

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        boolean hasStoneEquipped = hasFireStoneEquipped(serverPlayer);

        if (hasStoneEquipped) {
            grantFireSet(serverPlayer);
        } else {
            removeFireSet(serverPlayer);
        }
    }

    // ==================== GRANT FIRE SET ====================

    private void grantFireSet(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();

        if (data.getBoolean(TAG_FIRE_SET_GRANTED)) {
            return;
        }

        // Edge case: fire armor is already equipped but original armor not saved
        if (hasFireArmorEquipped(player)) {
            data.putBoolean(TAG_FIRE_SET_GRANTED, true);
            return;
        }

        // First time granting - save original armor and equip fire set
        saveOriginalArmor(player);
        data.putBoolean(TAG_FIRE_SET_GRANTED, true);
        equipFireArmorOnce(player);
    }

    private void equipFireArmorOnce(ServerPlayer player) {
        player.setItemSlot(EquipmentSlot.HEAD, createEnchantedArmor(player, Kots3Items.FIRE_ARMOR_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, createEnchantedArmor(player, Kots3Items.FIRE_ARMOR_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, createEnchantedArmor(player, Kots3Items.FIRE_ARMOR_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, createEnchantedArmor(player, Kots3Items.FIRE_ARMOR_BOOTS.get()));

        ItemStack sword = new ItemStack(Kots3Items.FIRE_SWORD.get());
        sword.enchant(player.level().holderOrThrow(Enchantments.BINDING_CURSE), 1);
        sword.enchant(player.level().holderOrThrow(Enchantments.VANISHING_CURSE), 1);
        player.getInventory().add(sword);
    }

    private void saveOriginalArmor(ServerPlayer player) {
        if (hasFireArmorEquipped(player)) {
            return;
        }

        CompoundTag armorTag = new CompoundTag();
        for (int i = 0; i < SLOT_NAMES.length; i++) {
            ItemStack originalStack = player.getItemBySlot(ARMOR_SLOTS[i]);
            if (!originalStack.isEmpty() && !isFireArmor(originalStack)) {
                CompoundTag stackTag = (CompoundTag) originalStack.save(player.level().registryAccess());
                armorTag.put(SLOT_NAMES[i], stackTag);
            }
        }

        player.getPersistentData().put(TAG_ORIGINAL_ARMOR, armorTag);
    }

    private void restoreOriginalArmor(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (data.contains(TAG_ORIGINAL_ARMOR)) {
            CompoundTag armorTag = data.getCompound(TAG_ORIGINAL_ARMOR);

            for (int i = 0; i < SLOT_NAMES.length; i++) {
                if (armorTag.contains(SLOT_NAMES[i])) {
                    ItemStack restored = ItemStack.parse(player.level().registryAccess(), armorTag.getCompound(SLOT_NAMES[i])).orElse(ItemStack.EMPTY);
                    player.setItemSlot(ARMOR_SLOTS[i], restored);
                } else {
                    player.setItemSlot(ARMOR_SLOTS[i], ItemStack.EMPTY);
                }
            }
            data.remove(TAG_ORIGINAL_ARMOR);
        }
    }

    // ==================== REMOVE FIRE SET ====================

    private void removeFireSet(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();

        removeFireArmorFromSlots(player);

        if (data.contains(TAG_ORIGINAL_ARMOR)) {
            restoreOriginalArmor(player);
        }

        data.remove(TAG_FIRE_SET_GRANTED);
        data.remove(TAG_ORIGINAL_ARMOR);

        removeFireSword(player);
    }

    // ==================== HELPER METHODS ====================

    private ItemStack createEnchantedArmor(ServerPlayer player, net.minecraft.world.item.Item armorItem) {
        ItemStack stack = new ItemStack(armorItem);
        stack.enchant(player.level().holderOrThrow(Enchantments.BINDING_CURSE), 1);
        stack.enchant(player.level().holderOrThrow(Enchantments.VANISHING_CURSE), 1);
        return stack;
    }

    private boolean isFireArmor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() == Kots3Items.FIRE_ARMOR_HELMET.get()
                || stack.getItem() == Kots3Items.FIRE_ARMOR_CHESTPLATE.get()
                || stack.getItem() == Kots3Items.FIRE_ARMOR_LEGGINGS.get()
                || stack.getItem() == Kots3Items.FIRE_ARMOR_BOOTS.get();
    }

    private boolean hasFireArmorEquipped(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (isFireArmor(armorStack)) {
                return true;
            }
        }
        return false;
    }

    private void removeFireSword(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Kots3Items.FIRE_SWORD.get()) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean hasFireStoneEquipped(ServerPlayer player) {
        Optional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(player);
        if (curios.isPresent()) {
            ICuriosItemHandler handler = curios.get();
            Optional<ICurioStacksHandler> stoneHandler = handler.getStacksHandler("stone");
            if (stoneHandler.isPresent()) {
                IDynamicStackHandler stacks = stoneHandler.get().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() == Kots3Items.FIRE_STONE.get()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}