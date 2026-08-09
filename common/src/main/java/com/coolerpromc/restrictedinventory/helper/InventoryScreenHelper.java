package com.coolerpromc.restrictedinventory.helper;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.mixin.accessor.AbstractContainerScreenAccessor;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.util.GhostItemOpacity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class InventoryScreenHelper {
    public static <T extends AbstractContainerMenu> void extractSlotIndex(GuiGraphicsExtractor graphics, AbstractContainerScreen<T> screen){
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
        for (Slot slot : screen.getMenu().slots){
            if (isModifiableSlot(slot)){
                int x = slot.x + accessor.restrictedinventory$getLeftPos() + 8;
                int y = slot.y + accessor.restrictedinventory$getTopPos() + (screen.getFont().lineHeight / 2);
                graphics.centeredText(screen.getFont(), String.valueOf(slot.getContainerSlot()), x, y, -1);
            }
        }
    }

    public static <T extends AbstractContainerMenu> void extractRestrictedSlot(GuiGraphicsExtractor graphics, AbstractContainerScreen<T> screen) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
        Player player = Minecraft.getInstance().player;

        for (Map.Entry<Integer, String> restrictedSlot : CommonConfig.restrictedSlots(player).entrySet()){
            Slot slot = getSlotByInventoryIndex(screen.getMenu().slots, restrictedSlot.getKey());
            if (slot != null && slot.getItem().isEmpty()){
                int x = slot.x + accessor.restrictedinventory$getLeftPos();
                int y = slot.y + accessor.restrictedinventory$getTopPos();

                String value = CommonConfig.restrictedSlots(player).get(slot.getContainerSlot());
                if (value.startsWith("#")){
                    TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(value.substring(1)));
                    HolderSet<Item> items = BuiltInRegistries.ITEM.getOrThrow(tag);

                    int size = items.size();
                    if (size > 0) {
                        int index = (int) ((System.currentTimeMillis() / 1000) % size);
                        Item tagItem = items.get(index).value();
                        GhostItemOpacity.render(0.15f, () -> graphics.fakeItem(tagItem.getDefaultInstance(), x, y));
                    }
                }
                else {
                    Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(value));
                    GhostItemOpacity.render(0.15f, () -> graphics.fakeItem(item.getDefaultInstance(), x, y));
                }
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
