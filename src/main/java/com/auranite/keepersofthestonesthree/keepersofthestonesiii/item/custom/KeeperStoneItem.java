package com.auranite.keepersofthestonesthree.keepersofthestonesiii.item.custom;

import com.auranite.keepersofthestonesthree.keepersofthestonesiii.component.Kots3DataComponents;
import com.auranite.keepersofthestonesthree.keepersofthestonesiii.effect.Kots3Effects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * An elemental keeper stone. Right-click activates the Master effect for 10 minutes
 * and grants the master equipment (see {@link com.auranite.keepersofthestonesthree.keepersofthestonesiii.handler.StoneMasterHandler}).
 * Deactivation is bound to the U key. After deactivation or expiry the stone goes on recharge.
 */
public class KeeperStoneItem extends Item {
    /** Master effect duration: 10 minutes = 12000 ticks. */
    public static final int MASTER_DURATION_TICKS = 12000;
    /** Recharge time after the master effect ends: 5 minutes = 6000 ticks. */
    public static final long RECHARGE_TICKS = 6000;

    public KeeperStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (isActive(stack)) {
                // Right-clicking an active stone deactivates it manually.
                deactivate(level, player, stack);
            } else if (isRecharging(stack, level.getGameTime())) {
                long remainingSeconds = (getRechargeUntil(stack) - level.getGameTime()) / 20;
                player.displayClientMessage(Component.translatable("msg.kots.stone.recharging",
                                String.format("%d:%02d", remainingSeconds / 60, remainingSeconds % 60))
                        .withStyle(ChatFormatting.GOLD), true);
                return InteractionResultHolder.fail(stack);
            } else {
                activate(level, player, stack);
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, isActive(stack) ? 0.6f : 1.4f);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** Activates the stone: grants the Master effect for 10 minutes. */
    public static void activate(Level level, Player player, ItemStack stack) {
        stack.set(Kots3DataComponents.STONE_ACTIVE.get(), Boolean.TRUE);
        stack.remove(Kots3DataComponents.STONE_RECHARGE_UNTIL.get());
        player.addEffect(new MobEffectInstance(Kots3Effects.fireMaster(), MASTER_DURATION_TICKS, 0, false, true, true));
    }

    /** Deactivates the stone (U key or right-click) and starts the recharge phase. */
    public static void deactivate(Level level, Player player, ItemStack stack) {
        stack.remove(Kots3DataComponents.STONE_ACTIVE.get());
        startRecharge(stack, level.getGameTime());
        player.removeEffect(Kots3Effects.fireMaster());
    }

    /** Puts the stone into its recharge cooldown. */
    public static void startRecharge(ItemStack stack, long gameTime) {
        stack.set(Kots3DataComponents.STONE_RECHARGE_UNTIL.get(), gameTime + RECHARGE_TICKS);
    }

    public static boolean isActive(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(Kots3DataComponents.STONE_ACTIVE.get()));
    }

    public static boolean isRecharging(ItemStack stack, long gameTime) {
        Long until = stack.get(Kots3DataComponents.STONE_RECHARGE_UNTIL.get());
        return until != null && until > gameTime;
    }

    public static long getRechargeUntil(ItemStack stack) {
        Long until = stack.get(Kots3DataComponents.STONE_RECHARGE_UNTIL.get());
        return until == null ? 0L : until;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (isActive(stack)) {
            tooltip.add(Component.translatable("tooltip.kots.stone.active").withStyle(ChatFormatting.RED));
        } else if (stack.has(Kots3DataComponents.STONE_RECHARGE_UNTIL.get())) {
            tooltip.add(Component.translatable("tooltip.kots.stone.recharging").withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.translatable("tooltip.kots.stone.ready").withStyle(ChatFormatting.GREEN));
        }
    }
}
