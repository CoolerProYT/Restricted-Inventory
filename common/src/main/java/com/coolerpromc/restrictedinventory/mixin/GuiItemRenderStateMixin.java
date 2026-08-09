package com.coolerpromc.restrictedinventory.mixin;

import com.coolerpromc.restrictedinventory.util.GhostItemOpacity;
import com.coolerpromc.restrictedinventory.util.GuiItemOpacityAccess;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiItemRenderState.class)
public abstract class GuiItemRenderStateMixin implements GuiItemOpacityAccess {
    @Unique
    private float restrictedinventory$opacity = 1.0F;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void restrictedinventory$captureOpacity(CallbackInfo ci) {
        restrictedinventory$opacity = GhostItemOpacity.current();
    }

    @Override
    public float restrictedinventory$getOpacity() {
        return restrictedinventory$opacity;
    }
}