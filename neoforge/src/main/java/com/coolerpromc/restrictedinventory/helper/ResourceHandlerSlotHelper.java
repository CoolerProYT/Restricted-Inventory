package com.coolerpromc.restrictedinventory.helper;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.mixin.accessor.VanillaContainerWrapperAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
                    String value = CommonConfig.restrictedSlots(player).get(handler.getContainerSlot());
                    if (value.startsWith("#")){
                        TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(value.substring(1)));
                        return stack.is(tag);
                    }
                    else {
                        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(value));
                        return stack.is(item);
                    }
                }
            }
        }
        return true;
    }

    private static boolean isModifiableSlot(ResourceHandlerSlot slot) {
        return slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 35;
    }
}