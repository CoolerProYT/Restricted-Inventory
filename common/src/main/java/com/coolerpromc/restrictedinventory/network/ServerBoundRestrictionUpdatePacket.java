package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record ServerBoundRestrictionUpdatePacket(Map<String, Either<String, ItemEntry>> newRestriction) implements CustomPacket {
    public static final ResourceLocation TYPE = Constants.id("restriction_update");

    public void handle(PayloadContext context){
        context.execute(() -> {
            CommonConfig.RESTRICTED_SLOTS.set(newRestriction);
            CommonConfig.CONFIG.save();
        });
    }

    @Override
    public FriendlyByteBuf encode(FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ItemEntry.CONFIG_CODEC, newRestriction);
        return buf;
    }

    public static ServerBoundRestrictionUpdatePacket decode(FriendlyByteBuf buf){
        return new ServerBoundRestrictionUpdatePacket(buf.readJsonWithCodec(ItemEntry.CONFIG_CODEC));
    }

    @Override
    public ResourceLocation id() {
        return TYPE;
    }
}
