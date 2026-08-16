package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.TargetedRestrictions;
import com.coolerpromc.restrictedinventory.network.ClientBoundCommonConfigSyncPacket;
import com.coolerpromc.restrictedinventory.platform.Services;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class RestrictedInventory {
    public static void init() {
        CommonConfig.init();
    }

    public static void syncCommonRestrictedInventory(@Nullable Player player){
        if (player == null) return;

        if (!CommonConfig.useClientRestriction()){
            Services.PLATFORM.setRestrictedSlots(player, CommonConfig.getRestrictedSlots());
        }
        refreshTargetedRestrictions(player);
        if (player instanceof ServerPlayer serverPlayer) {
            try {
                Services.NETWORK.sendToPlayer(serverPlayer, new ClientBoundCommonConfigSyncPacket(CommonConfig.useClientRestriction(), CommonConfig.getGroups()));
            } catch (Exception ignored) {}
        }
    }

    public static void refreshTargetedRestrictions(MinecraftServer server){
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            refreshTargetedRestrictions(player);
        }
    }

    public static void refreshTargetedRestrictions(Player player){
        TargetedRestrictions resolved = CommonConfig.resolveTargetedRestrictions(player);
        if (!resolved.equals(Services.PLATFORM.getTargetedRestrictions(player))) {
            Services.PLATFORM.setTargetedRestrictions(player, resolved);
        }
    }
}
