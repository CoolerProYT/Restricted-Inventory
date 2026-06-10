package com.coolerpromc.restrictedinventory.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashMap;
import java.util.Map;

public class RestrictedSlotsProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final RestrictedSlotsCapability cap = new RestrictedSlotsCapability();

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == ModCapabilities.RESTRICTED_SLOTS) {
            return LazyOptional.of(() -> cap).cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        cap.getRestrictedSlots().forEach((k, v) -> tag.putString(k.toString(), v));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        Map<Integer, String> map = new HashMap<>();
        tag.getAllKeys().forEach(k -> map.put(Integer.parseInt(k), tag.getString(k)));
        cap.setRestrictedSlots(map);
    }
}