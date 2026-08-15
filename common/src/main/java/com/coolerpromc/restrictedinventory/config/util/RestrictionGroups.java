package com.coolerpromc.restrictedinventory.config.util;

import com.coolerpromc.restrictedinventory.Constants;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class RestrictionGroups {
    private static volatile Map<ResourceLocation, RestrictionGroup> local = Map.of();
    private static volatile @Nullable Map<ResourceLocation, RestrictionGroup> remote = null;

    private RestrictionGroups() {
    }

    public static void setLocal(Map<ResourceLocation, RestrictionGroup> groups) {
        local = Map.copyOf(groups);
    }

    public static void setRemote(Map<ResourceLocation, RestrictionGroup> groups) {
        remote = Map.copyOf(groups);
    }

    public static Map<ResourceLocation, RestrictionGroup> all() {
        Map<ResourceLocation, RestrictionGroup> synced = remote;
        return synced != null ? synced : local;
    }

    public static Optional<RestrictionGroup> get(ResourceLocation id) {
        return Optional.ofNullable(all().get(id));
    }

    public static Map<ResourceLocation, RestrictionGroup> resolve(Map<String, RestrictionGroup> raw) {
        Map<ResourceLocation, RestrictionGroup> groups = new LinkedHashMap<>();

        raw.forEach((key, group) -> RestrictionCodecs.parseGroupId(key)
            .resultOrPartial(error -> Constants.LOGGER.warn("Ignoring restriction group '{}': {}", key, error))
            .ifPresent(id -> {
                if (groups.putIfAbsent(id, group) != null) {
                    Constants.LOGGER.warn("Duplicate restriction group id {} (from key '{}'); keeping the first definition", id, key);
                }
            }));

        return groups;
    }

    public static void validate(Collection<Restriction> restrictions) {
        for (Restriction restriction : restrictions) {
            if (restriction instanceof GroupEntry entry && get(entry.group()).isEmpty()) {
                Constants.LOGGER.warn("Restriction references unknown group {}; that slot will not accept any item. Known groups: {}", entry.group(), all().keySet());
            }
        }
    }
}
