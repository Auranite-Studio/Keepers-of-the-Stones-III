package com.auranite.keepersofthestonesthree.ability;

import com.auranite.abloom.handler.ElementDamageHandler;
import com.auranite.abloom.util.ElementType;
import com.auranite.keepersofthestonesthree.KeepersOfTheStonesIIIMod;
import com.auranite.keepersofthestonesthree.effect.Kots3Effects;
import com.auranite.keepersofthestonesthree.event.FlameSkillHandler;
import com.auranite.keepersofthestonesthree.init.Kots3Attachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

/**
 * Способность "Язык Пламени" — огненный луч.
 *
 * При использовании (ПКМ) при наличии 3 очков Пламени:
 * - Списывает 3 очка
 * - Применяет эффект Поджигатель (+10%/+20% огненный урон, +5%/+10% сопротивление)
 * - Выпускает огненный луч на 3 секунды, нанося суммарно 480% урона (160% в секунду)
 */
public class FlameTongueAbility {

    private static final int FLAME_MAX_POINTS = 3;
    private static final int FLAME_TONGUE_DURATION_TICKS = 60; // 3 секунды
    private static final float TOTAL_DAMAGE_MULTIPLIER = 4.8f; // суммарно 480% от базового урона оружия
    private static final int FLAME_RANGE = 12; // дальность луча
    private static final int FLAME_WIDTH = 2; // ширина луча
    private static final int BEAM_UPDATE_INTERVAL_TICKS = 5; // периодическое обновление луча (6 обновлений за 3 сек)

