package com.auranite.keepersofthestonesthree.keepersofthestonesiii.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FireMasterEffect extends MobEffect {
    public FireMasterEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
//        this.addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.break_0"), -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
