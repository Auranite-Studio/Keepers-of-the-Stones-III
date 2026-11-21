package com.auranite.keepersofthestonesthree.init;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class Kots3Attachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, "kots");

    public static final Supplier<AttachmentType<CompoundTag>> FLAME_POINTS =
            ATTACHMENT_TYPES.register("flame_points",
                    () -> AttachmentType.<CompoundTag>builder(() -> new CompoundTag()).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }

    public static CompoundTag getFlamePointsData(net.minecraft.world.entity.Entity entity) {
        return entity.getData(FLAME_POINTS.get());
    }

    // ==================== Helper methods для работы с данными ====================

    public static int getFlamePoints(net.minecraft.world.entity.Entity entity) {
        CompoundTag data = getFlamePointsData(entity);
        return data.getInt("FlamePoints");
    }

    public static void setFlamePoints(net.minecraft.world.entity.Entity entity, int points) {
        CompoundTag data = getFlamePointsData(entity);
        data.putInt("FlamePoints", Math.max(0, Math.min(3, points)));
    }

    public static void addFlamePoints(net.minecraft.world.entity.Entity entity, int amount) {
        CompoundTag data = getFlamePointsData(entity);
        int current = data.getInt("FlamePoints");
        data.putInt("FlamePoints", Math.max(0, Math.min(3, current + amount)));
    }

    public static long getLastDamagePointTime(net.minecraft.world.entity.Entity entity) {
        CompoundTag data = getFlamePointsData(entity);
        return data.getLong("LastDamagePointTime");
    }

    public static void setLastDamagePointTime(net.minecraft.world.entity.Entity entity, long time) {
        CompoundTag data = getFlamePointsData(entity);
        data.putLong("LastDamagePointTime", time);
    }

    public static boolean canGainDamagePoint(net.minecraft.world.entity.Entity entity, long currentTime, long cooldownTicks) {
        return (currentTime - getLastDamagePointTime(entity)) >= cooldownTicks;
    }
}
