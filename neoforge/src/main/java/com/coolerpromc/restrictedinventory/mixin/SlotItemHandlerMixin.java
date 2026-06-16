package com.coolerpromc.restrictedinventory.mixin;

import com.coolerpromc.restrictedinventory.helper.SlotItemHandlerHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlotItemHandler.class)
public abstract class SlotItemHandlerMixin extends Slot {
    @Shadow
    public abstract IItemHandler getItemHandler();

    public SlotItemHandlerMixin(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    public void mayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        if (this.getItemHandler() instanceof InvWrapper wrapper){
            cir.setReturnValue(SlotItemHandlerHelper.mayPlace(stack, (SlotItemHandler)(Object)this, wrapper));
        }
    }
}