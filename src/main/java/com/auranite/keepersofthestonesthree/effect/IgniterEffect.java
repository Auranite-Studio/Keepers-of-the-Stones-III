package com.auranite.keepersofthestonesthree.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class IgniterEffect extends MobEffect {
    public IgniterEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
        this.addAttributeModifier(AbloomModAttributes.FIRE_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.igniter_1"), 0.05, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.FIRE_DMG_BONUS, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.igniter_2"), 0.1, AttributeModifier.Operation.ADD_VALUE);

    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
