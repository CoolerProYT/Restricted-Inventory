package com.coolerpromc.restrictedinventory.screen;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.ClientConfig;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.GroupEntry;
import com.coolerpromc.restrictedinventory.config.util.ItemEntry;
import com.coolerpromc.restrictedinventory.config.util.Restriction;
import com.coolerpromc.restrictedinventory.config.util.RestrictionGroups;
import com.coolerpromc.restrictedinventory.network.ServerBoundRestrictionUpdatePacket;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.screen.widget.ScrollableItemListWidget;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class RestrictionConfigScreen extends Screen {
    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");
    public static final Identifier TEXTURE = Constants.id("textures/gui/restriction_config.png");

    private final Map<Integer, Restriction> restrictions = new HashMap<>(
            CommonConfig.clientCache.useClientRestriction()
                    ? ClientConfig.getRestrictedSlots()
                    : CommonConfig.clientCache.restrictedSlots()
    );
    private final Map<Integer, RestrictionSlot> slotByIndex = new HashMap<>();
    private final List<TagKey<Item>> itemTags = new ArrayList<>();
    private final List<ItemChoice> choices = new ArrayList<>();
    private final int bgWidth = 176;
    private final int bgHeight = 214;
    private int selectedSlot = -1;
    private String query = "";
    private int x;
    private int y;

    private ScrollableItemListWidget itemListWidget;
    private EditBox searchInput;

    public RestrictionConfigScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - bgWidth) / 2;
        this.y = (this.height - bgHeight) / 2;

        if (this.choices.isEmpty()) {
            this.itemTags.addAll(getAllItemTags());
            this.choices.addAll(collectChoices());
        }

        this.initSlots();

        List<ItemStack> displayItems = this.choices.stream().map(ItemChoice::stack).toList();
        List<TagKey<Item>> displayTags = this.itemTags.stream().filter(key -> BuiltInRegistries.ITEM.getOrThrow(key).size() > 0).toList();
        this.itemListWidget = addRenderableWidget(new ScrollableItemListWidget(x + 8, y + 24 + 8, bgWidth - 16, 70, displayItems, displayTags, this::onItemSelected));

        int buttonWidth = this.bgWidth / 3;
        SpriteIconButton saveButton = SpriteIconButton.builder(Component.literal("Save"), this::onSave, false).width(buttonWidth).sprite(Constants.id("icon/save"), 18, 18).build();
        SpriteIconButton cancelButton = SpriteIconButton.builder(Component.literal("Cancel"), this::onCancel, false).width(buttonWidth).sprite(Constants.id("icon/cancel"), 18, 18).build();

        cancelButton.setX(x + buttonWidth / 3);
        cancelButton.setY(y + 84 + 58 + 18 + 24 + 4);

        saveButton.setX((x + bgWidth - buttonWidth) - buttonWidth / 3);
        saveButton.setY(y + 84 + 58 + 18 + 24 + 4);

        searchInput = new EditBox(this.font, x + 8, y + 8, bgWidth - 16, 16, Component.literal("Search"));
        searchInput.setResponder(this::onSearch);
        searchInput.setHint(Component.literal("Search..."));
        searchInput.setValue(this.query);

        addRenderableWidget(searchInput);
        addRenderableWidget(saveButton);
        addRenderableWidget(cancelButton);
    }

    private void onSearch(String s) {
        this.query = s;
        String query = s.toLowerCase().trim();

        if (query.startsWith("@")) {
            String rest = query.substring(1);
            int spaceIdx = rest.indexOf(' ');
            String nsPrefix = spaceIdx >= 0 ? rest.substring(0, spaceIdx) : rest;
            String nameQuery = spaceIdx >= 0 ? rest.substring(spaceIdx + 1).trim() : "";

            List<ItemStack> displayItems = this.choices.stream().filter(choice -> choice.namespace().startsWith(nsPrefix)).filter(choice -> nameQuery.isEmpty() || wordStartMatch(choice.searchText(), nameQuery)).map(ItemChoice::stack).toList();
            List<TagKey<Item>> displayTags = this.itemTags.stream().filter(key -> BuiltInRegistries.ITEM.get(key).map(h -> h.size() > 0).orElse(false)).filter(key -> tagNamespaceMatch(key, nsPrefix, nameQuery)).toList();

            itemListWidget.setItems(displayItems);
            itemListWidget.setTags(displayTags);
            return;
        }

        List<ItemStack> displayItems = this.choices.stream().filter(choice -> wordStartMatch(choice.searchText(), query)).map(ItemChoice::stack).toList();
        List<TagKey<Item>> displayTags = this.itemTags.stream().filter(key -> BuiltInRegistries.ITEM.get(key).map(h -> h.size() > 0).orElse(false)).filter(key -> tagMatch(key, query)).toList();

        itemListWidget.setItems(displayItems);
        itemListWidget.setTags(displayTags);
    }

    private boolean tagNamespaceMatch(TagKey<Item> key, String nsPrefix, String nameQuery) {
        if (!key.location().getNamespace().startsWith(nsPrefix)) return false;
        if (nameQuery.isEmpty()) return true;
        String path = key.location().getPath().toLowerCase().replace("/", " ").replace("_", " ");
        return wordStartMatch(path, nameQuery);
    }

    private boolean tagMatch(TagKey<Item> key, String query) {
        String path = key.location().getPath().toLowerCase().replace("/", " ").replace("_", " ");
        String namespace = key.location().getNamespace().toLowerCase();
        String full = (namespace + " " + path).trim();
        String q = query.toLowerCase().replace(":", " ").replace("/", " ").replace("_", " ");

        return wordStartMatch(full, q);
    }

    private boolean wordStartMatch(String text, String query) {
        String[] queryWords = query.toLowerCase().split("\\s+");
        String[] textWords = text.toLowerCase().split("\\s+");
        for (String qw : queryWords) {
            if (Arrays.stream(textWords).noneMatch(tw -> tw.startsWith(qw))) return false;
        }
        return true;
    }

    private void onItemSelected(ScrollableItemListWidget widget) {
        if (this.selectedSlot == -1) return;

        String selected = widget.getSelectedString();
        if (selected == null) return;

        // a creative stack carries the components that make it distinct (a book's enchantments, a
        // potion's effect), and those are exactly the filter the restriction should use
        ItemStack stack = widget.getSelectedStack();
        ItemEntry entry = new ItemEntry(selected, stack == null ? Optional.empty() : ItemEntry.componentsOf(stack, Minecraft.getInstance().level.registryAccess()));

        this.restrictions.put(this.selectedSlot, entry);
        this.slotByIndex.put(this.selectedSlot, this.slotByIndex.get(this.selectedSlot).withEntry(entry));
    }

    private static List<ItemChoice> collectChoices() {
        Set<ItemStack> stacks = ItemStackLinkedSet.createTypeAndComponentsSet();

        buildCreativeTabs();
        for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            if (tab.getType() != CreativeModeTab.Type.CATEGORY) continue;
            try {
                stacks.addAll(tab.getDisplayItems());
            } catch (Exception e) {
                Constants.LOGGER.warn("Could not read creative tab contents", e);
            }
        }

        Set<Item> covered = stacks.stream().map(ItemStack::getItem).collect(Collectors.toSet());
        BuiltInRegistries.ITEM.stream().filter(item -> item != Items.AIR).filter(item -> !covered.contains(item)).forEach(item -> stacks.add(new ItemStack(item)));

        return stacks.stream().map(RestrictionConfigScreen::toChoice).toList();
    }

    private static void buildCreativeTabs() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) return;

        boolean hasPermissions = minecraft.player.canUseGameMasterBlocks() && minecraft.options.operatorItemsTab().get();
        try {
            CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(), hasPermissions, level.registryAccess());
        } catch (Exception e) {
            Constants.LOGGER.warn("Could not build creative tab contents", e);
        }
    }

    private static ItemChoice toChoice(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        StringBuilder searchText = new StringBuilder(stack.getHoverName().getString());
        searchText.append(' ').append(id.getPath().replace("_", " "));

        // component variants share a display name, so their tooltip is the only thing that tells
        // them apart -- "sharpness" has to find the right enchanted book
        if (!stack.getComponentsPatch().isEmpty()) {
            for (Component line : stack.getTooltipLines(Item.TooltipContext.of(Minecraft.getInstance().level), Minecraft.getInstance().player, TooltipFlag.Default.NORMAL)) {
                searchText.append(' ').append(line.getString());
            }
        }

        return new ItemChoice(stack, searchText.toString().toLowerCase(Locale.ROOT), id.getNamespace());
    }

    private record ItemChoice(ItemStack stack, String searchText, String namespace) {
    }

    private void clearSlot() {
        if (this.selectedSlot != -1) {
            this.restrictions.remove(this.selectedSlot);
            this.slotByIndex.put(this.selectedSlot, this.slotByIndex.get(this.selectedSlot).withEntry(null));
        }
    }

    private void onCancel(Button button) {
        this.onClose();
    }

    private void onSave(Button button) {
        Map<String, Restriction> newRestriction = this.restrictions.entrySet().stream().filter(e -> e.getValue() != null).collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue));

        if (CommonConfig.clientCache.useClientRestriction()) {
            ClientConfig.RESTRICTED_SLOTS.set(newRestriction);
            ClientConfig.CONFIG.save();
        } else {
            Services.NETWORK.sendToServer(new ServerBoundRestrictionUpdatePacket(newRestriction));
        }

        this.onClose();
    }

    private void initSlots() {
        int left = 8;
        int top = 84 + 24;

        for (int i = 0; i < 9; i++) {
            slotByIndex.put(i, new RestrictionSlot(left + x + (i * 18), y + top + 58, restrictions.get(i)));
        }

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 9; i++) {
                int index = i + (j + 1) * 9;
                slotByIndex.put(index, new RestrictionSlot(left + x + (i * 18), y + top + (j * 18), restrictions.get(index)));
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, bgWidth, bgHeight, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        this.extractRestrictionSlots(graphics, mouseX, mouseY);
        this.extractTooltip(graphics, mouseX, mouseY);
    }

    private void extractRestrictionSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY){
        for (Map.Entry<Integer, RestrictionSlot> entry : slotByIndex.entrySet()){
            int index = entry.getKey();
            String str = String.valueOf(index);
            RestrictionSlot slot = entry.getValue();

            if (slot.isHovering(mouseX, mouseY) && index != this.selectedSlot){
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, slot.x - 4, slot.y - 4, 24, 24);
            }
            if (index == this.selectedSlot) {
                int fillX = slot.x;
                int fillY = slot.y;
                graphics.fill(fillX, fillY, fillX + 16, fillY + 16, 0xAAFFFFFF);
            }
            graphics.text(this.font, str, slot.x() + (18 - this.font.width(str)) / 2, slot.y() + (this.font.lineHeight / 2), 0xFFFFFFFF, false);
            List<ItemStack> stacks = slot.stacks;
            int size = stacks.size();
            if (size > 0) {
                int itemIndex = (int) ((System.currentTimeMillis() / 1000) % size);
                graphics.fakeItem(stacks.get(itemIndex), slot.x, slot.y);
            }
            if (slot.isHovering(mouseX, mouseY) && index != this.selectedSlot){
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, slot.x - 4, slot.y - 4, 24, 24);
            }
        }
    }

    private void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for (RestrictionSlot slot : slotByIndex.values()){
            if (slot.isHovering(mouseX, mouseY) && slot.entry != null){
                graphics.setComponentTooltipForNextFrame(this.font, describe(slot.entry), mouseX, mouseY);
            }
        }
    }

    private static List<Component> describe(Restriction restriction) {
        List<Component> lines = new ArrayList<>();

        if (restriction instanceof GroupEntry group) {
            lines.add(Component.literal(group.group().toString()));
            RestrictionGroups.get(group.group()).ifPresentOrElse(
                value -> lines.add(Component.literal(value.entries().size() + " entries").withStyle(ChatFormatting.DARK_GRAY)),
                () -> lines.add(Component.literal("Unknown group").withStyle(ChatFormatting.RED))
            );
            return lines;
        }

        ItemEntry entry = (ItemEntry) restriction;
        lines.add(Component.literal(entry.item()));
        entry.components().ifPresent(tag -> {
            for (String line : new SnbtPrinterTagVisitor().visit(tag).split("\n")) {
                lines.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY));
            }
        });
        entry.display().ifPresent(display -> lines.add(Component.literal("Display: " + display.item()).withStyle(ChatFormatting.DARK_GRAY)));
        lines.add(Component.literal("Middle click to edit components").withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0){
            for (Map.Entry<Integer, RestrictionSlot> entry : slotByIndex.entrySet()){
                RestrictionSlot slot = entry.getValue();
                if (slot.isHovering(event.x(), event.y())){
                    this.selectedSlot = entry.getKey();
                    return true;
                }
            }
        }
        else if (event.button() == 1){
            for (Map.Entry<Integer, RestrictionSlot> entry : slotByIndex.entrySet()){
                RestrictionSlot slot = entry.getValue();
                if (slot.isHovering(event.x(), event.y())){
                    this.selectedSlot = entry.getKey();
                    this.clearSlot();
                    return true;
                }
            }
        }
        else if (event.button() == 2){
            for (Map.Entry<Integer, RestrictionSlot> entry : slotByIndex.entrySet()){
                RestrictionSlot slot = entry.getValue();
                if (slot.isHovering(event.x(), event.y()) && slot.entry() instanceof ItemEntry itemEntry){
                    this.selectedSlot = entry.getKey();
                    this.minecraft.setScreen(new NbtEditScreen(this, entry.getKey(), itemEntry));
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    public void applyComponents(int slot, Optional<CompoundTag> components) {
        if (!(this.restrictions.get(slot) instanceof ItemEntry current)) return;

        ItemEntry updated = new ItemEntry(current.item(), components, current.display());
        this.restrictions.put(slot, updated);
        this.slotByIndex.put(slot, this.slotByIndex.get(slot).withEntry(updated));
    }

    private static List<ItemStack> stacksOf(@Nullable Restriction restriction) {
        if (restriction == null) return List.of();

        return restriction.displayStacks(Minecraft.getInstance().level.registryAccess());
    }

    private List<TagKey<Item>> getAllItemTags() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return List.of();

        RegistryAccess registryAccess = level.registryAccess();
        HolderLookup.RegistryLookup<Item> itemLookup = registryAccess.lookupOrThrow(Registries.ITEM);

        return itemLookup.listTagIds().toList();
    }

    public record RestrictionSlot(int x, int y, List<ItemStack> stacks, @Nullable Restriction entry) {
        public RestrictionSlot(int x, int y, @Nullable Restriction entry) {
            this(x, y, stacksOf(entry), entry);
        }

        public boolean isHovering(double mouseX, double mouseY) {
            return mouseX >= x - 1 && mouseX <= x + 16 && mouseY >= y - 1 && mouseY <= y + 16;
        }

        public RestrictionSlot withEntry(@Nullable Restriction entry) {
            return new RestrictionSlot(x, y, entry);
        }
    }
}
