package com.auranite.keepersofthestonesthree.event;

import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.util.DamageModificationManager;
import com.auranite.abloom.util.DamageModificationManager.DamageModifier;
import com.auranite.keepersofthestonesthree.init.Kots3Attachments;
import com.auranite.keepersofthestonesthree.item.Kots3Items;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import java.util.Optional;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * Обработчик пассивных и дополнительных навыков огненного камня.
 *
 * Пассивный навык:
 * - При экипировке FIRE_STONE: +3 очка Пламени
 * - Урон по горящему врагу: +1 очко (раз в 3 секунды)
 * - Взрыв огненного резонанса: +2 очка
 *
 * Дополнительный навык:
 * - Уровень Поджигателя повышается до 2, если рядом огонь/лава (10 блоков) или в Незере
 */
public class FlameSkillHandler {

    private static final long DAMAGE_POINT_COOLDOWN_TICKS = 60L; // 3 секунды = 60 тиков

    /**
     * Статический helper для синхронизации очков Пламени с клиентом.
     */
    private static void syncFlamePointsStatic(ServerPlayer player) {
        byte points = (byte) Kots3Attachments.getFlamePoints(player);
        PacketDistributor.sendToPlayer(player, new com.auranite.keepersofthestonesthree.network.FlamePointsSyncPacket(points));
    }

    static {
        // Регистрируем модификатор для перехвата FIRE резонанса
        DamageModificationManager.registerModifier(new DamageModifier() {
            @Override
            public float modifyDamage(LivingEntity target, DamageSource source, float baseDamage, float currentDamage) {
                if (target.level().isClientSide) return currentDamage;

                // Проверяем, что это FIRE урон от резонанса
                // Резонансный FIRE урон имеет msgId "abloom:fire_damage"
                String msgId = source.type().msgId();
                if (msgId == null || !msgId.contains("fire")) return currentDamage;

                // Проверяем, что на цели есть эффект BURN (признак резонанса)
                if (!target.hasEffect(AbloomModEffects.BURN)) return currentDamage;

                // Получаем атакующего
                Entity causingEntity = source.getEntity();
                if (!(causingEntity instanceof ServerPlayer attacker)) return currentDamage;

                // Начисляем 2 очка за FIRE резонанс
                Kots3Attachments.addFlamePoints(attacker, 2);
                syncFlamePointsStatic(attacker);

                return currentDamage;
            }
        }, -1); // Low priority — после Abloom
    }

    // ==================== АКТИВАЦИЯ КАМНЯ ====================

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.level().isClientSide) return;

        if (hasFireStoneEquipped(serverPlayer)) {
            grantInitialFlamePoints(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.level().isClientSide) return;

        if (hasFireStoneEquipped(serverPlayer)) {
            grantInitialFlamePoints(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) return;

        boolean hasStone = hasFireStoneEquipped(serverPlayer);

        if (hasStone) {
            if (Kots3Attachments.getFlamePoints(serverPlayer) == 0) {
                grantInitialFlamePoints(serverPlayer);
            }
        } else {
            if (Kots3Attachments.getFlamePoints(serverPlayer) > 0) {
                Kots3Attachments.setFlamePoints(serverPlayer, 0);
                syncFlamePoints(serverPlayer);
            }
        }
    }

    private void grantInitialFlamePoints(ServerPlayer player) {
        if (Kots3Attachments.getFlamePoints(player) >= 3) return;

        Kots3Attachments.setFlamePoints(player, 3);
        syncFlamePoints(player);

        player.displayClientMessage(Component.translatable("chat.kots.flame_points_granted", 3), true);
    }

    // ==================== УРОН ПО ГОРЯЩЕМУ ВРАГУ ====================

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;

        LivingEntity attacker = getAttacker(event.getSource());
        if (!(attacker instanceof ServerPlayer serverPlayer)) return;

        // Проверить, есть ли на цели эффект BURN от Abloom
        if (!target.hasEffect(AbloomModEffects.BURN)) return;

        // Проверить cooldown
        long currentTime = serverPlayer.level().getGameTime();
        if (!Kots3Attachments.canGainDamagePoint(serverPlayer, currentTime, DAMAGE_POINT_COOLDOWN_TICKS)) return;

        // Начислить очко
        Kots3Attachments.addFlamePoints(serverPlayer, 1);
        Kots3Attachments.setLastDamagePointTime(serverPlayer, currentTime);
        syncFlamePoints(serverPlayer);
    }

    // ==================== ПРОВЕРКА УРОВНЯ ПОДЖИГАТЕЛЯ ====================

    public static int getIgniterLevel(ServerPlayer player) {
        Level level = player.level();

        // Проверка: в Незере?
        if (level.dimension() == Level.NETHER) {
            return 2;
        }

        // Проверка: источники огня/лавы в радиусе 10 блоков
        BlockPos playerPos = player.blockPosition();
        int radius = 10;

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-radius, -radius, -radius),
                playerPos.offset(radius, radius, radius))) {

            BlockState state = level.getBlockState(pos);

            if (state.is(Blocks.FIRE) || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
                return 2;
            }

            if (state.getFluidState().is(Fluids.LAVA)) {
                return 2;
            }
        }

        return 1;
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private LivingEntity getAttacker(DamageSource source) {
        Entity entity = source.getEntity();
        if (entity instanceof LivingEntity living) return living;

        Entity direct = source.getDirectEntity();
        if (direct instanceof LivingEntity living) return living;

        return null;
    }

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

    private void syncFlamePoints(ServerPlayer player) {
        byte points = (byte) Kots3Attachments.getFlamePoints(player);
        PacketDistributor.sendToPlayer(player, new com.auranite.keepersofthestonesthree.network.FlamePointsSyncPacket(points));
    }
}
