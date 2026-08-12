package com.coolerpromc.restrictedinventory.screen.widget;

import com.coolerpromc.restrictedinventory.mixin.accessor.MultiLineEditBoxAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NbtEditBox extends MultiLineEditBox {
    public NbtEditBox(Font font, int x, int y, int width, int height, Component placeholder, Component message) {
        super(font, x, y, width, height, placeholder, message);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.visible && button == 0 && withinContentAreaPoint(mouseX, mouseY)) {
            MultilineTextField textField = ((MultiLineEditBoxAccessor) this).getTextField();
            textField.setSelecting(Screen.hasShiftDown());
            textField.seekCursorToPoint(mouseX - this.getX() - this.innerPadding(), mouseY - this.getY() - this.innerPadding() + this.scrollAmount());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
