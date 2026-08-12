package com.coolerpromc.restrictedinventory.platform.services;

import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public interface IPlatformHelper {
    String getPlatformName();
    boolean isModLoaded(String modId);
    boolean isDevelopmentEnvironment();
    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
    boolean isClient();
    Map<Integer, ItemEntry> getRestrictedSlots(Player player);
    void setRestrictedSlots(Player player, Map<Integer, ItemEntry> restrictedSlots);
    void syncRestrictedSlots(Map<Integer, ItemEntry> restrictedSlots);
    void updatePlayersPermission();
}