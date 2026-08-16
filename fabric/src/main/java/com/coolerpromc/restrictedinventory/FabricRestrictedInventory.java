package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.command.ModCommands;
import com.coolerpromc.restrictedinventory.config.util.Restriction;
import com.coolerpromc.restrictedinventory.config.util.TargetedRestrictions;
import com.coolerpromc.restrictedinventory.network.ServerBoundClientRestrictedSlotsPacket;
import com.coolerpromc.restrictedinventory.network.ServerBoundRestrictionUpdatePacket;
import com.coolerpromc.restrictedinventory.platform.util.FabricPayloadContext;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public class FabricRestrictedInventory implements ModInitializer {
    public static MinecraftServer MINECRAFT_SERVER;
    public static final AttachmentType<Map<Integer, Restriction>> RESTRICTED_SLOTS_ATTACHMENT =
            AttachmentRegistry.<Map<Integer, Restriction>>builder()
                    .copyOnDeath().initializer(HashMap::new)
                    .persistent(Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, Object::toString), Restriction.CODEC))
                    .buildAndRegister(Constants.id("restricted_slots_attachment"));

    // Resolved from live team/tag state every tick, so it is never written to disk. This Fabric API
    // version has no attachment sync, so the value reaches the client through
    // ClientBoundTargetedRestrictionsSyncPacket, exactly as the restricted slots do.
    public static final AttachmentType<TargetedRestrictions> TARGETED_RESTRICTIONS_ATTACHMENT =
            AttachmentRegistry.<TargetedRestrictions>builder()
                    .initializer(() -> TargetedRestrictions.NONE)
                    .buildAndRegister(Constants.id("targeted_restrictions_attachment"));

    @Override
    public void onInitialize() {
        RestrictedInventory.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> MINECRAFT_SERVER = server);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((p, b) -> RestrictedInventory.syncCommonRestrictedInventory(p));
        ServerTickEvents.END_SERVER_TICK.register(RestrictedInventory::refreshTargetedRestrictions);

        ServerPlayNetworking.registerGlobalReceiver(ServerBoundClientRestrictedSlotsPacket.TYPE, (server, player, handler, buf, responseSender) -> ServerBoundClientRestrictedSlotsPacket.decode(buf).handle(new FabricPayloadContext(server, player, handler)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundRestrictionUpdatePacket.TYPE, (server, player, handler, buf, responseSender) -> ServerBoundRestrictionUpdatePacket.decode(buf).handle(new FabricPayloadContext(server, player, handler)));

        CommandRegistrationCallback.EVENT.register((dispatcher, b, s) -> ModCommands.register(dispatcher));
    }
}
