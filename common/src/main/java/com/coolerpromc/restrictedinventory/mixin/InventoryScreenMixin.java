package com.coolerpromc.restrictedinventory.mixin;

import com.coolerpromc.restrictedinventory.config.ClientConfig;
import com.coolerpromc.restrictedinventory.helper.InventoryScreenHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class InventoryScreenMixin<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {
    @Unique
    private boolean restrictedInventory$isTabDown = false;

    protected InventoryScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("RETURN"))
    public void extractContents(GuiGraphics graphics, int mouseX, int mouseY, float a, CallbackInfo ci){
        InventoryScreenHelper.extractRestrictedSlot(graphics, (AbstractContainerScreen<T>)(Object)this);
        if (restrictedInventory$isTabDown && ClientConfig.showSlotIndex()){
            InventoryScreenHelper.extractSlotIndex(graphics, (AbstractContainerScreen<T>)(Object)this);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode == InputConstants.KEY_TAB){
            this.restrictedInventory$isTabDown = true;
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_TAB){
            this.restrictedInventory$isTabDown = false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
