package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface CustomPacket {
    FriendlyByteBuf encode(FriendlyByteBuf buf);
    ResourceLocation id();
    void handle(PayloadContext context);
}
