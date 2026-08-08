package com.coolerpromc.restrictedinventory.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "draylar.inmis.ui.BackpackScreenHandler$BackpackLockedSlot", remap = false)
public abstract class BackpackLockedSlotMixin extends Slot {
    public BackpackLockedSlotMixin(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"), require = 0)
    public boolean mayPlace(boolean original, @Local(argsOnly = true) ItemStack stack){
        if (original){
            return super.mayPlace(stack);
        }
        return false;
    }
}
