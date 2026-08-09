package com.coolerpromc.restrictedinventory.helper;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.mixin.accessor.AbstractContainerScreenAccessor;
import com.coolerpromc.restrictedinventory.mixin.accessor.ScreenAccessor;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
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
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
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

                String value = CommonConfig.restrictedSlots(player).get(slot.getContainerSlot());
                if (value.startsWith("#")){
                    TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(value.substring(1)));
                    HolderSet<Item> items = BuiltInRegistries.ITEM.getOrCreateTag(tag);

                    int size = items.size();
                    if (size > 0) {
                        int index = (int) ((System.currentTimeMillis() / 1000) % size);
                        Item tagItem = items.get(index).value();
                        renderTransparentItem(graphics, tagItem.getDefaultInstance(), x, y, 0.07f);
                    }
                }
                else {
                    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(value));
                    renderTransparentItem(graphics, item.getDefaultInstance(), x, y, 0.07f);
                }
            }
        }
    }

    private static void renderTransparentItem(GuiGraphics graphics, ItemStack stack, int x, int y, float opacity) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();

        BakedModel model = itemRenderer.getModel(stack, minecraft.level, null, 0);

        RenderType translucentType = RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);

        MultiBufferSource translucentBuffer = ignored -> graphics.bufferSource().getBuffer(translucentType);

        boolean flatLighting = !model.usesBlockLight();

        graphics.flush();
        graphics.pose().pushPose();

        try {
            graphics.pose().translate(x + 8.0F, y + 8.0F, 150.0F);
            graphics.pose().scale(16.0F, -16.0F, 16.0F);

            if (flatLighting) {
                Lighting.setupForFlatItems();
            }

            graphics.setColor(1.0F, 1.0F, 1.0F, opacity);

            itemRenderer.render(stack, ItemDisplayContext.GUI, false, graphics.pose(), translucentBuffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);

            graphics.flush();
        } finally {
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

            if (flatLighting) {
                Lighting.setupFor3DItems();
            }

            graphics.pose().popPose();
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
