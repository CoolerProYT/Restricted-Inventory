package com.coolerpromc.restrictedinventory.platform;

import com.coolerpromc.restrictedinventory.FabricRestrictedInventory;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import com.coolerpromc.restrictedinventory.network.ClientBoundAttachmentSyncPacket;
import com.coolerpromc.restrictedinventory.platform.services.IPlatformHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
    public Map<Integer, ItemEntry> getRestrictedSlots(Player player) {
        return player.getAttachedOrCreate(FabricRestrictedInventory.RESTRICTED_SLOTS_ATTACHMENT);
    }

    @Override
    public void setRestrictedSlots(Player player, Map<Integer, ItemEntry> restrictedSlots) {
        player.setAttached(FabricRestrictedInventory.RESTRICTED_SLOTS_ATTACHMENT, restrictedSlots);
        if (player instanceof ServerPlayer serverPlayer){
            Services.NETWORK.sendToPlayer(serverPlayer, new ClientBoundAttachmentSyncPacket(restrictedSlots));
        }
    }

    @Override
    public void syncRestrictedSlots(Map<Integer, ItemEntry> restrictedSlots) {
        MinecraftServer server = FabricRestrictedInventory.MINECRAFT_SERVER;
        if (server != null){
            PlayerLookup.all(server).forEach(p -> setRestrictedSlots(p, restrictedSlots));
        }
    }

    @Override
    public void updatePlayersPermission() {
        MinecraftServer server = FabricRestrictedInventory.MINECRAFT_SERVER;
        if (server != null){
            server.getPlayerList().getPlayers().forEach(p -> server.getPlayerList().sendPlayerPermissionLevel(p));
        }
    }
}
