package com.coolerpromc.restrictedinventory.helper;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class GhostItemRenderer {
    public static final float MIN_ALPHA = 0.15f;

    private GhostItemRenderer() {
    }

    public static void render(GuiGraphics graphics, ItemStack stack, int x, int y, float alpha) {
        if (stack.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, minecraft.level, null, 0);

        float ghostAlpha = Math.max(alpha, MIN_ALPHA);
        MultiBufferSource.BufferSource source = graphics.bufferSource();
        MultiBufferSource ghostSource = type -> isItemSheet(type)
            ? new AlphaVertexConsumer(source.getBuffer(ghostSheet()), ghostAlpha)
            : source.getBuffer(type);

        graphics.flush();

        graphics.pose().pushPose();
        graphics.pose().translate(x + 8.0F, y + 8.0F, 150.0F);
        graphics.pose().scale(16.0F, -16.0F, 16.0F);

        boolean flatLighting = !model.usesBlockLight();
        if (flatLighting) {
            Lighting.setupForFlatItems();
        }

        try {
            itemRenderer.render(stack, ItemDisplayContext.GUI, false, graphics.pose(), ghostSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
            graphics.flush();
        } finally {
            if (flatLighting) {
                Lighting.setupFor3DItems();
            }
            graphics.pose().popPose();
        }
    }

    private static RenderType ghostSheet() {
        return RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS);
    }

    private static boolean isItemSheet(RenderType type) {
        return type == Sheets.translucentItemSheet() || type == Sheets.cutoutBlockSheet() || type == Sheets.translucentCullBlockSheet();
    }

    private record AlphaVertexConsumer(VertexConsumer delegate, float alpha) implements VertexConsumer {
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            delegate.color(r, g, b, (int) (a * alpha));
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            delegate.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            delegate.defaultColor(r, g, b, (int) (a * alpha));
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }
}
