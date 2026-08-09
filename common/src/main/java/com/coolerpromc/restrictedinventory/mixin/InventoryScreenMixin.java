package com.coolerpromc.restrictedinventory.mixin;

import com.coolerpromc.restrictedinventory.config.ClientConfig;
import com.coolerpromc.restrictedinventory.helper.InventoryScreenHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.input.KeyEvent;
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

    @Inject(method = "extractContents(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("RETURN"))
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci){
        InventoryScreenHelper.extractRestrictedSlot(graphics, (AbstractContainerScreen<T>)(Object)this);
        if (restrictedInventory$isTabDown && ClientConfig.showSlotIndex()){
            InventoryScreenHelper.extractSlotIndex(graphics, (AbstractContainerScreen<T>)(Object)this);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (event.key() == InputConstants.KEY_TAB){
            this.restrictedInventory$isTabDown = true;
        }
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (event.key() == InputConstants.KEY_TAB){
            this.restrictedInventory$isTabDown = false;
        }
        return super.keyReleased(event);
    }
}
