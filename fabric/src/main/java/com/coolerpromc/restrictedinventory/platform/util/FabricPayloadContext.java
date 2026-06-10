package com.coolerpromc.restrictedinventory.platform.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record FabricPayloadContext(MinecraftServer server, Player player, ServerGamePacketListenerImpl listener) implements PayloadContext{
    @Override
    public Player player() {
        return player;
    }

    @Override
    public Level level() {
        return player.level();
    }

    @Override
    public void execute(Runnable runnable) {
        server.execute(runnable);
    }

    @Override
    public void disconnect(Component reason) {
        listener.disconnect(reason);
    }
}
