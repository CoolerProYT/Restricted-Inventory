package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.command.ModCommands;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import com.coolerpromc.restrictedinventory.network.ServerBoundClientRestrictedSlotsPacket;
import com.coolerpromc.restrictedinventory.network.ServerBoundRestrictionUpdatePacket;
import com.coolerpromc.restrictedinventory.platform.util.FabricPayloadContext;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public class FabricRestrictedInventory implements ModInitializer {
    public static MinecraftServer MINECRAFT_SERVER;
    public static final AttachmentType<Map<Integer, ItemEntry>> RESTRICTED_SLOTS_ATTACHMENT =
            AttachmentRegistry.<Map<Integer, ItemEntry>>builder()
                    .copyOnDeath().initializer(HashMap::new)
                    .persistent(Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, Object::toString), ItemEntry.STORAGE_CODEC))
                    .buildAndRegister(Constants.id("restricted_slots_attachment"));

    @Override
    public void onInitialize() {
        RestrictedInventory.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> MINECRAFT_SERVER = server);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((p, b) -> RestrictedInventory.syncCommonRestrictedInventory(p));

        ServerPlayNetworking.registerGlobalReceiver(ServerBoundClientRestrictedSlotsPacket.TYPE, (server, player, handler, buf, responseSender) -> ServerBoundClientRestrictedSlotsPacket.decode(buf).handle(new FabricPayloadContext(server, player, handler)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundRestrictionUpdatePacket.TYPE, (server, player, handler, buf, responseSender) -> ServerBoundRestrictionUpdatePacket.decode(buf).handle(new FabricPayloadContext(server, player, handler)));

        CommandRegistrationCallback.EVENT.register((dispatcher, b, s) -> ModCommands.register(dispatcher));
    }
}
