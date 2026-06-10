package com.coolerpromc.restrictedinventory;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
	public static final String MODID = "restrictedinventory";
	public static final String MOD_NAME = "RestrictedInventory";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	public static ResourceLocation id(String name){
		return ResourceLocation.fromNamespaceAndPath(MODID, name);
	}
}