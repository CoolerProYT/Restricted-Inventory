package com.coolerpromc.restrictedinventory.config;

import com.coolerpromc.coolerconfig.config.*;
import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import com.coolerpromc.restrictedinventory.network.ServerBoundClientRestrictedSlotsPacket;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.stream.Collectors;

public class ClientConfig {
    public static final ConfigValue<Boolean> SHOW_SLOT_INDEX;
    public static final ConfigValue<Map<String, Either<String, ItemEntry>>> RESTRICTED_SLOTS;

    public static ConfigSpec CONFIG;

    static {
        ConfigBuilder builder = ConfigSpec.builder(Constants.MODID, ConfigFormat.JSON).side(ConfigSide.CLIENT).watchForChanges();

        SHOW_SLOT_INDEX = builder.defineBoolean("showSlotIndex", true, "Show slot index in menu screen when tab is pressed");
        RESTRICTED_SLOTS = builder.defineCodec("restrictedSlots", ItemEntry.CONFIG_CODEC, Map.of(), "");

        CONFIG = builder.build();
    }

    public static void init(){
        CONFIG.addReloadListener(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.getConnection() == null) return;
            Services.NETWORK.sendToServer(new ServerBoundClientRestrictedSlotsPacket(getRestrictedSlots()));
        });
    }

    public static Map<Integer, ItemEntry> getRestrictedSlots() {
        return RESTRICTED_SLOTS.get().entrySet().stream().collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), e -> ItemEntry.of(e.getValue())));
    }

    public static boolean showSlotIndex(){
        return SHOW_SLOT_INDEX.get();
    }
}
