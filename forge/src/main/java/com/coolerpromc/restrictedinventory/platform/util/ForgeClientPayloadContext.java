package com.coolerpromc.restrictedinventory.platform.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

@OnlyIn(Dist.CLIENT)
public record ForgeClientPayloadContext(NetworkEvent.Context context) implements PayloadContext {
    @Override
    public Player player() {
        return Minecraft.getInstance().player;
    }

    @Override
    public Level level() {
        return player().level();
    }

    @Override
    public void execute(Runnable runnable) {
        context.enqueueWork(runnable);
    }

    @Override
    public void disconnect(Component reason) {
        context.getNetworkManager().disconnect(reason);
    }
}
