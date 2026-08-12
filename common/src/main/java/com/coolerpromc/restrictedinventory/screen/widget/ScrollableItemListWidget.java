package com.coolerpromc.restrictedinventory.screen.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ScrollableItemListWidget extends AbstractWidget {
    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");
    private static final Identifier SCROLLER_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("widget/scroller_background");
    private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("widget/scroller");
    private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
    private static final int ITEM_SIZE = 14;
    private static final int ITEM_GAP = 0;
    private static final int ITEM_STEP = ITEM_SIZE + ITEM_GAP;
    private static final int SCROLLBAR_WIDTH = 5;
    private static final int TAB_HEIGHT = 12;

    private List<ItemStack> items;
    private List<TagKey<Item>> tags;
    private Mode mode = Mode.ITEMS;
    private double scrollAmount = 0;
    private boolean isDraggingScrollbar = false;
    private double dragScrollbarOffsetY = 0;
    private int selectedIndex = -1;
    private final OnClick select;

    public ScrollableItemListWidget(int x, int y, int width, int height, List<ItemStack> items, List<TagKey<Item>> tags, OnClick select) {
        super(x, y, width, height, Component.empty());
        this.items = items;
        this.tags = tags;
        this.select = select;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
        if (mode == Mode.ITEMS) this.scrollAmount = 0;
    }

    public void setTags(List<TagKey<Item>> tags) {
        this.tags = tags;
        if (mode == Mode.TAGS) this.scrollAmount = 0;
    }

    public String getSelectedString() {
        if (selectedIndex < 0 || selectedIndex >= getCurrentSize()) return null;
        if (mode == Mode.ITEMS) {
            return BuiltInRegistries.ITEM.getKey(items.get(selectedIndex).getItem()).toString();
        } else {
            return "#" + tags.get(selectedIndex).location();
        }
    }

    @Nullable
    public ItemStack getSelectedStack() {
        if (mode != Mode.ITEMS || selectedIndex < 0 || selectedIndex >= getCurrentSize()) return null;
        return items.get(selectedIndex);
    }

    private int getListY() { return getY() + TAB_HEIGHT; }
    private int getListHeight() { return this.height - TAB_HEIGHT; }

    private int getItemsPerRow() {
        return Math.max(1, (this.width - SCROLLBAR_WIDTH - 1) / ITEM_STEP);
    }

    private int getCurrentSize() {
        return mode == Mode.ITEMS ? items.size() : tags.size();
    }

    private int getMaxScroll() {
        int totalRows = (getCurrentSize() + getItemsPerRow() - 1) / getItemsPerRow();
        return Math.max(0, totalRows * ITEM_STEP - getListHeight());
    }

    private int getScrollbarThumbHeight() {
        int totalRows = (getCurrentSize() + getItemsPerRow() - 1) / getItemsPerRow();
        int totalHeight = totalRows * ITEM_STEP;
        if (totalHeight <= getListHeight()) return getListHeight();
        return Math.max(10, (int) ((double) getListHeight() / totalHeight * getListHeight()));
    }

    private int getScrollbarThumbY() {
        int maxScroll = getMaxScroll();
        if (maxScroll == 0) return getListY();
        return getListY() + (int) ((scrollAmount / maxScroll) * (getListHeight() - getScrollbarThumbHeight()));
    }

    private int computeHoveredIndex(int mouseX, int mouseY) {
        int itemsPerRow = getItemsPerRow();
        if (mouseX < getX() || mouseX >= getX() + this.width - SCROLLBAR_WIDTH ||
                mouseY < getListY() || mouseY >= getListY() + getListHeight()) {
            return -1;
        }
        int col = (mouseX - getX()) / ITEM_STEP;
        int row = (int) ((mouseY - getListY() + scrollAmount) / ITEM_STEP);
        int index = row * itemsPerRow + col;
        if (col < 0 || col >= itemsPerRow || index < 0 || index >= getCurrentSize()) return -1;

        int cellX = getX() + col * ITEM_STEP;
        int cellY = (int) (getListY() + row * ITEM_STEP - scrollAmount);
        if (mouseX < cellX || mouseX >= cellX + ITEM_SIZE || mouseY < cellY || mouseY >= cellY + ITEM_SIZE) return -1;
        return index;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        renderTabs(graphics);
        renderList(graphics, mouseX, mouseY);
    }

    private void renderTabs(GuiGraphicsExtractor graphics) {
        var font = Minecraft.getInstance().font;
        int tabWidth = this.width / 2;

        int itemsColor = mode == Mode.ITEMS ? 0xFF606060 : 0xFF383838;
        graphics.fill(getX(), getY(), getX() + tabWidth - 1, getY() + TAB_HEIGHT - 1, itemsColor);
        graphics.centeredText(font, "Items", getX() + tabWidth / 2, getY() + 2, 0xFFFFFFFF);

        int tagsColor = mode == Mode.TAGS ? 0xFF606060 : 0xFF383838;
        graphics.fill(getX() + tabWidth, getY(), getX() + this.width, getY() + TAB_HEIGHT - 1, tagsColor);
        graphics.centeredText(font, "Tags", getX() + tabWidth + (this.width - tabWidth) / 2, getY() + 2, 0xFFFFFFFF);

        graphics.fill(getX(), getY() + TAB_HEIGHT - 1, getX() + this.width, getY() + TAB_HEIGHT, 0xFF222222);
    }

    private void renderList(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (getCurrentSize() == 0) return;

        int maxScroll = getMaxScroll();
        this.scrollAmount = Math.clamp(this.scrollAmount, 0, maxScroll);
        int itemsPerRow = getItemsPerRow();
        int hoveredIndex = computeHoveredIndex(mouseX, mouseY);

        graphics.enableScissor(getX(), getListY(), getX() + this.width, getListY() + getListHeight());

        for (int i = 0; i < getCurrentSize(); i++) {
            int row = i / itemsPerRow;
            int col = i % itemsPerRow;
            int drawX = getX() + col * ITEM_STEP;
            int drawY = (int) (getListY() + row * ITEM_STEP - scrollAmount);

            if (drawY + ITEM_SIZE < getListY() || drawY > getListY() + getListHeight()) continue;

            ItemStack renderStack = resolveStack(i);
            if (renderStack == null) continue;

            boolean hovered = i == hoveredIndex;

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, drawX, drawY, ITEM_SIZE, ITEM_SIZE);

            if (hovered) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, drawX - 3, drawY - 3, 20, 20);
            }

            graphics.pose().pushMatrix();
            graphics.pose().translate(drawX + 1, drawY + 1);
            graphics.pose().scale(0.75f, 0.75f);
            graphics.fakeItem(renderStack, 0, 0);
            graphics.pose().popMatrix();

            if (hovered) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, drawX - 3, drawY - 3, 20, 20);
            }
        }

        graphics.disableScissor();

        int scrollbarX = getX() + this.width - SCROLLBAR_WIDTH;
        int thumbHeight = getScrollbarThumbHeight();
        int thumbY = getScrollbarThumbY();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND_SPRITE, scrollbarX, getListY(), SCROLLBAR_WIDTH, getListHeight());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, scrollbarX, thumbY, SCROLLBAR_WIDTH, thumbHeight);

        if (hoveredIndex >= 0) {
            var font = Minecraft.getInstance().font;
            graphics.setComponentTooltipForNextFrame(font, buildTooltip(hoveredIndex), mouseX, mouseY);
        }
    }

    private List<Component> buildTooltip(int index) {
        if (mode != Mode.ITEMS) {
            return List.of(Component.literal("#" + tags.get(index).location()));
        }

        ItemStack stack = items.get(index);
        List<Component> lines = new ArrayList<>(stack.getTooltipLines(Item.TooltipContext.of(Minecraft.getInstance().level), Minecraft.getInstance().player, TooltipFlag.Default.NORMAL));
        lines.add(Component.literal(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
        return lines;
    }

    @Nullable
    private ItemStack resolveStack(int index) {
        if (mode == Mode.ITEMS) {
            return items.get(index);
        }
        HolderSet<Item> members = BuiltInRegistries.ITEM.getOrThrow(tags.get(index));
        if (members.size() == 0) return null;
        int cycleIndex = (int) ((System.currentTimeMillis() / 1000) % members.size());
        return members.get(cycleIndex).value().getDefaultInstance();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;

        if (event.y() < getListY()) {
            Mode newMode = event.x() < getX() + this.width / 2f ? Mode.ITEMS : Mode.TAGS;
            if (newMode != mode) {
                mode = newMode;
                scrollAmount = 0;
                selectedIndex = -1;
            }
            return true;
        }

        int maxScroll = getMaxScroll();
        if (maxScroll > 0 && event.x() >= getX() + this.width - SCROLLBAR_WIDTH) {
            int thumbY = getScrollbarThumbY();
            int thumbHeight = getScrollbarThumbHeight();
            if (event.y() >= thumbY && event.y() <= thumbY + thumbHeight) {
                isDraggingScrollbar = true;
                dragScrollbarOffsetY = event.y() - thumbY;
                return true;
            }
        }

        int clicked = computeHoveredIndex((int) event.x(), (int) event.y());
        if (clicked >= 0) {
            selectedIndex = clicked;
            this.select.select(this);
            playButtonClickSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (isDraggingScrollbar) {
            int maxScroll = getMaxScroll();
            int thumbHeight = getScrollbarThumbHeight();
            double relativeY = event.y() - getListY() - dragScrollbarOffsetY;
            this.scrollAmount = Math.max(0, Math.min((relativeY / (getListHeight() - thumbHeight)) * maxScroll, maxScroll));
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (isDraggingScrollbar) {
            isDraggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isMouseOver(mouseX, mouseY)) {
            this.scrollAmount -= scrollY * ITEM_STEP * 3;
            return true;
        }
        return false;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {}

    public interface OnClick{
        void select(ScrollableItemListWidget widget);
    }

    public enum Mode {
        ITEMS,
        TAGS
    }
}
