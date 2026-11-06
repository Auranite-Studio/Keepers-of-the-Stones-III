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

    // ==================== PLAYER DEATH HANDLER ====================

    @SubscribeEvent
    public void onPlayerDeath(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Если keepInventory включен, игрок сохраняет всё естественно.
        // Мы просто выходим, не трогая ни дроп, ни теги, ни слоты.
        if (serverPlayer.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        CompoundTag data = serverPlayer.getPersistentData();
        boolean hasStoneEquipped = hasFireStoneEquipped(serverPlayer);
        boolean hasOriginalArmorSaved = data.contains(TAG_ORIGINAL_ARMOR);

        // 1. Remove fire armor from drops (don't drop it)
        if (hasFireArmorEquipped(serverPlayer)) {
            Collection<ItemEntity> droppedItems = event.getDrops();
            droppedItems.removeIf(item -> isFireArmor(item.getItem()));
        }

        // 2. Drop fire stone instead of deleting it forever
        if (hasStoneEquipped) {
            dropFireStone(serverPlayer, event.getDrops());
        }

        // 3. Drop original armor if saved
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

        // 4. Remove fire armor from player slots (ensures clean state for original armor restore)
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

            // ИСПРАВЛЕНО: Копируем теги состояния ВСЕГДА, независимо от keepInventory.
            // keepInventory влияет только на физический дроп, но логическое состояние
            // системы (какая броня была оригинальной) должно мигрировать на нового игрока.
            if (oldData.contains(TAG_ORIGINAL_ARMOR)) {
                newData.put(TAG_ORIGINAL_ARMOR, oldData.getCompound(TAG_ORIGINAL_ARMOR).copy());
            }
            if (oldData.contains(TAG_FIRE_SET_GRANTED)) {
                newData.putBoolean(TAG_FIRE_SET_GRANTED, oldData.getBoolean(TAG_FIRE_SET_GRANTED));
            }

            // Если включен keepInventory, мы не должны ничего удалять или дропать.
            // Теги уже скопированы, система продолжит работать корректно.
            if (oldPlayer.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                return;
            }

            // Дальнейшая логика дропа в Clone не нужна, так как она полностью
            // и корректно обрабатывается в LivingDropsEvent.
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

            // Защита от вечной блокировки в огненной броне при потере NBT (сбой сервера)
            if (hasStoneFlag && !hasOriginalArmor && hasFireArmorEquipped(serverPlayer)) {
                removeFireArmorFromSlots(serverPlayer);
                removeFireSword(serverPlayer);
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
            // Already granted - just ensure armor is equipped safely (without resetting durability)
            equipFireArmorSafely(player);
            return;
        }

        // Edge case: fire armor is already equipped but original armor not saved (e.g., after reload)
        if (hasFireArmorEquipped(player)) {
            data.putBoolean(TAG_FIRE_SET_GRANTED, true);
            return;
        }

        // First time granting - save original armor
        saveOriginalArmor(player);
        data.putBoolean(TAG_FIRE_SET_GRANTED, true);
        equipFireArmorSafely(player);
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

    private void equipFireArmorSafely(ServerPlayer player) {
        equipSlotIfMissingOrWrong(player, EquipmentSlot.HEAD, Kots3Items.FIRE_ARMOR_HELMET.get());
        equipSlotIfMissingOrWrong(player, EquipmentSlot.CHEST, Kots3Items.FIRE_ARMOR_CHESTPLATE.get());
        equipSlotIfMissingOrWrong(player, EquipmentSlot.LEGS, Kots3Items.FIRE_ARMOR_LEGGINGS.get());
        equipSlotIfMissingOrWrong(player, EquipmentSlot.FEET, Kots3Items.FIRE_ARMOR_BOOTS.get());

        if (!hasFireSwordInInventory(player)) {
            ItemStack sword = new ItemStack(Kots3Items.FIRE_SWORD.get());
            sword.enchant(player.level().holderOrThrow(Enchantments.BINDING_CURSE), 1);
            sword.enchant(player.level().holderOrThrow(Enchantments.VANISHING_CURSE), 1);
            player.getInventory().add(sword);
        }
    }

    private void equipSlotIfMissingOrWrong(ServerPlayer player, EquipmentSlot slot, net.minecraft.world.item.Item expectedItem) {
        ItemStack current = player.getItemBySlot(slot);
        // Если слот уже содержит правильный предмет, НЕ перезаписываем его (сохраняем прочность и NBT)
        if (current.getItem() == expectedItem) {
            return;
        }
        player.setItemSlot(slot, createEnchantedArmor(player, expectedItem));
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

    private boolean hasFireSwordInInventory(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Kots3Items.FIRE_SWORD.get()) {
                return true;
            }
        }
        return false;
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