package com.coolerpromc.restrictedinventory.platform;

import com.coolerpromc.restrictedinventory.FabricRestrictedInventory;
import com.coolerpromc.restrictedinventory.platform.services.IPlatformHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT);
    }

    @Override
    public Map<Integer, String> getRestrictedSlots(Player player) {
        return player.getAttachedOrCreate(FabricRestrictedInventory.RESTRICTED_SLOTS_ATTACHMENT);
    }

    @Override
    public void setRestrictedSlots(Player player, Map<Integer, String> restrictedSlots) {
        player.setAttached(FabricRestrictedInventory.RESTRICTED_SLOTS_ATTACHMENT, restrictedSlots);
    }

    @Override
    public void syncRestrictedSlots(Map<Integer, String> restrictedSlots) {
        MinecraftServer server = FabricRestrictedInventory.MINECRAFT_SERVER;
        if (server != null){
            PlayerLookup.all(server).forEach(p -> setRestrictedSlots(p, restrictedSlots));
        }
    }
}
