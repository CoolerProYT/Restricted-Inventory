package com.coolerpromc.restrictedinventory.mixin.compat;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.hrznstudio.titanium.container.impl.DisableableSlot")
public abstract class DisableableSlotMixin extends Slot {
    public DisableableSlotMixin(Container container, int slot, int x, int y) {
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
