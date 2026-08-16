package com.coolerpromc.restrictedinventory.capability;

import com.coolerpromc.restrictedinventory.config.util.Restriction;
import com.coolerpromc.restrictedinventory.config.util.TargetedRestrictions;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface IRestrictedSlots {
    Map<Integer, Restriction> getRestrictedSlots();
    void setRestrictedSlots(Map<Integer, Restriction> slots);

    // Resolved from live team/tag state every tick, so it rides along here without being written to
    // the provider's NBT and without being carried across a respawn.
    TargetedRestrictions getTargetedRestrictions();
    void setTargetedRestrictions(TargetedRestrictions targetedRestrictions);
}