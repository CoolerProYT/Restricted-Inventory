package com.coolerpromc.restrictedinventory.capability;

import com.coolerpromc.restrictedinventory.config.util.Restriction;

import java.util.HashMap;
import java.util.Map;

public class RestrictedSlotsCapability implements IRestrictedSlots {
    private Map<Integer, Restriction> slots = new HashMap<>();

    @Override
    public Map<Integer, Restriction> getRestrictedSlots() { return slots; }

    @Override
    public void setRestrictedSlots(Map<Integer, Restriction> slots) { this.slots = slots; }
}