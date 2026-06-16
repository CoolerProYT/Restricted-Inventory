package com.coolerpromc.restrictedinventory.platform;

import com.coolerpromc.restrictedinventory.platform.services.ISlotHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class NeoForgeSlotHelper implements ISlotHelper {
    @Override
    public boolean mayPlace(Slot slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean isModifiableSlot(Slot slot) {
        if (slot instanceof SlotItemHandler itemHandler){
            if (itemHandler.getItemHandler() instanceof InvWrapper wrapper){
                return (wrapper.getInv() instanceof Inventory && !(wrapper.getInv() instanceof ArmorSlot) && slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 35);
            }
        }
        if (slot instanceof ResourceHandlerSlot handlerSlot){
            if (handlerSlot.getResourceHandler() instanceof PlayerInventoryWrapper){
                return slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 35;
            }
        }
        return false;
    }
}