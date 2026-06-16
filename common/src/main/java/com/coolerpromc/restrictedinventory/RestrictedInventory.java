package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.network.ClientBoundCommonConfigSyncPacket;
import com.coolerpromc.restrictedinventory.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class RestrictedInventory {
    public static void init() {
        CommonConfig.init();
    }

    public static void syncCommonRestrictedInventory(Player player){
        if (!CommonConfig.useClientRestriction()){
            Services.PLATFORM.setRestrictedSlots(player, CommonConfig.getRestrictedSlots());
        }
        if (player instanceof ServerPlayer serverPlayer) {
            try {
                Services.NETWORK.sendToPlayer(serverPlayer, new ClientBoundCommonConfigSyncPacket(CommonConfig.useClientRestriction()));
            } catch (Exception ignored) {}
        }
    }
}