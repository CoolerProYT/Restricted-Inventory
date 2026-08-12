package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record ServerBoundRestrictionUpdatePacket(Map<String, ItemEntry> newRestriction) implements CustomPacketPayload {
    public static final Type<ServerBoundRestrictionUpdatePacket> TYPE = new Type<>(Constants.id("restriction_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundRestrictionUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ItemEntry.STREAM_CODEC),
            ServerBoundRestrictionUpdatePacket::newRestriction,
            ServerBoundRestrictionUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            // the config holds the compact form, a bare id for entries with no component filter
            CommonConfig.RESTRICTED_SLOTS.set(newRestriction.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().compact())));
            CommonConfig.CONFIG.save();
        });
    }
}
