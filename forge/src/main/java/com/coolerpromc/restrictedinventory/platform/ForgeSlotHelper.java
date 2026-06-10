package com.coolerpromc.restrictedinventory.platform;

import com.coolerpromc.restrictedinventory.platform.services.ISlotHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ForgeSlotHelper implements ISlotHelper {
    @Override
    public boolean mayPlace(Slot slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean isModifiableSlot(Slot slot) {
        if (slot instanceof SlotItemHandler itemHandler){
            if (itemHandler.getItemHandler() instanceof InvWrapper wrapper){
                return (wrapper.getInv() instanceof Inventory && slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 35);
            }
        }
        return false;
    }
}
