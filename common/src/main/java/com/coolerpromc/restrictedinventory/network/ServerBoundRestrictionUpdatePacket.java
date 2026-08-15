package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.Restriction;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record ServerBoundRestrictionUpdatePacket(Map<String, Restriction> newRestriction) implements CustomPacket {
    public static final ResourceLocation TYPE = Constants.id("restriction_update");

    public void handle(PayloadContext context){
        context.execute(() -> {
            CommonConfig.RESTRICTED_SLOTS.set(newRestriction);
            CommonConfig.CONFIG.save();
        });
    }

    @Override
    public FriendlyByteBuf encode(FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(Restriction.CONFIG_CODEC, newRestriction);
        return buf;
    }

    public static ServerBoundRestrictionUpdatePacket decode(FriendlyByteBuf buf){
        return new ServerBoundRestrictionUpdatePacket(buf.readJsonWithCodec(Restriction.CONFIG_CODEC));
    }

    @Override
    public ResourceLocation id() {
        return TYPE;
    }
}
