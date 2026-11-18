package com.auranite.keepersofthestonesthree.mixin;

import com.auranite.keepersofthestonesthree.item.Kots3Items;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Shadow @Final public Container container;

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void kots3$denyFireSword(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!(this.container instanceof Inventory) && stack.is(Kots3Items.FIRE_SWORD.get())) {
            cir.setReturnValue(false);
        }
    }
}