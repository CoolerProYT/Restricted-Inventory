package com.coolerpromc.restrictedinventory.platform;

import com.coolerpromc.restrictedinventory.capability.IRestrictedSlots;
import com.coolerpromc.restrictedinventory.capability.ModCapabilities;
import com.coolerpromc.restrictedinventory.network.ClientBoundAttachmentSyncPacket;
import com.coolerpromc.restrictedinventory.platform.services.IPlatformHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Map;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public Map<Integer, String> getRestrictedSlots(Player player) {
        return player.getCapability(ModCapabilities.RESTRICTED_SLOTS).map(IRestrictedSlots::getRestrictedSlots).orElse(Map.of());
    }

    @Override
    public void setRestrictedSlots(Player player, Map<Integer, String> restrictedSlots) {
        player.getCapability(ModCapabilities.RESTRICTED_SLOTS).ifPresent(cap -> cap.setRestrictedSlots(restrictedSlots));

        if (player instanceof ServerPlayer serverPlayer) {
            Services.NETWORK.sendToPlayer(serverPlayer, new ClientBoundAttachmentSyncPacket(restrictedSlots));
        }
    }

    @Override
    public void syncRestrictedSlots(Map<Integer, String> restrictedSlots) {
        if (ServerLifecycleHooks.getCurrentServer() != null){
            ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(p -> setRestrictedSlots(p, restrictedSlots));
        }
    }
}