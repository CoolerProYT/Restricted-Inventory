package com.coolerpromc.restrictedinventory.mixin;

import com.coolerpromc.restrictedinventory.util.GuiItemOpacityAccess;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @ModifyExpressionValue(method = "submitBlitFromItemAtlas", at = @At(value = "NEW", target = "Lnet/minecraft/client/renderer/state/gui/BlitRenderState;"))
    private BlitRenderState restrictedinventory$applyItemOpacity(BlitRenderState original, @Local(argsOnly = true) GuiItemRenderState itemState) {
        float opacity = ((GuiItemOpacityAccess) (Object) itemState).restrictedinventory$getOpacity();

        if (opacity >= 1.0F) {
            return original;
        }

        return new BlitRenderState(original.pipeline(), original.textureSetup(), original.pose(), original.x0(), original.y0(), original.x1(), original.y1(), original.u0(), original.u1(), original.v0(), original.v1(), restrictedinventory$multiplyPremultiplied(original.color(), opacity), original.scissorArea(), original.bounds());
    }

    @Unique
    private static int restrictedinventory$multiplyPremultiplied(int color, float opacity) {
        int a = scale(color >>> 24, opacity);
        int r = scale((color >>> 16) & 0xFF, opacity);
        int g = scale((color >>> 8) & 0xFF, opacity);
        int b = scale(color & 0xFF, opacity);

        return a << 24 | r << 16 | g << 8 | b;
    }

    private static int scale(int component, float opacity) {
        return Math.round(component * opacity);
    }
}