package com.coolerpromc.restrictedinventory.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "draylar.inmis.ui.BackpackScreenHandler$BackpackLockedSlot", remap = false)
public abstract class BackpackLockedSlotMixin extends Slot {
    public BackpackLockedSlotMixin(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    // Inmis is an optional, unmapped target. Its Slot override uses the Mojang
    // name in development and the Fabric intermediary name in production.
    @ModifyReturnValue(method = {"mayPlace", "method_7680"}, at = @At("RETURN"), require = 1)
    public boolean mayPlace(boolean original, @Local(argsOnly = true) ItemStack stack){
        if (original){
            return super.mayPlace(stack);
        }
        return false;
    }
}
