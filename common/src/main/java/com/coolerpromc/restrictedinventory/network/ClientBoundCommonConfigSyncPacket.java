package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.RestrictionGroup;
import com.coolerpromc.restrictedinventory.config.util.RestrictionGroups;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record ClientBoundCommonConfigSyncPacket(boolean useClientRestriction, Map<ResourceLocation, RestrictionGroup> groups) implements CustomPacketPayload {
    public static final Type<ClientBoundCommonConfigSyncPacket> TYPE = new Type<>(Constants.id("common_config_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundCommonConfigSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ClientBoundCommonConfigSyncPacket::useClientRestriction,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, RestrictionGroup.STREAM_CODEC),
            ClientBoundCommonConfigSyncPacket::groups,
            ClientBoundCommonConfigSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            RestrictionGroups.setRemote(groups);
            CommonConfig.clientCache = new CommonConfig.ClientCache(useClientRestriction, CommonConfig.restrictedSlots(context.player()));
        });
    }
}
