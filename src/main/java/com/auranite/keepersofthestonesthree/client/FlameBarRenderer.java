package com.auranite.keepersofthestonesthree.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Рендерер прогресс-бара очков Пламени в хотбаре.
 * Отображает 3 сегмента слева от хотбара.
 */
@OnlyIn(Dist.CLIENT)
public class FlameBarRenderer {

    private static final int FLAME_ICON_SIZE = 9;
    private static final int FLAME_ICON_SPACING = 11;
    private static final int FLAME_COUNT = 3;

    /**
     * Отрисовка прогресс-бара очков Пламени.
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRenderFlameBar(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int flamePoints = ClientFlamePointsStorage.getFlamePoints();

        // Если нет очков — ничего не рисуем
        if (flamePoints <= 0) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Позиция хотбара (центрирован внизу экрана)
        int hotbarWidth = 182;
        int hotbarHeight = 22;
        int hotbarX = (screenWidth - hotbarWidth) / 2;
        int hotbarY = screenHeight - hotbarHeight;

        // Позиция для иконок пламени — слева от хотбара
        int baseX = hotbarX - (FLAME_COUNT * FLAME_ICON_SPACING) - 4;
        int baseY = hotbarY + 3;

        // Сохраняем текущий матричный стек
        guiGraphics.pose().pushPose();

        // Рисуем иконки пламени (заполненные/пустые сегменты)
        for (int i = 0; i < FLAME_COUNT; i++) {
            int x = baseX + (i * FLAME_ICON_SPACING);
            int y = baseY;

            if (i < flamePoints) {
                // Заполненный сегмент — оранжевый (#FF5500)
                RenderSystem.setShaderColor(1.0f, 0.333f, 0.0f, 1.0f);
                guiGraphics.fill(x, y, x + FLAME_ICON_SIZE, y + FLAME_ICON_SIZE, 0xFFFF5500);
            } else {
                // Пустой сегмент — полупрозрачный оранжевый
                RenderSystem.setShaderColor(0.6f, 0.2f, 0.0f, 0.5f);
                guiGraphics.fill(x, y, x + FLAME_ICON_SIZE, y + FLAME_ICON_SIZE, 0x80331100);
            }
        }

        // Сбрасываем цвет
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Восстанавливаем матричный стек
        guiGraphics.pose().popPose();
    }
}
