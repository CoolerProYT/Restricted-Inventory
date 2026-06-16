package com.coolerpromc.restrictedinventory.platform.services;

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
    Map<Integer, String> getRestrictedSlots(Player player);
    void setRestrictedSlots(Player player, Map<Integer, String> restrictedSlots);
    void syncRestrictedSlots(Map<Integer, String> restrictedSlots);
    void updatePlayersPermission();
}