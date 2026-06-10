package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.config.ClientConfig;
import com.coolerpromc.restrictedinventory.network.ClientBoundAttachmentSyncPacket;
import com.coolerpromc.restrictedinventory.network.ClientBoundNotifyUpdatePacket;
import com.coolerpromc.restrictedinventory.network.ServerBoundClientRestrictedSlotsPacket;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.platform.util.FabricClientPayloadContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricRestrictedInventoryClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RestrictedInventoryClient.init();

        ClientPlayNetworking.registerGlobalReceiver(ClientBoundNotifyUpdatePacket.TYPE, (client, handler, buf, sender) -> ClientBoundNotifyUpdatePacket.decode(buf).handle(new FabricClientPayloadContext(client, handler)));
        ClientPlayNetworking.registerGlobalReceiver(ClientBoundAttachmentSyncPacket.TYPE, (client, handler, buf, sender) -> ClientBoundAttachmentSyncPacket.decode(buf).handle(new FabricClientPayloadContext(client, handler)));

        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
            Services.NETWORK.sendToServer(new ServerBoundClientRestrictedSlotsPacket(ClientConfig.getRestrictedSlots()));
        });
    }
}
