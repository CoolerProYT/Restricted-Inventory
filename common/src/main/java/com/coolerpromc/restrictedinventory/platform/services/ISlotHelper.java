package com.coolerpromc.restrictedinventory.platform.services;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface ISlotHelper {
    boolean mayPlace(Slot slot, ItemStack stack);
    boolean isModifiableSlot(Slot slot);
}
