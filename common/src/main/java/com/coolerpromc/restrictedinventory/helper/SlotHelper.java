package com.coolerpromc.restrictedinventory.helper;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class SlotHelper {
    public static boolean mayPlace(Slot slot, ItemStack stack){
        if (slot.container instanceof Inventory inventory){
            Player player = inventory.player;
            Set<Integer> indexes = CommonConfig.restrictedSlots(player).keySet();
            if (indexes.contains(slot.getContainerSlot()) && isModifiableSlot(slot)){
                String value = CommonConfig.restrictedSlots(player).get(slot.getContainerSlot());
                if (value.startsWith("#")){
                    TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(value.substring(1)));
                    return stack.is(tag);
                }
                else {
                    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(value));
                    return stack.is(item);
                }
            }
        }
        return true;
    }

    private static boolean isModifiableSlot(Slot slot) {
        return slot.container instanceof Inventory && !(slot instanceof ArmorSlot) && slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 35;
    }
}
