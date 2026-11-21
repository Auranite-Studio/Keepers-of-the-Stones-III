package com.auranite.keepersofthestonesthree.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Клиентское хранилище синхронизированных очков Пламени.
 * Заполняется через сетевой пакет FlamePointsSyncPacket.
 */
@OnlyIn(Dist.CLIENT)
public class ClientFlamePointsStorage {

    private static int syncedFlamePoints = 0;

    /**
     * Получает синхронизированные очки Пламени.
     */
    @OnlyIn(Dist.CLIENT)
    public static int getFlamePoints() {
        return syncedFlamePoints;
    }

    /**
     * Устанавливает синхронизированные очки Пламени (вызывается при получении пакета).
     */
    @OnlyIn(Dist.CLIENT)
    public static void setFlamePoints(int points) {
        syncedFlamePoints = Math.max(0, Math.min(3, points));
    }

    /**
     * Сбрасывает очки Пламени (вызывается при смене уровня/отключении камня).
     */
    @OnlyIn(Dist.CLIENT)
    public static void reset() {
        syncedFlamePoints = 0;
    }
}
