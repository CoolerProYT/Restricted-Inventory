package com.coolerpromc.restrictedinventory.capability;

import java.util.HashMap;
import java.util.Map;

public class RestrictedSlotsCapability implements IRestrictedSlots {
    private Map<Integer, String> slots = new HashMap<>();

    @Override
    public Map<Integer, String> getRestrictedSlots() { return slots; }

    @Override
    public void setRestrictedSlots(Map<Integer, String> slots) { this.slots = slots; }
}