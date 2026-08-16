package com.coolerpromc.restrictedinventory.network;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.util.Restriction;
import com.coolerpromc.restrictedinventory.config.util.TargetedRestrictions;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.platform.util.PayloadContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ClientBoundTargetedRestrictionsSyncPacket(TargetedRestrictions targetedRestrictions) implements CustomPacket {
    public static final ResourceLocation TYPE = Constants.id("targeted_restrictions_sync");

    @Override
    public FriendlyByteBuf encode(FriendlyByteBuf buf) {
        buf.writeBoolean(targetedRestrictions.matched());
        buf.writeMap(targetedRestrictions.restrictions(), FriendlyByteBuf::writeInt, (buf1, restriction) -> buf1.writeJsonWithCodec(Restriction.CODEC, restriction));
        return buf;
    }

    public static ClientBoundTargetedRestrictionsSyncPacket decode(FriendlyByteBuf buf){
        boolean matched = buf.readBoolean();
        return new ClientBoundTargetedRestrictionsSyncPacket(new TargetedRestrictions(
                matched,
                buf.readMap(FriendlyByteBuf::readInt, buf1 -> buf1.readJsonWithCodec(Restriction.CODEC))
        ));
    }

    @Override
    public ResourceLocation id() {
        return TYPE;
    }

    @Override
    public void handle(PayloadContext context){
        context.execute(() -> {
            Services.PLATFORM.setTargetedRestrictions(context.player(), targetedRestrictions);
        });
    }
}
