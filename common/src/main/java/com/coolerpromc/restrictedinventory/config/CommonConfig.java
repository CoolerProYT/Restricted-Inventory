package com.coolerpromc.restrictedinventory.config;

import com.coolerpromc.coolerconfig.config.*;
import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.util.Restriction;
import com.coolerpromc.restrictedinventory.config.util.RestrictionGroup;
import com.coolerpromc.restrictedinventory.config.util.RestrictionGroups;
import com.coolerpromc.restrictedinventory.config.util.RestrictionProfile;
import com.coolerpromc.restrictedinventory.config.util.TargetedRestrictions;
import com.coolerpromc.restrictedinventory.network.ClientBoundCommonConfigSyncPacket;
import com.coolerpromc.restrictedinventory.network.ClientBoundNotifyUpdatePacket;
import com.coolerpromc.restrictedinventory.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommonConfig {
    public static final ConfigValue<Boolean> USE_CLIENT_RESTRICTION;
    public static final ConfigValue<Boolean> USE_TEAM_AND_TAG_RESTRICTIONS;
    public static final ConfigValue<Map<String, Restriction>> RESTRICTED_SLOTS;
    public static final ConfigValue<Map<String, RestrictionGroup>> GROUPS;
    public static final ConfigValue<Map<String, RestrictionProfile>> TEAM_RESTRICTIONS;
    public static final ConfigValue<Map<String, RestrictionProfile>> TAG_RESTRICTIONS;
    public static ClientCache clientCache = ClientCache.EMPTY;

    public static ConfigSpec CONFIG;

    static {
        ConfigBuilder builder = ConfigSpec.builder(Constants.MODID, ConfigFormat.JSON).side(ConfigSide.COMMON).watchForChanges();

        USE_CLIENT_RESTRICTION = builder.defineBoolean("useClientRestriction", false, "Inventory restriction will be per client instead of everyone being same");
        USE_TEAM_AND_TAG_RESTRICTIONS = builder.defineBoolean("useTeamAndTagRestrictions", false, "Let 'teamRestrictions' and 'tagRestrictions' target players by their vanilla scoreboard team and vanilla entity tags. A player matched by one of those rules uses that set instead of 'restrictedSlots'; everyone else keeps using 'restrictedSlots'");
        RESTRICTED_SLOTS = builder.defineCodec("restrictedSlots", Restriction.CONFIG_CODEC, Map.of(), "");
        GROUPS = builder.defineCodec("groups", RestrictionGroup.CONFIG_CODEC, Map.of(), "Named sets of entries a slot can be restricted to with {\"group\": \"restrictedinventory:<name>\"}");
        TEAM_RESTRICTIONS = builder.defineCodec("teamRestrictions", RestrictionProfile.CONFIG_CODEC, Map.of(), "Restrictions per vanilla scoreboard team name, as {\"<team>\": {\"restrictedSlots\": {...}}}. Only read when 'useTeamAndTagRestrictions' is true; the team does not have to exist yet");
        TAG_RESTRICTIONS = builder.defineCodec("tagRestrictions", RestrictionProfile.CONFIG_CODEC, Map.of(), "Restrictions per vanilla entity tag, as {\"<tag>\": {\"restrictedSlots\": {...}}}. Only read when 'useTeamAndTagRestrictions' is true; tags a player has that are not named here are ignored");

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
                } catch (Exception ignored){}
            }
            try{
                Services.NETWORK.sendToAllPlayer(new ClientBoundCommonConfigSyncPacket(USE_CLIENT_RESTRICTION.get(), getGroups()));
                Services.PLATFORM.updatePlayersPermission();
            }catch (Exception e){}
        });
    }

    private static void refreshGroups() {
        RestrictionGroups.setLocal(getGroups());
        RestrictionGroups.validate(getRestrictedSlots().values());
        TEAM_RESTRICTIONS.get().values().forEach(profile -> RestrictionGroups.validate(profile.restrictedSlots().values()));
        TAG_RESTRICTIONS.get().values().forEach(profile -> RestrictionGroups.validate(profile.restrictedSlots().values()));
    }

    public static Map<Integer, Restriction> restrictedSlots(Player player){
        TargetedRestrictions targeted = Services.PLATFORM.getTargetedRestrictions(player);
        return targeted.matched() ? targeted.restrictions() : Services.PLATFORM.getRestrictedSlots(player);
    }

    public static TargetedRestrictions resolveTargetedRestrictions(Player player) {
        if (!useTeamAndTagRestrictions()) return TargetedRestrictions.NONE;

        Map<String, RestrictionProfile> teamRules = TEAM_RESTRICTIONS.get();
        Map<String, RestrictionProfile> tagRules = TAG_RESTRICTIONS.get();
        if (teamRules.isEmpty() && tagRules.isEmpty()) return TargetedRestrictions.NONE;

        boolean matched = false;
        Map<Integer, Restriction> resolved = new HashMap<>();

        Team team = player.getTeam();
        if (team != null) {
            RestrictionProfile profile = teamRules.get(team.getName());
            if (profile != null) {
                matched = true;
                resolved.putAll(profile.restrictedSlots());
            }
        }

        if (!tagRules.isEmpty()) {
            List<String> tags = new ArrayList<>(player.getTags());
            Collections.sort(tags);

            for (String tag : tags) {
                RestrictionProfile profile = tagRules.get(tag);
                if (profile != null) {
                    matched = true;
                    resolved.putAll(profile.restrictedSlots());
                }
            }
        }

        if (!matched) return TargetedRestrictions.NONE;

        if (useClientRestriction()) {
            resolved.putAll(Services.PLATFORM.getRestrictedSlots(player));
        }

        return new TargetedRestrictions(true, Map.copyOf(resolved));
    }

    public static Map<Integer, Restriction> getRestrictedSlots() {
        return RESTRICTED_SLOTS.get().entrySet().stream().collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), Map.Entry::getValue));
    }

    public static Map<ResourceLocation, RestrictionGroup> getGroups() {
        return RestrictionGroups.resolve(GROUPS.get());
    }

    public static boolean useClientRestriction(){
        return USE_CLIENT_RESTRICTION.get();
    }

    public static boolean useTeamAndTagRestrictions(){
        return USE_TEAM_AND_TAG_RESTRICTIONS.get();
    }

    public record ClientCache(boolean useClientRestriction, Map<Integer, Restriction> restrictedSlots){
        public static final ClientCache EMPTY = new ClientCache(false, Map.of());
    }
}
