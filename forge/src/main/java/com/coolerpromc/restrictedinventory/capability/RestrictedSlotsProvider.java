package com.coolerpromc.restrictedinventory.capability;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
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
        cap.getRestrictedSlots().forEach((k, v) -> tag.put(k.toString(), v.serializeNbt()));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        Map<Integer, ItemEntry> map = new HashMap<>();

        // anything unreadable here is skipped rather than thrown: this runs inside player data
        // loading, where an exception costs the player their login
        for (String key : tag.getAllKeys()) {
            int slot;
            try {
                slot = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                Constants.LOGGER.warn("Ignoring restricted slot with non numeric index {}", key);
                continue;
            }

            ItemEntry.read(tag.get(key)).ifPresent(entry -> map.put(slot, entry));
        }

        cap.setRestrictedSlots(map);
    }
}