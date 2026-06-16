package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.Map;

public record ServerBoundRestrictionUpdatePacket(Map<String, String> newRestriction) implements CustomPacketPayload {
    public static final Type<ServerBoundRestrictionUpdatePacket> TYPE = new Type<>(Constants.id("restriction_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundRestrictionUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
            ServerBoundRestrictionUpdatePacket::newRestriction,
            ServerBoundRestrictionUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            CommonConfig.RESTRICTED_SLOTS.set(newRestriction);
            CommonConfig.CONFIG.save();
        });
    }
}
