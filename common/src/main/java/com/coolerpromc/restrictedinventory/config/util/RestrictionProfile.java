package com.coolerpromc.restrictedinventory.config.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record RestrictionProfile(Map<Integer, Restriction> restrictedSlots) {
    public static final RestrictionProfile EMPTY = new RestrictionProfile(Map.of());

    public static final Codec<Map<Integer, Restriction>> SLOTS_CODEC =
        Codec.unboundedMap(RestrictionCodecs.SLOT_INDEX.xmap(Integer::parseInt, Object::toString), Restriction.CODEC);

    public static final Codec<RestrictionProfile> CODEC = RecordCodecBuilder.create(i -> i.group(
        SLOTS_CODEC.fieldOf("restrictedSlots").forGetter(RestrictionProfile::restrictedSlots)
    ).apply(i, RestrictionProfile::new));

    public static final Codec<Map<String, RestrictionProfile>> CONFIG_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);
}
