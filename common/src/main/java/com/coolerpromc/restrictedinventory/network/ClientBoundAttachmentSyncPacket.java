package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.util.Restriction;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record ClientBoundAttachmentSyncPacket(Map<Integer, Restriction> restrictedSlots) implements CustomPacket {
    public static final ResourceLocation TYPE = Constants.id("attachment_sync");

    @Override
    public FriendlyByteBuf encode(FriendlyByteBuf buf) {
        buf.writeMap(restrictedSlots, FriendlyByteBuf::writeInt, (buf1, restriction) -> buf1.writeJsonWithCodec(Restriction.CODEC, restriction));
        return buf;
    }

    public static ClientBoundAttachmentSyncPacket decode(FriendlyByteBuf buf){
        return new ClientBoundAttachmentSyncPacket(buf.readMap(FriendlyByteBuf::readInt, buf1 -> buf1.readJsonWithCodec(Restriction.CODEC)));
    }

    @Override
    public ResourceLocation id() {
        return TYPE;
    }

    @Override
    public void handle(PayloadContext context){
        context.execute(() -> {
            Services.PLATFORM.setRestrictedSlots(context.player(), restrictedSlots);
        });
    }
}
