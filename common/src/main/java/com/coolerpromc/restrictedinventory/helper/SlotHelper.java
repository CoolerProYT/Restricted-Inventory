package com.coolerpromc.restrictedinventory.helper;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class SlotHelper {
    public static boolean mayPlace(Slot slot, ItemStack stack){
        if (slot.container instanceof Inventory inventory){
            Player player = inventory.player;
            Set<Integer> indexes = CommonConfig.restrictedSlots(player).keySet();
            if (indexes.contains(slot.getContainerSlot()) && isModifiableSlot(slot)){
                ItemEntry value = CommonConfig.restrictedSlots(player).get(slot.getContainerSlot());
                return value.matches(stack, player.registryAccess());
            }
        }
        return true;
    }

    private static boolean isModifiableSlot(Slot slot) {
        return slot.container instanceof Inventory && !(slot instanceof ArmorSlot) && slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 35;
    }
}
