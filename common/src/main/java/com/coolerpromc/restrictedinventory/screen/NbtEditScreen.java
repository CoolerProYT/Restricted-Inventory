package com.coolerpromc.restrictedinventory.screen;

import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import com.coolerpromc.restrictedinventory.screen.widget.NbtEditBox;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public class NbtEditScreen extends Screen {
    private static final int EDITOR_WIDTH = 220;
    private static final int EDITOR_HEIGHT = 90;

    private final RestrictionConfigScreen parent;
    private final int slot;
    private final ItemEntry entry;
    private final @Nullable Item previewItem;

    private MultiLineEditBox editor;
    private Button saveButton;
    private ItemStack preview;
    private @Nullable Component error;
    private String text;

    public NbtEditScreen(RestrictionConfigScreen parent, int slot, ItemEntry entry) {
        super(Component.literal("Edit NBT"));
        this.parent = parent;
        this.slot = slot;
        this.entry = entry;
        this.previewItem = entry.items().stream().findFirst().orElse(null);
        this.preview = previewItem == null ? ItemStack.EMPTY : entry.display(previewItem);
        this.text = entry.tag().map(tag -> new SnbtPrinterTagVisitor().visit(tag)).orElse("");
    }

    @Override
    protected void init() {
        super.init();

        int editorX = (this.width - EDITOR_WIDTH) / 2;
        int editorY = 60;

        this.editor = addRenderableWidget(new NbtEditBox(this.font, editorX, editorY, EDITOR_WIDTH, EDITOR_HEIGHT, Component.literal("{}"), Component.literal("NBT")));
        // init runs again on resize, so the text lives in a field rather than only in the widget
        this.editor.setValue(this.text);
        this.editor.setValueListener(edited -> {
            this.text = edited;
            onEdited(edited);
        });

        this.saveButton = addRenderableWidget(Button.builder(Component.literal("Save"), b -> onSave()).bounds(editorX + EDITOR_WIDTH - 100, editorY + EDITOR_HEIGHT + 24, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose()).bounds(editorX, editorY + EDITOR_HEIGHT + 24, 100, 20).build());

        setInitialFocus(this.editor);
        onEdited(this.text);
    }

    private void onEdited(String text) {
        Optional<CompoundTag> parsed = parse(text);

        this.saveButton.active = this.error == null;
        this.preview = previewItem == null ? ItemStack.EMPTY : new ItemEntry(entry.item(), parsed).display(previewItem);
    }

    private Optional<CompoundTag> parse(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            this.error = null;
            return Optional.empty();
        }

        try {
            CompoundTag tag = TagParser.parseTag(trimmed);
            this.error = null;
            return tag.isEmpty() ? Optional.empty() : Optional.of(tag);
        } catch (CommandSyntaxException e) {
            this.error = Component.literal(e.getMessage()).withStyle(ChatFormatting.RED);
            return Optional.empty();
        }
    }

    private void onSave() {
        Optional<CompoundTag> tag = parse(this.text);
        if (this.error != null) return;

        parent.applyNbt(slot, tag);
        onClose();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, "Slot " + slot + " NBT filter", this.width / 2, 20, 0xFFFFFFFF);

        Component name = preview.isEmpty() ? Component.literal(entry.item()) : preview.getHoverName();
        int nameWidth = this.font.width(name);
        int nameX = (this.width - nameWidth + 20) / 2;
        graphics.drawString(this.font, name, nameX, 40, 0xFFAAAAAA, false);
        if (!preview.isEmpty()) {
            graphics.renderFakeItem(preview, nameX - 20, 36);
        }

        if (this.error != null) {
            int errorY = 60 + EDITOR_HEIGHT + 4;
            for (FormattedCharSequence line : this.font.split(this.error, EDITOR_WIDTH)) {
                graphics.drawCenteredString(this.font, line, this.width / 2, errorY, 0xFFFF5555);
                errorY += this.font.lineHeight;
            }
        }
    }
}
