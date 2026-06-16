package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ClientBoundCommonConfigSyncPacket(boolean useClientRestriction) implements CustomPacket {
    public static final ResourceLocation TYPE = Constants.id("common_config_sync");

    @Override
    public FriendlyByteBuf encode(FriendlyByteBuf buf) {
        buf.writeBoolean(useClientRestriction);
        return buf;
    }

    public static ClientBoundCommonConfigSyncPacket decode(FriendlyByteBuf buf){
        return new ClientBoundCommonConfigSyncPacket(buf.readBoolean());
    }

    @Override
    public ResourceLocation id() {
        return TYPE;
    }

    @Override
    public void handle(PayloadContext context){
        context.execute(() -> {
            CommonConfig.clientCache = new CommonConfig.ClientCache(useClientRestriction, CommonConfig.restrictedSlots(context.player()));
        });
    }
}
