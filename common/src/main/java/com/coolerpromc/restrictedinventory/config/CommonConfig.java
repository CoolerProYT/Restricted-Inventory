package com.coolerpromc.restrictedinventory.config;

import com.coolerpromc.coolerconfig.config.*;
import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.network.ClientBoundCommonConfigSyncPacket;
import com.coolerpromc.restrictedinventory.network.ClientBoundNotifyUpdatePacket;
import com.coolerpromc.restrictedinventory.platform.Services;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.stream.Collectors;

public class CommonConfig {
    public static final ConfigValue<Boolean> USE_CLIENT_RESTRICTION;
    public static final ConfigValue<Map<String, String>> RESTRICTED_SLOTS;
    public static ClientCache clientCache = ClientCache.EMPTY;

    public static ConfigSpec CONFIG;

    static {
        ConfigBuilder builder = ConfigSpec.builder(Constants.MODID, ConfigFormat.JSON).side(ConfigSide.COMMON).watchForChanges();

        USE_CLIENT_RESTRICTION = builder.defineBoolean("useClientRestriction", false, "Inventory restriction will be per client instead of everyone being same");
        RESTRICTED_SLOTS = builder.define("restrictedSlots", Map.of(), "", o -> o instanceof Map<?,?> map && map.keySet().stream().allMatch(k -> k instanceof String s && s.matches("\\d+") && Integer.parseInt(s) >= 0 && Integer.parseInt(s) <= 35));

        CONFIG = builder.build();
    }

    public static void init(){
        CONFIG.addReloadListener(() -> {
            if (!USE_CLIENT_RESTRICTION.get()){
                Services.PLATFORM.syncRestrictedSlots(getRestrictedSlots());
            }
            else{
                try{
                    Services.NETWORK.sendToAllPlayer(new ClientBoundNotifyUpdatePacket());
                } catch (Exception ignored){}
            }
            try{
                Services.NETWORK.sendToAllPlayer(new ClientBoundCommonConfigSyncPacket(USE_CLIENT_RESTRICTION.get()));
                Services.PLATFORM.updatePlayersPermission();
            }catch (Exception e){}
        });
    }

    public static Map<Integer, String> restrictedSlots(Player player){
        return Services.PLATFORM.getRestrictedSlots(player);
    }

    public static Map<Integer, String> getRestrictedSlots() {
        return RESTRICTED_SLOTS.get().entrySet().stream().collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), Map.Entry::getValue));
    }

    public static boolean useClientRestriction(){
        return USE_CLIENT_RESTRICTION.get();
    }

    public record ClientCache(boolean useClientRestriction, Map<Integer, String> restrictedSlots){
        public static final ClientCache EMPTY = new ClientCache(false, Map.of());
    }
}
