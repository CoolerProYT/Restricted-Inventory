package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.command.ModClientCommands;
import com.coolerpromc.restrictedinventory.config.ClientConfig;
import com.coolerpromc.restrictedinventory.network.ClientBoundCommonConfigSyncPacket;
import com.coolerpromc.restrictedinventory.network.ClientBoundNotifyUpdatePacket;
import com.coolerpromc.restrictedinventory.network.ServerBoundClientRestrictedSlotsPacket;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.platform.util.FabricClientPayloadContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricRestrictedInventoryClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RestrictedInventoryClient.init();
        ClientTickEvents.END_CLIENT_TICK.register(RestrictedInventoryClient::tick);

        ClientPlayNetworking.registerGlobalReceiver(ClientBoundNotifyUpdatePacket.TYPE, (payload, context) -> payload.handle(new FabricClientPayloadContext(context)));
        ClientPlayNetworking.registerGlobalReceiver(ClientBoundCommonConfigSyncPacket.TYPE, (payload, context) -> payload.handle(new FabricClientPayloadContext(context)));

        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
            Services.NETWORK.sendToServer(new ServerBoundClientRestrictedSlotsPacket(ClientConfig.getRestrictedSlots()));
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, c) -> ModClientCommands.register(dispatcher));
    }
}
