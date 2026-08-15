package com.coolerpromc.restrictedinventory.capability;

import com.coolerpromc.restrictedinventory.config.util.Restriction;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface IRestrictedSlots {
    Map<Integer, Restriction> getRestrictedSlots();
    void setRestrictedSlots(Map<Integer, Restriction> slots);
}