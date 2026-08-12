package com.coolerpromc.restrictedinventory.helper;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import com.coolerpromc.restrictedinventory.mixin.accessor.VanillaContainerWrapperAccessor;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

import java.util.Set;

public class ResourceHandlerSlotHelper {
    public static boolean mayPlace(ItemStack stack, ResourceHandlerSlot handler, PlayerInventoryWrapper wrapper){
        if (wrapper instanceof VanillaContainerWrapper vanillaContainerWrapper){
            Container container = ((VanillaContainerWrapperAccessor) vanillaContainerWrapper).getContainer();
            if (container instanceof Inventory inventory){
                Player player = inventory.player;
                Set<Integer> indexes = CommonConfig.restrictedSlots(player).keySet();
                if (indexes.contains(handler.getContainerSlot()) && isModifiableSlot(handler)){
                    ItemEntry value = CommonConfig.restrictedSlots(player).get(handler.getContainerSlot());
                    return value.matches(stack, player.registryAccess());
                }
            }
        }
        return true;
    }

    private static boolean isModifiableSlot(ResourceHandlerSlot slot) {
        return slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 35;
    }
}