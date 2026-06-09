package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.config.ClientConfig;
import com.coolerpromc.restrictedinventory.network.ClientBoundNotifyUpdatePacket;
import com.coolerpromc.restrictedinventory.network.ServerBoundClientRestrictedSlotsPacket;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.platform.util.NeoForgePayloadContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Constants.MODID, dist = Dist.CLIENT)
public class NeoForgeRestrictedInventoryClient {
    public NeoForgeRestrictedInventoryClient(IEventBus eventBus) {
        RestrictedInventoryClient.init();

        eventBus.addListener(this::onRegisterClientPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(this::onClientPlayerNetwork);
    }

    public void onRegisterClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(ClientBoundNotifyUpdatePacket.TYPE, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
    }

    public void onClientPlayerNetwork(ClientPlayerNetworkEvent.LoggingIn event) {
        Services.NETWORK.sendToServer(new ServerBoundClientRestrictedSlotsPacket(ClientConfig.getRestrictedSlots()));
    }
}