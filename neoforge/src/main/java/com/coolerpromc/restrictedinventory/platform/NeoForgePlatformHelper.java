package com.coolerpromc.restrictedinventory.platform;

import com.coolerpromc.restrictedinventory.NeoForgeRestrictedInventory;
import com.coolerpromc.restrictedinventory.platform.services.IPlatformHelper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Map;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.getDist().isClient();
    }

    @Override
    public Map<Integer, String> getRestrictedSlots(Player player) {
        return player.getData(NeoForgeRestrictedInventory.RESTRICTED_SLOTS_ATTACHMENT);
    }

    @Override
    public void setRestrictedSlots(Player player, Map<Integer, String> restrictedSlots) {
        player.setData(NeoForgeRestrictedInventory.RESTRICTED_SLOTS_ATTACHMENT, restrictedSlots);
    }

    @Override
    public void syncRestrictedSlots(Map<Integer, String> restrictedSlots) {
        if (ServerLifecycleHooks.getCurrentServer() != null){
            ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(p -> setRestrictedSlots(p, restrictedSlots));
        }
    }
}