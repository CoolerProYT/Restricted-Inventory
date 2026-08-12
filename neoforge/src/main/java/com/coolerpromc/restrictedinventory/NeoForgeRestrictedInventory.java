package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.command.ModCommands;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import com.coolerpromc.restrictedinventory.network.*;
import com.coolerpromc.restrictedinventory.platform.util.NeoForgePayloadContext;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Map;
import java.util.function.Supplier;

@Mod(Constants.MODID)
public class NeoForgeRestrictedInventory {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MODID);
    public static final Supplier<AttachmentType<Map<Integer, ItemEntry>>> RESTRICTED_SLOTS_ATTACHMENT = ATTACHMENTS.register("restricted_slots_attachment", () -> AttachmentType.<Map<Integer, ItemEntry>>builder(Map::of).serialize(
            Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, Object::toString), ItemEntry.STORAGE_CODEC)).copyOnDeath().build());

    public NeoForgeRestrictedInventory(IEventBus eventBus) {
        RestrictedInventory.init();

        ATTACHMENTS.register(eventBus);
        NeoForge.EVENT_BUS.addListener(this::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        eventBus.addListener(this::onRegisterPayloadHandlers);
    }

    public void onDatapackSync(OnDatapackSyncEvent event) {
        RestrictedInventory.syncCommonRestrictedInventory(event.getPlayer());
    }

    public void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(ServerBoundClientRestrictedSlotsPacket.TYPE, ServerBoundClientRestrictedSlotsPacket.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToServer(ServerBoundRestrictionUpdatePacket.TYPE, ServerBoundRestrictionUpdatePacket.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToClient(ClientBoundNotifyUpdatePacket.TYPE, ClientBoundNotifyUpdatePacket.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToClient(ClientBoundCommonConfigSyncPacket.TYPE, ClientBoundCommonConfigSyncPacket.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToClient(ClientBoundAttachmentSyncPacket.TYPE, ClientBoundAttachmentSyncPacket.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
    }

    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}