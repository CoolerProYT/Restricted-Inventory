package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.config.ClientConfig;
import com.coolerpromc.restrictedinventory.network.ServerBoundClientRestrictedSlotsPacket;
import com.coolerpromc.restrictedinventory.platform.Services;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Constants.MODID, dist = Dist.CLIENT)
public class NeoForgeRestrictedInventoryClient {
    public NeoForgeRestrictedInventoryClient(IEventBus eventBus) {
        RestrictedInventoryClient.init();

        NeoForge.EVENT_BUS.addListener(this::onClientPlayerNetwork);
    }

    public void onClientPlayerNetwork(ClientPlayerNetworkEvent.LoggingIn event) {
        Services.NETWORK.sendToServer(new ServerBoundClientRestrictedSlotsPacket(ClientConfig.getRestrictedSlots()));
    }
}