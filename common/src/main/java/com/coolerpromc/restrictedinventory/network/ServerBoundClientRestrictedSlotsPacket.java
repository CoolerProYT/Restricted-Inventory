package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.Restriction;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.Map;

public record ServerBoundClientRestrictedSlotsPacket(Map<Integer, Restriction> restrictedSlots) implements CustomPacketPayload {
    public static final Type<ServerBoundClientRestrictedSlotsPacket> TYPE = new Type<>(Constants.id("restricted_slots"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundClientRestrictedSlotsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, Restriction.STREAM_CODEC),
            ServerBoundClientRestrictedSlotsPacket::restrictedSlots,
            ServerBoundClientRestrictedSlotsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            if (CommonConfig.useClientRestriction()){
                Services.PLATFORM.setRestrictedSlots(context.player(), restrictedSlots);
            }
        });
    }
}
