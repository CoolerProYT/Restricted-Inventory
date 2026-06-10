package com.coolerpromc.restrictedinventory.platform.services;

import com.coolerpromc.restrictedinventory.network.CustomPacket;
import net.minecraft.server.level.ServerPlayer;

public interface INetworkHelper {
    <T extends CustomPacket> void sendToPlayer(ServerPlayer player, T packet);
    <T extends CustomPacket> void sendToAllPlayer(T packet);
    <T extends CustomPacket> void sendToServer(T packet);
}