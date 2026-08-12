package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record ServerBoundClientRestrictedSlotsPacket(Map<Integer, ItemEntry> restrictedSlots) implements CustomPacket{
    public static final ResourceLocation TYPE = Constants.id("restricted_slots");

    @Override
    public void handle(PayloadContext context){
        context.execute(() -> {
            if (CommonConfig.useClientRestriction()){
                Services.PLATFORM.setRestrictedSlots(context.player(), restrictedSlots);
            }
        });
    }

    @Override
    public FriendlyByteBuf encode(FriendlyByteBuf buf) {
        buf.writeMap(restrictedSlots, FriendlyByteBuf::writeInt, (buf1, itemEntry) -> buf1.writeJsonWithCodec(ItemEntry.CODEC, itemEntry));
        return buf;
    }

    public static ServerBoundClientRestrictedSlotsPacket decode(FriendlyByteBuf buf){
        return new ServerBoundClientRestrictedSlotsPacket(buf.readMap(FriendlyByteBuf::readInt, buf1 -> buf1.readJsonWithCodec(ItemEntry.CODEC)));
    }

    @Override
    public ResourceLocation id() {
        return TYPE;
    }
}