    /**
     * Активные лучи: id игрока -> время окончания действия (gameTime).
     */
    private static final java.util.Map<Integer, Long> ACTIVE_BEAMS = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Активация способности через ПКМ по блоку.
     */
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // Способность не блокирует обычное взаимодействие с блоками — событие не отменяется.
        tryActivateFlameTongue(serverPlayer);
    }

    /**
     * Активация способности через ПКМ в пустоту.
     */
    @SubscribeEvent
    public void onRightClickAir(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        tryActivateFlameTongue(serverPlayer);
    }

    /**
     * Основная логика активации способности.
     */
    private void tryActivateFlameTongue(ServerPlayer player) {
        // Проверка: способность уже активна? (повторная активация возможна только после окончания луча)
        long gameTime = player.level().getGameTime();
        Long beamEnd = ACTIVE_BEAMS.get(player.getId());
        if (beamEnd != null && gameTime < beamEnd) {
            return;
        }

        // Проверка: достаточно ли очков Пламени
        int currentPoints = Kots3Attachments.getFlamePoints(player);
        if (currentPoints < FLAME_MAX_POINTS) {
            return;
        }

        // Списываем ВСЕ очки (максимум 3)
        Kots3Attachments.setFlamePoints(player, 0);
        syncFlamePoints(player);

        // Применяем эффект Поджигателя
        int igniterLevel = FlameSkillHandler.getIgniterLevel(player);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                Kots3Effects.IGNITER, 200, igniterLevel - 1, false, true));

        // Показываем сообщение
        player.displayClientMessage(Component.translatable("ability.kots.flame_tongue_activated"), true);

        // Запускаем огненный луч
        startFlameTongue(player, gameTime);
    }

    /**
     * Запускает огненный луч на 3 секунды.
     * Наносит суммарно 480% урона от базового значения оружия (160% в секунду).
     */
    private void startFlameTongue(ServerPlayer player, long startTime) {
        ACTIVE_BEAMS.put(player.getId(), startTime + FLAME_TONGUE_DURATION_TICKS);

        // Первый залп сразу, далее — централизованно в onPlayerTick
        applyBeamDamage(player);
        spawnFlameBeamParticles(player);
    }

    /**
     * Централизованный тикер активных лучей: луч движется вместе с игроком,
     * периодическое нанесение урона и спавн частиц.
     */
    @SubscribeEvent
    public void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.level().isClientSide || !(p instanceof ServerPlayer player)) return;

        long gameTime = player.level().getGameTime();
        Long beamEnd = ACTIVE_BEAMS.get(player.getId());
        if (beamEnd == null || gameTime >= beamEnd) return;
        if (!player.isAlive() || player.isDeadOrDying()) {
            ACTIVE_BEAMS.remove(player.getId());
            return;
        }

        int elapsed = (int) (gameTime - (beamEnd - FLAME_TONGUE_DURATION_TICKS));
        if (elapsed % BEAM_UPDATE_INTERVAL_TICKS != 0) return;

        applyBeamDamage(player);
        spawnFlameBeamParticles(player);
    }

    /**
     * Наносит урон лучом по всем целям перед игроком.
     */
    private void applyBeamDamage(ServerPlayer player) {
        float baseWeaponDamage = getBaseWeaponDamage(player);
        float damagePerUpdate = (baseWeaponDamage * TOTAL_DAMAGE_MULTIPLIER)
                / ((float) FLAME_TONGUE_DURATION_TICKS / BEAM_UPDATE_INTERVAL_TICKS);

        for (LivingEntity target : findTargetsInBeam(player)) {
            if (target.isAlive() && !target.isSpectator()) {
                ElementDamageHandler.dealElementDamage(
                        target,
                        ElementType.FIRE,
                        damagePerUpdate,
                        1.0f,
                        player,
                        false);

                // Если на цели есть поджог, Abloom вызывает взрыв огненного резонанса —
                // начисляем 2 очка Пламени (не чаще раза в 3 секунды на пару игрок-цель).
                FlameSkillHandler.onFireResonanceExplosion(player, target);
            }
        }
    }

    /**
     * Находит цели в луче перед игроком (raycast).
     */
    private List<LivingEntity> findTargetsInBeam(ServerPlayer player) {
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 direction = player.getViewVector(1.0f).normalize();
        Vec3 end = start.add(direction.scale(FLAME_RANGE));

        // Raycast — проверяем каждый блок на пути луча
        List<LivingEntity> targets = new java.util.ArrayList<>();
        Vec3 step = direction.scale(0.5); // шаг 0.5 блока
        Vec3 current = start;

        while (current.distanceTo(end) > 0.1) {
            // Проверяем сущностей в зоне луча
            AABB beamBox = new AABB(
                    current.x - FLAME_WIDTH, current.y - 0.5, current.z - FLAME_WIDTH,
                    current.x + FLAME_WIDTH, current.y + 0.5, current.z + FLAME_WIDTH
            );

            List<Entity> nearby = player.level().getEntities((Entity) null, beamBox,
                    entity -> entity instanceof LivingEntity living && living != player);

            for (Entity entity : nearby) {
                if (entity instanceof LivingEntity target && target.isAlive() && !target.isSpectator()) {
                    // Проверяем, не добавили ли уже
                    boolean alreadyAdded = false;
                    for (LivingEntity existing : targets) {
                        if (existing.getId() == target.getId()) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded) {
                        targets.add(target);
                    }
                }
            }

            current = current.add(step);
        }

        return targets;
    }

    /**
     * Спавнит частицы огненного луча.
     */
    private void spawnFlameBeamParticles(ServerPlayer player) {
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 direction = player.getViewVector(1.0f).normalize();
        Vec3 end = start.add(direction.scale(FLAME_RANGE));

        // Спавним частицы вдоль луча
        Vec3 step = direction.scale(0.5);
        Vec3 current = start;

        for (int p = 0; p < 10; p++) {
            double offsetX = (player.level().random.nextDouble() - 0.5) * FLAME_WIDTH;
            double offsetY = (player.level().random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (player.level().random.nextDouble() - 0.5) * FLAME_WIDTH;

            player.level().addParticle(
                    ParticleTypes.FLAME,
                    current.x + offsetX,
                    current.y + offsetY,
                    current.z + offsetZ,
                    direction.x * 0.5,
                    0.1,
                    direction.z * 0.5
            );

            current = current.add(step);
        }
    }

    /**
     * Получает базовый урон оружия игрока.
     */
    private float getBaseWeaponDamage(Player player) {
        double attackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return (float) Math.max(1.0f, attackDamage);
    }

    /**
     * Синхронизирует очки Пламени с клиентом.
     */
    private void syncFlamePoints(ServerPlayer player) {
        byte points = (byte) Kots3Attachments.getFlamePoints(player);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new com.auranite.keepersofthestonesthree.network.FlamePointsSyncPacket(points));
    }
}
