package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.ClientConfig;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientBoundNotifyUpdatePacket() implements CustomPacketPayload {
    public static final Type<ClientBoundNotifyUpdatePacket> TYPE = new Type<>(Constants.id("notify_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundNotifyUpdatePacket> STREAM_CODEC = StreamCodec.unit(new ClientBoundNotifyUpdatePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            Services.NETWORK.sendToServer(new ServerBoundClientRestrictedSlotsPacket(ClientConfig.getRestrictedSlots()));
        });
    }
}
