package com.coolerpromc.restrictedinventory.capability;

import java.util.Map;

public interface IRestrictedSlots {
    Map<Integer, String> getRestrictedSlots();
    void setRestrictedSlots(Map<Integer, String> slots);
}