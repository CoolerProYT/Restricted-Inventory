package com.coolerpromc.restrictedinventory.capability;

import com.coolerpromc.restrictedinventory.config.util.ItemEntry;

import java.util.HashMap;
import java.util.Map;

public class RestrictedSlotsCapability implements IRestrictedSlots {
    private Map<Integer, ItemEntry> slots = new HashMap<>();

    @Override
    public Map<Integer, ItemEntry> getRestrictedSlots() { return slots; }

    @Override
    public void setRestrictedSlots(Map<Integer, ItemEntry> slots) { this.slots = slots; }
}