package com.coolerpromc.restrictedinventory.platform;

import com.coolerpromc.restrictedinventory.platform.services.ISlotHelper;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FabricSlotHelper implements ISlotHelper {
    @Override
    public boolean mayPlace(Slot slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean isModifiableSlot(Slot slot) {
        return false;
    }
}
