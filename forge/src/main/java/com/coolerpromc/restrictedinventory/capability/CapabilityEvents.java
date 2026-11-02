package com.coolerpromc.restrictedinventory.capability;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.platform.Services;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class CapabilityEvents {

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        ModCapabilities.register(event);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(
                Constants.id("restricted_slots"),
                new RestrictedSlotsProvider()
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(ModCapabilities.RESTRICTED_SLOTS).ifPresent(oldCap -> {
            event.getEntity().getCapability(ModCapabilities.RESTRICTED_SLOTS).ifPresent(newCap -> {
                newCap.setRestrictedSlots(new HashMap<>(oldCap.getRestrictedSlots()));
            });
        });
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        // Clone preserves server data, but the replacement client player needs a fresh sync.
        Services.PLATFORM.setRestrictedSlots(player, Services.PLATFORM.getRestrictedSlots(player));
        Services.PLATFORM.setTargetedRestrictions(player, CommonConfig.resolveTargetedRestrictions(player));
    }
}
