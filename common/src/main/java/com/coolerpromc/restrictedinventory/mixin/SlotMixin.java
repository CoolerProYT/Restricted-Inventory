package com.coolerpromc.restrictedinventory.mixin;

import com.coolerpromc.restrictedinventory.helper.SlotHelper;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true)
    public void mayPlace(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(SlotHelper.mayPlace((Slot)(Object)this, itemStack));
    }
}
