package com.coolerpromc.restrictedinventory.platform;

import com.coolerpromc.restrictedinventory.ForgeRestrictedInventory;
import com.coolerpromc.restrictedinventory.network.CustomPacket;
import com.coolerpromc.restrictedinventory.platform.services.INetworkHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class ForgeNetworkHelper implements INetworkHelper {
    @Override
    public <T extends CustomPacket> void sendToPlayer(ServerPlayer player, T packet) {
        ForgeRestrictedInventory.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    @Override
    public <T extends CustomPacket> void sendToAllPlayer(T packet) {
        ForgeRestrictedInventory.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    @Override
    public <T extends CustomPacket> void sendToServer(T packet) {
        ForgeRestrictedInventory.CHANNEL.sendToServer(packet);
    }
}
