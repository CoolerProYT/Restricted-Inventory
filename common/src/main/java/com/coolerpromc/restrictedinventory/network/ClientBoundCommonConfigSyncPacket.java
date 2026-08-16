package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.RestrictionGroup;
import com.coolerpromc.restrictedinventory.config.util.RestrictionGroups;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record ClientBoundCommonConfigSyncPacket(boolean useClientRestriction, Map<ResourceLocation, RestrictionGroup> groups) implements CustomPacket {
    public static final ResourceLocation TYPE = Constants.id("common_config_sync");

    private static final Codec<Map<ResourceLocation, RestrictionGroup>> GROUPS_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, RestrictionGroup.CODEC);

    @Override
    public FriendlyByteBuf encode(FriendlyByteBuf buf) {
        buf.writeBoolean(useClientRestriction);
        buf.writeJsonWithCodec(GROUPS_CODEC, groups);
        return buf;
    }

    public static ClientBoundCommonConfigSyncPacket decode(FriendlyByteBuf buf){
        return new ClientBoundCommonConfigSyncPacket(buf.readBoolean(), buf.readJsonWithCodec(GROUPS_CODEC));
    }

    @Override
    public ResourceLocation id() {
        return TYPE;
    }

    @Override
    public void handle(PayloadContext context){
        context.execute(() -> {
            RestrictionGroups.setRemote(groups);
            CommonConfig.clientCache = new CommonConfig.ClientCache(useClientRestriction, Services.PLATFORM.getRestrictedSlots(context.player()));
        });
    }
}
