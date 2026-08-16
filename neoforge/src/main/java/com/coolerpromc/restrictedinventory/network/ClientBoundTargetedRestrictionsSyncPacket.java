package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.NeoForgeRestrictedInventory;
import com.coolerpromc.restrictedinventory.config.util.TargetedRestrictions;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientBoundTargetedRestrictionsSyncPacket(TargetedRestrictions targetedRestrictions) implements CustomPacketPayload {
    public static final Type<ClientBoundTargetedRestrictionsSyncPacket> TYPE = new Type<>(Constants.id("targeted_restrictions_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundTargetedRestrictionsSyncPacket> STREAM_CODEC = StreamCodec.composite(
            TargetedRestrictions.STREAM_CODEC,
            ClientBoundTargetedRestrictionsSyncPacket::targetedRestrictions,
            ClientBoundTargetedRestrictionsSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            context.player().setData(NeoForgeRestrictedInventory.TARGETED_RESTRICTIONS_ATTACHMENT, targetedRestrictions);
        });
    }
}
