package com.coolerpromc.restrictedinventory.capability;

import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface IRestrictedSlots {
    Map<Integer, ItemEntry> getRestrictedSlots();
    void setRestrictedSlots(Map<Integer, ItemEntry> slots);
}