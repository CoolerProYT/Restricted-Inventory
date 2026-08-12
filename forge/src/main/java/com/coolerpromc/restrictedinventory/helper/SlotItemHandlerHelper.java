package com.coolerpromc.restrictedinventory.helper;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.Set;

public class SlotItemHandlerHelper {
    public static boolean mayPlace(ItemStack stack, SlotItemHandler handler, InvWrapper wrapper){
        if (wrapper.getInv() instanceof Inventory inventory){
            Player player = inventory.player;
            Set<Integer> indexes = CommonConfig.restrictedSlots(player).keySet();
            if (indexes.contains(handler.getContainerSlot()) && isModifiableSlot(handler)){
                ItemEntry value = CommonConfig.restrictedSlots(player).get(handler.getContainerSlot());
                return value.matches(stack);
            }
        }
        return true;
    }

    private static boolean isModifiableSlot(SlotItemHandler slot) {
        return slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 35;
    }
}
