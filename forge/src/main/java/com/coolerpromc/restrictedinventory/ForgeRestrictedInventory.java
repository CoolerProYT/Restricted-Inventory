package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.network.ClientBoundAttachmentSyncPacket;
import com.coolerpromc.restrictedinventory.network.ClientBoundNotifyUpdatePacket;
import com.coolerpromc.restrictedinventory.network.ServerBoundClientRestrictedSlotsPacket;
import com.coolerpromc.restrictedinventory.platform.util.ForgeClientPayloadContext;
import com.coolerpromc.restrictedinventory.platform.util.ForgePayloadContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
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
        this.onRegisterPayloadHandlers();
    }

    public void onDatapackSync(OnDatapackSyncEvent event) {
        RestrictedInventory.syncCommonRestrictedInventory(event.getPlayer());
    }

    public void onRegisterPayloadHandlers() {
        CHANNEL.registerMessage(0, ServerBoundClientRestrictedSlotsPacket.class, ServerBoundClientRestrictedSlotsPacket::encode, ServerBoundClientRestrictedSlotsPacket::decode, (p, c) -> {
            p.handle(new ForgePayloadContext(c.get()));
            c.get().setPacketHandled(true);
        });
        CHANNEL.registerMessage(1, ClientBoundNotifyUpdatePacket.class, ClientBoundNotifyUpdatePacket::encode, ClientBoundNotifyUpdatePacket::decode, (p, c) -> {
            p.handle(new ForgeClientPayloadContext(c.get()));
            c.get().setPacketHandled(true);
        });
        CHANNEL.registerMessage(2, ClientBoundAttachmentSyncPacket.class, ClientBoundAttachmentSyncPacket::encode, ClientBoundAttachmentSyncPacket::decode, (p, c) -> {
            p.handle(new ForgeClientPayloadContext(c.get()));
            c.get().setPacketHandled(true);
        });
    }
}