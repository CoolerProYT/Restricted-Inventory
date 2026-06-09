package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.network.ClientBoundNotifyUpdatePacket;
import com.coolerpromc.restrictedinventory.network.ServerBoundClientRestrictedSlotsPacket;
import com.coolerpromc.restrictedinventory.platform.util.FabricPayloadContext;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public class FabricRestrictedInventory implements ModInitializer {
    public static MinecraftServer MINECRAFT_SERVER;
    public static final AttachmentType<Map<Integer, String>> RESTRICTED_SLOTS_ATTACHMENT = AttachmentRegistry.create(Constants.id("restricted_slots_attachment"), b -> b.copyOnDeath().initializer(Map::of).syncWith(ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, ByteBufCodecs.STRING_UTF8), AttachmentSyncPredicate.all()).persistent(Codec.unboundedMap(Codec.INT, Codec.STRING)));

    @Override
    public void onInitialize() {
        RestrictedInventory.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> MINECRAFT_SERVER = server);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((p, b) -> RestrictedInventory.syncCommonRestrictedInventory(p));

        PayloadTypeRegistry.serverboundPlay().register(ServerBoundClientRestrictedSlotsPacket.TYPE, ServerBoundClientRestrictedSlotsPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundNotifyUpdatePacket.TYPE, ClientBoundNotifyUpdatePacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerBoundClientRestrictedSlotsPacket.TYPE, (payload, context) -> payload.handle(new FabricPayloadContext(context)));
    }
}
