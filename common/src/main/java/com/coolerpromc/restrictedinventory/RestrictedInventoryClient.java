package com.coolerpromc.restrictedinventory;

import com.coolerpromc.restrictedinventory.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class RestrictedInventoryClient {
    public static Screen pendingScreen = null;

    public static void init(){
        ClientConfig.init();
    }

    public static void tick(Minecraft minecraft) {
        if (pendingScreen != null) {
            minecraft.setScreen(pendingScreen);
            pendingScreen = null;
        }
    }
}
