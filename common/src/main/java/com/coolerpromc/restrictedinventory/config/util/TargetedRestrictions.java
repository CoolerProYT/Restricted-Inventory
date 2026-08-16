package com.coolerpromc.restrictedinventory.config.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public record TargetedRestrictions(boolean matched, Map<Integer, Restriction> restrictions) {
    public static final TargetedRestrictions NONE = new TargetedRestrictions(false, Map.of());

    public static final StreamCodec<ByteBuf, TargetedRestrictions> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        TargetedRestrictions::matched,
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, Restriction.STREAM_CODEC),
        TargetedRestrictions::restrictions,
        TargetedRestrictions::new
    );
}
