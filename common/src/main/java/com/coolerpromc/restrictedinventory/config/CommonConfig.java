package com.coolerpromc.restrictedinventory.config;

import com.coolerpromc.coolerconfig.config.*;
import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.util.Restriction;
import com.coolerpromc.restrictedinventory.config.util.RestrictionGroup;
import com.coolerpromc.restrictedinventory.config.util.RestrictionGroups;
import com.coolerpromc.restrictedinventory.network.ClientBoundCommonConfigSyncPacket;
import com.coolerpromc.restrictedinventory.network.ClientBoundNotifyUpdatePacket;
import com.coolerpromc.restrictedinventory.platform.Services;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.stream.Collectors;

public class CommonConfig {
    public static final ConfigValue<Boolean> USE_CLIENT_RESTRICTION;
    public static final ConfigValue<Map<String, Restriction>> RESTRICTED_SLOTS;
    public static final ConfigValue<Map<String, RestrictionGroup>> GROUPS;
    public static ClientCache clientCache = ClientCache.EMPTY;

    public static ConfigSpec CONFIG;

    static {
        ConfigBuilder builder = ConfigSpec.builder(Constants.MODID, ConfigFormat.JSON).side(ConfigSide.COMMON).watchForChanges();

        USE_CLIENT_RESTRICTION = builder.defineBoolean("useClientRestriction", false, "Inventory restriction will be per client instead of everyone being same");
        RESTRICTED_SLOTS = builder.defineCodec("restrictedSlots", Restriction.CONFIG_CODEC, Map.of(), "");
        GROUPS = builder.defineCodec("groups", RestrictionGroup.CONFIG_CODEC, Map.of(), "Named sets of entries a slot can be restricted to with {\"group\": \"restrictedinventory:<name>\"}");

        CONFIG = builder.build();
    }

    public static void init(){
        refreshGroups();

        CONFIG.addReloadListener(() -> {
            refreshGroups();

            if (!USE_CLIENT_RESTRICTION.get()){
                Services.PLATFORM.syncRestrictedSlots(getRestrictedSlots());
            }
            else{
                try{
                    Services.NETWORK.sendToAllPlayer(new ClientBoundNotifyUpdatePacket());
                } catch (Exception _){}
            }
            try{
                Services.NETWORK.sendToAllPlayer(new ClientBoundCommonConfigSyncPacket(USE_CLIENT_RESTRICTION.get(), getGroups()));
                Services.PLATFORM.updatePlayersPermission();
            }catch (Exception _){}
        });
    }

    private static void refreshGroups() {
        RestrictionGroups.setLocal(getGroups());
        RestrictionGroups.validate(getRestrictedSlots().values());
    }

    public static Map<Integer, Restriction> restrictedSlots(Player player){
        return Services.PLATFORM.getRestrictedSlots(player);
    }

    public static Map<Integer, Restriction> getRestrictedSlots() {
        return RESTRICTED_SLOTS.get().entrySet().stream().collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), Map.Entry::getValue));
    }

    public static Map<Identifier, RestrictionGroup> getGroups() {
        return RestrictionGroups.resolve(GROUPS.get());
    }

    public static boolean useClientRestriction(){
        return USE_CLIENT_RESTRICTION.get();
    }

    public record ClientCache(boolean useClientRestriction, Map<Integer, Restriction> restrictedSlots){
        public static final ClientCache EMPTY = new ClientCache(false, Map.of());
    }
}
