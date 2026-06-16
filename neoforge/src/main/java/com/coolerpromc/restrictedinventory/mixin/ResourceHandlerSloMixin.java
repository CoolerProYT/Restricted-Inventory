package com.coolerpromc.restrictedinventory.mixin;

import com.coolerpromc.restrictedinventory.helper.ResourceHandlerSlotHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.world.inventory.StackCopySlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResourceHandlerSlot.class)
public abstract class ResourceHandlerSloMixin extends StackCopySlot {
    @Shadow
    @Final
    private ResourceHandler<ItemResource> handler;

    public ResourceHandlerSloMixin(int slot, int x, int y) {
        super(slot, x, y);
    }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    public void mayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        if (this.handler instanceof PlayerInventoryWrapper wrapper){
            cir.setReturnValue(ResourceHandlerSlotHelper.mayPlace(stack, (ResourceHandlerSlot)(Object)this, wrapper));
        }
    }
}