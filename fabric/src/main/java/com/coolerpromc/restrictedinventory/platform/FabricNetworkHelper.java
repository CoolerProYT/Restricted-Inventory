package com.coolerpromc.restrictedinventory.platform;

import com.coolerpromc.restrictedinventory.FabricRestrictedInventory;
import com.coolerpromc.restrictedinventory.network.CustomPacket;
import com.coolerpromc.restrictedinventory.platform.services.INetworkHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class FabricNetworkHelper implements INetworkHelper {
    @Override
    public <T extends CustomPacket> void sendToPlayer(ServerPlayer player, T packet) {
        ServerPlayNetworking.send(player, packet.id(), packet.encode(PacketByteBufs.create()));
    }

    @Override
    public <T extends CustomPacket> void sendToAllPlayer(T packet) {
        MinecraftServer server = FabricRestrictedInventory.MINECRAFT_SERVER;
        if (server != null){
            PlayerLookup.all(server).forEach(p -> sendToPlayer(p, packet));
        }
    }

    @Override
    public <T extends CustomPacket> void sendToServer(T packet) {
        ClientPlayNetworking.send(packet.id(), packet.encode(PacketByteBufs.create()));
    }
}
