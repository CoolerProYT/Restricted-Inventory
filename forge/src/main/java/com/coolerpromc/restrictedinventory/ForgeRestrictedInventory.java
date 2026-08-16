package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.command.ModCommands;
import com.coolerpromc.restrictedinventory.network.*;
import com.coolerpromc.restrictedinventory.platform.util.ForgeClientPayloadContext;
import com.coolerpromc.restrictedinventory.platform.util.ForgePayloadContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(Constants.MODID)
public class ForgeRestrictedInventory {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Constants.id("main"),
            () -> PROTOCOL_VERSION,
            s -> true,
            s -> true
    );

    public ForgeRestrictedInventory() {
        RestrictedInventory.init();

        MinecraftForge.EVENT_BUS.addListener(this::onDatapackSync);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
        this.onRegisterPayloadHandlers();
    }

    public void onDatapackSync(OnDatapackSyncEvent event) {
        RestrictedInventory.syncCommonRestrictedInventory(event.getPlayer());
    }

    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        RestrictedInventory.refreshTargetedRestrictions(event.getServer());
    }

    public void onRegisterPayloadHandlers() {
        CHANNEL.registerMessage(0, ServerBoundClientRestrictedSlotsPacket.class, ServerBoundClientRestrictedSlotsPacket::encode, ServerBoundClientRestrictedSlotsPacket::decode, (p, c) -> {
            p.handle(new ForgePayloadContext(c.get()));
            c.get().setPacketHandled(true);
        });
        CHANNEL.registerMessage(3, ServerBoundRestrictionUpdatePacket.class, ServerBoundRestrictionUpdatePacket::encode, ServerBoundRestrictionUpdatePacket::decode, (p, c) -> {
            p.handle(new ForgePayloadContext(c.get()));
            c.get().setPacketHandled(true);
        });
        CHANNEL.registerMessage(1, ClientBoundNotifyUpdatePacket.class, ClientBoundNotifyUpdatePacket::encode, ClientBoundNotifyUpdatePacket::decode, (p, c) -> {
            p.handle(new ForgeClientPayloadContext(c.get()));
            c.get().setPacketHandled(true);
        });
        CHANNEL.registerMessage(4, ClientBoundCommonConfigSyncPacket.class, ClientBoundCommonConfigSyncPacket::encode, ClientBoundCommonConfigSyncPacket::decode, (p, c) -> {
            p.handle(new ForgeClientPayloadContext(c.get()));
            c.get().setPacketHandled(true);
        });
        CHANNEL.registerMessage(2, ClientBoundAttachmentSyncPacket.class, ClientBoundAttachmentSyncPacket::encode, ClientBoundAttachmentSyncPacket::decode, (p, c) -> {
            p.handle(new ForgeClientPayloadContext(c.get()));
            c.get().setPacketHandled(true);
        });
        CHANNEL.registerMessage(5, ClientBoundTargetedRestrictionsSyncPacket.class, ClientBoundTargetedRestrictionsSyncPacket::encode, ClientBoundTargetedRestrictionsSyncPacket::decode, (p, c) -> {
            p.handle(new ForgeClientPayloadContext(c.get()));
            c.get().setPacketHandled(true);
        });
    }

    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}