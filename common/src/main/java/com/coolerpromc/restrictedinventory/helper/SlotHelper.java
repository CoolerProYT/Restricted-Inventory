package com.coolerpromc.restrictedinventory.helper;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.Restriction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class SlotHelper {
    public static boolean mayPlace(Slot slot, ItemStack stack){
        if (slot.container instanceof Inventory inventory){
            Player player = inventory.player;
            Set<Integer> indexes = CommonConfig.restrictedSlots(player).keySet();
            if (indexes.contains(slot.getContainerSlot()) && isModifiableSlot(slot)){
                Restriction value = CommonConfig.restrictedSlots(player).get(slot.getContainerSlot());
                return value.matches(stack);
            }
        }
        return true;
    }

    private static boolean isModifiableSlot(Slot slot) {
        return slot.container instanceof Inventory && slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 35;
    }
}
