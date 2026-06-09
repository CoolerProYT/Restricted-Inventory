package com.coolerpromc.restrictedinventory.mixin.accessor;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Inventory.class)
public interface InventoryAccessor {
    @Invoker("addResource")
    int callAddResource(int slot, ItemStack itemStack);
}