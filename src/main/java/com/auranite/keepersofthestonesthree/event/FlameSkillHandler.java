package com.auranite.keepersofthestonesthree.event;

import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.keepersofthestonesthree.init.Kots3Attachments;
import com.auranite.keepersofthestonesthree.item.Kots3Items;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
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
    private static final long RESONANCE_COOLDOWN_TICKS = 60L;    // 3 секунды на резонансные очки

    /**
     * Кулдаун начисления очков за огненный резонанс (на пару атакующий -> цель).
     */
    private static final Map<String, Long> LAST_RESONANCE_TIME = new ConcurrentHashMap<>();

    /**
     * Статический helper для синхронизации очков Пламени с клиентом.
     */
    private static void syncFlamePointsStatic(ServerPlayer player) {
        byte points = (byte) Kots3Attachments.getFlamePoints(player);
        PacketDistributor.sendToPlayer(player, new com.auranite.keepersofthestonesthree.network.FlamePointsSyncPacket(points));
    }

    /**
     * Помечает цель как поражённую огненным уроном (кандидат на огненный резонанс).
     * Используется для начисления очков при фактических взрывах, причинённых игроком.
     */
    public static void markFireHit(ServerPlayer attacker, LivingEntity target) {
        if (attacker == null || target == null || target.level().isClientSide) return;
        LAST_RESONANCE_TIME.putIfAbsent(resonanceKey(attacker, target), -1L); // помечен, ещё не начислено
    }

    private static String resonanceKey(ServerPlayer attacker, LivingEntity target) {
        return attacker.getUUID() + ":" + target.getUUID();
    }

    /**
     * Пробует начислить 2 очка Пламени за взрыв огненного резонанса на цели.
     * Срабатывает один раз в RESONANCE_COOLDOWN_TICKS (3 секунды) на пару атакующий-цель.
     */
    private static boolean tryGrantResonancePoints(ServerPlayer attacker, LivingEntity target) {
        long gameTime = target.level().getGameTime();
        String key = resonanceKey(attacker, target);
        Long last = LAST_RESONANCE_TIME.get(key);
        if (last != null && last >= 0 && gameTime - last < RESONANCE_COOLDOWN_TICKS) return false; // кулдаун 3 сек
        LAST_RESONANCE_TIME.put(key, gameTime);
        Kots3Attachments.addFlamePoints(attacker, 2);
        syncFlamePointsStatic(attacker);
        return true;
    }

    /**
     * Начисляет 2 очка Пламени при срабатывании взрыва огненного резонанса Abloom:
     * FIRE-урон по цели, у которой есть эффект поджога (BURN). Вызывается из FlameTongueAbility
     * сразу после dealElementDamage, пока эффект ещё висит на цели.
     */
    public static void onFireResonanceExplosion(ServerPlayer attacker, LivingEntity target) {
        if (attacker == null || target == null || target.level().isClientSide) return;
        if (!target.hasEffect(AbloomModEffects.BURN)) return;
        tryGrantResonancePoints(attacker, target);
    }

    // ==================== АКТИВАЦИЯ КАМНЯ ====================

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            STONE_WAS_EQUIPPED.remove(serverPlayer.getUUID()); // при входе переопределим состояние по факту экипировки
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) return;

        boolean hasStone = hasFireStoneEquipped(serverPlayer);

        if (hasStone) {
            // Очки выдаются только при АКТИВАЦИИ камня (переход "не надет -> надет"),
            // а не автоматически при нулевом значении.
            Boolean prev = STONE_WAS_EQUIPPED.get(serverPlayer.getUUID());
            if (prev == null || !prev) {
                STONE_WAS_EQUIPPED.put(serverPlayer.getUUID(), true);
                grantActivationFlamePoints(serverPlayer);
            }
        } else {
            if (STONE_WAS_EQUIPPED.remove(serverPlayer.getUUID()) != null
                    && Kots3Attachments.getFlamePoints(serverPlayer) > 0) {
                Kots3Attachments.setFlamePoints(serverPlayer, 0);
                syncFlamePoints(serverPlayer);
            }
        }
    }

    private static final Map<java.util.UUID, Boolean> STONE_WAS_EQUIPPED = new ConcurrentHashMap<>();

    /**
     * Выдача 3 очков Пламени при активации (экипировке) огненного камня.
     */
    private void grantActivationFlamePoints(ServerPlayer player) {
        Kots3Attachments.setFlamePoints(player, Math.min(3, Kots3Attachments.getFlamePoints(player) + 3));
        syncFlamePoints(player);

        player.displayClientMessage(Component.translatable("chat.kots.flame_points_granted", 3), true);
    }

    // ==================== УРОН ПО ГОРЯЩЕМУ ВРАГУ / ОГНЕННЫЙ РЕЗОНАНС ====================

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;

        DamageSource source = event.getSource();

        // 1) Фактический взрыв (в т.ч. огненного резонанса Abloom), причинённый игроком: +2 очка
        if ((source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION))
                && source.getEntity() instanceof ServerPlayer attacker) {
            tryGrantResonancePoints(attacker, target);
        }

        // 2) Очко за урон по горящему врагу, не чаще раза в 3 секунды на игрока
        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof ServerPlayer serverPlayer)) return;

        boolean fireWithBurn = isFireDamage(source) && target.hasEffect(AbloomModEffects.BURN);
        if (fireWithBurn) {
            // Это кандидат на огненный резонанс — начисляем резонансные очки (+2),
            // обычное очко за урон при этом не дублируем.
            tryGrantResonancePoints(serverPlayer, target);
            return;
        }

        if (!isTargetBurning(target)) return;

        long currentTime = serverPlayer.level().getGameTime();
        if (!Kots3Attachments.canGainDamagePoint(serverPlayer, currentTime, DAMAGE_POINT_COOLDOWN_TICKS)) return;

        Kots3Attachments.addFlamePoints(serverPlayer, 1);
        Kots3Attachments.setLastDamagePointTime(serverPlayer, currentTime);
        syncFlamePoints(serverPlayer);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        // Очистка записей кулдаунов резонанса по умершей цели
        if (event.getEntity().level().isClientSide) return;
        java.util.UUID dead = event.getEntity().getUUID();
        LAST_RESONANCE_TIME.keySet().removeIf(key -> key.endsWith(":" + dead));
    }

    private static boolean isFireDamage(DamageSource source) {
        String msgId = source.type().msgId();
        if (msgId != null && msgId.contains("fire")) return true;
        return source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR);
    }

    private static boolean isTargetBurning(LivingEntity target) {
        return target.hasEffect(AbloomModEffects.BURN) || target.isOnFire();
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
