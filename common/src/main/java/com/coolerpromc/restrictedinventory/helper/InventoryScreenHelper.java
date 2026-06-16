package com.coolerpromc.restrictedinventory.helper;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.mixin.accessor.AbstractContainerScreenAccessor;
import com.coolerpromc.restrictedinventory.mixin.accessor.ScreenAccessor;
import com.coolerpromc.restrictedinventory.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class InventoryScreenHelper {
    public static <T extends AbstractContainerMenu> void extractSlotIndex(GuiGraphics graphics, AbstractContainerScreen<T> screen){
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
        ScreenAccessor screenAccessor = (ScreenAccessor) screen;
            for (Slot slot : screen.getMenu().slots){
            if (isModifiableSlot(slot)){
                int x = slot.x + accessor.restrictedinventory$getLeftPos() + 8;
                int y = slot.y + accessor.restrictedinventory$getTopPos() + (screenAccessor.getFont().lineHeight / 2);
                graphics.drawCenteredString(screenAccessor.getFont(), String.valueOf(slot.getContainerSlot()), x, y, -1);
            }
        }
    }

    public static <T extends AbstractContainerMenu> void extractRestrictedSlot(GuiGraphics graphics, AbstractContainerScreen<T> screen) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
        Player player = Minecraft.getInstance().player;
        for (Map.Entry<Integer, String> restrictedSlot : CommonConfig.restrictedSlots(player).entrySet()){
            Slot slot = getSlotByInventoryIndex(screen.getMenu().slots, restrictedSlot.getKey());
            if (slot != null && slot.getItem().isEmpty()){
                int x = slot.x + accessor.restrictedinventory$getLeftPos();
                int y = slot.y + accessor.restrictedinventory$getTopPos();

                graphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);

                String value = CommonConfig.restrictedSlots(player).get(slot.getContainerSlot());
                if (value.startsWith("#")){
                    TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(value.substring(1)));
                    HolderSet<Item> items = BuiltInRegistries.ITEM.getOrCreateTag(tag);

                    int size = items.size();
                    if (size > 0) {
                        int index = (int) ((System.currentTimeMillis() / 1000) % size);
                        Item tagItem = items.get(index).value();
                        graphics.renderFakeItem(tagItem.getDefaultInstance(), x, y);
                    }
                }
                else {
                    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(value));
                    graphics.renderFakeItem(item.getDefaultInstance(), x, y);
                }
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 200);
                graphics.fill(x, y, x + 16, y + 16, 0xCC8B8B8B);
                graphics.pose().popPose();
            }
        }
    }

    private static boolean isModifiableSlot(Slot slot) {
        return (slot.container instanceof Inventory && !(slot instanceof ArmorSlot) && slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 35) || Services.SLOT.isModifiableSlot(slot);
    }

    private static @Nullable Slot getSlotByInventoryIndex(NonNullList<Slot> slots, int index){
        for (Slot slot : slots){
            if (isModifiableSlot(slot) && slot.getContainerSlot() == index){
                return slot;
            }
        }
        return null;
    }
}
