package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.network.ServerBoundClientRestrictedSlotsPacket;
import com.coolerpromc.restrictedinventory.platform.util.FabricPayloadContext;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FabricRestrictedInventory implements ModInitializer {
    public static MinecraftServer MINECRAFT_SERVER;
    public static final AttachmentType<Map<Integer, String>> RESTRICTED_SLOTS_ATTACHMENT = AttachmentRegistry.<Map<Integer, String>>builder().copyOnDeath().initializer(HashMap::new).persistent(Codec.unboundedMap(Codec.INT, Codec.STRING).xmap(HashMap::new, Function.identity())).buildAndRegister(Constants.id("restricted_slots_attachment"));

    @Override
    public void onInitialize() {
        RestrictedInventory.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> MINECRAFT_SERVER = server);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((p, b) -> RestrictedInventory.syncCommonRestrictedInventory(p));

        ServerPlayNetworking.registerGlobalReceiver(ServerBoundClientRestrictedSlotsPacket.TYPE, (server, player, handler, buf, responseSender) -> ServerBoundClientRestrictedSlotsPacket.decode(buf).handle(new FabricPayloadContext(server, player, handler)));
    }
}
