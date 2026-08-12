package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.NeoForgeRestrictedInventory;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.Map;

public record ClientBoundAttachmentSyncPacket(Map<Integer, ItemEntry> restrictedSlots) implements CustomPacketPayload {
    public static final Type<ClientBoundAttachmentSyncPacket> TYPE = new Type<>(Constants.id("attachment_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundAttachmentSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, ItemEntry.STREAM_CODEC),
            ClientBoundAttachmentSyncPacket::restrictedSlots,
            ClientBoundAttachmentSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            context.player().setData(NeoForgeRestrictedInventory.RESTRICTED_SLOTS_ATTACHMENT, restrictedSlots);
        });
    }
}
