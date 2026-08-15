package com.coolerpromc.restrictedinventory.helper;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.Restriction;
import com.coolerpromc.restrictedinventory.mixin.accessor.AbstractContainerScreenAccessor;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.util.GhostItemOpacity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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

        for (Map.Entry<Integer, Restriction> restrictedSlot : CommonConfig.restrictedSlots(player).entrySet()) {
            Slot slot = getSlotByInventoryIndex(screen.getMenu().slots, restrictedSlot.getKey());
            if (slot != null && slot.getItem().isEmpty()){
                int x = slot.x + accessor.restrictedinventory$getLeftPos();
                int y = slot.y + accessor.restrictedinventory$getTopPos();

                ItemStack stack = restrictedSlot.getValue().displayStack(player.registryAccess());
                if (!stack.isEmpty()) {
                    GhostItemOpacity.render(0.15f, () -> graphics.fakeItem(stack, x, y));
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
