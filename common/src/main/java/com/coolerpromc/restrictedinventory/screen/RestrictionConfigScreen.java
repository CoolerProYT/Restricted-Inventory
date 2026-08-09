package com.coolerpromc.restrictedinventory.screen;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.ClientConfig;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.network.ServerBoundRestrictionUpdatePacket;
import com.coolerpromc.restrictedinventory.platform.Services;
import com.coolerpromc.restrictedinventory.screen.widget.ScrollableItemListWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.stream.Collectors;

public class RestrictionConfigScreen extends Screen {
    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");
    public static final Identifier TEXTURE = Constants.id("textures/gui/restriction_config.png");

    private final Map<Integer, String> restrictions = new HashMap<>(
            CommonConfig.clientCache.useClientRestriction()
                    ? ClientConfig.getRestrictedSlots()
                    : CommonConfig.clientCache.restrictedSlots()
    );
    private final Map<Integer, RestrictionSlot> slotByIndex = new HashMap<>();
    private final List<TagKey<Item>> itemTags = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final int bgWidth = 176;
    private final int bgHeight = 214;
    private int selectedSlot = -1;
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
        this.itemTags.addAll(getAllItemTags());
        this.items.addAll(BuiltInRegistries.ITEM.stream().toList());

        this.initSlots();

        List<Item> displayItems = this.items.stream().filter(item -> item != Items.AIR).toList();
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

        addRenderableWidget(searchInput);
        addRenderableWidget(saveButton);
        addRenderableWidget(cancelButton);
    }

    private void onSearch(String s) {
        String query = s.toLowerCase().trim();

        if (query.startsWith("@")) {
            String rest = query.substring(1);
            int spaceIdx = rest.indexOf(' ');
            String nsPrefix = spaceIdx >= 0 ? rest.substring(0, spaceIdx) : rest;
            String nameQuery = spaceIdx >= 0 ? rest.substring(spaceIdx + 1).trim() : "";

            List<Item> displayItems = this.items.stream().filter(item -> item != Items.AIR).filter(item -> itemNamespaceMatch(item, nsPrefix, nameQuery)).toList();
            List<TagKey<Item>> displayTags = this.itemTags.stream().filter(key -> BuiltInRegistries.ITEM.get(key).map(h -> h.size() > 0).orElse(false)).filter(key -> tagNamespaceMatch(key, nsPrefix, nameQuery)).toList();

            itemListWidget.setItems(displayItems);
            itemListWidget.setTags(displayTags);
            return;
        }

        List<Item> displayItems = this.items.stream().filter(item -> item != Items.AIR).filter(item -> wordStartMatch(I18n.get(item.getDescriptionId()), query)).toList();
        List<TagKey<Item>> displayTags = this.itemTags.stream().filter(key -> BuiltInRegistries.ITEM.get(key).map(h -> h.size() > 0).orElse(false)).filter(key -> tagMatch(key, query)).toList();

        itemListWidget.setItems(displayItems);
        itemListWidget.setTags(displayTags);
    }

    private boolean itemNamespaceMatch(Item item, String nsPrefix, String nameQuery) {
        String id = BuiltInRegistries.ITEM.getKey(item).getPath().replace("_", " ");
        String ns = BuiltInRegistries.ITEM.getResourceKey(item).map(k -> k.identifier().getNamespace()).orElse("");
        if (!ns.startsWith(nsPrefix)) return false;
        if (nameQuery.isEmpty()) return true;
        return wordStartMatch(I18n.get(item.getDescriptionId()), nameQuery) || wordStartMatch(id, nameQuery);
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
        if (this.selectedSlot != -1){
            this.restrictions.put(this.selectedSlot, widget.getSelectedString());
            this.slotByIndex.put(this.selectedSlot, this.slotByIndex.get(this.selectedSlot).withNewValue(widget.getSelectedString()));
        }
    }

    private void clearSlot() {
        if (this.selectedSlot != -1){
            this.restrictions.remove(this.selectedSlot);
            this.slotByIndex.put(this.selectedSlot, this.slotByIndex.get(this.selectedSlot).withNewValue(null));
        }
    }

    private void onCancel(Button button) {
        this.onClose();
    }

    private void onSave(Button button) {
        Map<String, String> newRestriction = this.restrictions.entrySet().stream().map(e -> Map.entry(String.valueOf(e.getKey()), e.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (CommonConfig.clientCache.useClientRestriction()){
            ClientConfig.RESTRICTED_SLOTS.set(newRestriction);
            ClientConfig.CONFIG.save();
        }
        else{
            Services.NETWORK.sendToServer(new ServerBoundRestrictionUpdatePacket(newRestriction));
        }

        this.onClose();
    }

    private void initSlots(){
        int left = 8;
        int top = 84 + 24;

        for (int i = 0; i < 9; i++){
            slotByIndex.put(i, new RestrictionSlot(left + x + (i * 18), y + top + 58, itemsFromString(restrictions.getOrDefault(i, "minecraft:air")), restrictions.get(i)));
        }

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 9; i++) {
                int index = i + (j + 1) * 9;
                slotByIndex.put(index, new RestrictionSlot(left + x + (i * 18), y + top + (j * 18), itemsFromString(restrictions.getOrDefault(index, "minecraft:air")), restrictions.get(index)));
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
            if (index == this.selectedSlot){
                int fillX = slot.x;
                int fillY = slot.y;
                graphics.fill(fillX, fillY, fillX + 16, fillY + 16, 0xAAFFFFFF);
            }
            graphics.text(this.font, str, slot.x() + (18 - this.font.width(str)) / 2, slot.y() + (this.font.lineHeight / 2), 0xFFFFFFFF, false);
            List<Item> items = slot.items;
            int size = items.size();
            if (size > 0) {
                int itemIndex = (int) ((System.currentTimeMillis() / 1000) % size);
                Item tagItem = items.get(itemIndex);
                graphics.fakeItem(tagItem.getDefaultInstance(), slot.x, slot.y);
            }
            if (slot.isHovering(mouseX, mouseY) && index != this.selectedSlot){
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, slot.x - 4, slot.y - 4, 24, 24);
            }
        }
    }

    private void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for (RestrictionSlot slot : slotByIndex.values()){
            if (slot.isHovering(mouseX, mouseY) && slot.value != null){
                graphics.setComponentTooltipForNextFrame(this.font, List.of(Component.literal(slot.value)), mouseX, mouseY);
            }
        }
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
        return super.mouseClicked(event, doubleClick);
    }

    private static List<Item> itemsFromString(String str){
        if (str.startsWith("#")){
            TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(str.substring(1)));

            return BuiltInRegistries.ITEM.getOrThrow(tag).stream().map(Holder::value).toList();
        }
        else {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(str));
            return List.of(item);
        }
    }

    private List<TagKey<Item>> getAllItemTags() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return List.of();

        RegistryAccess registryAccess = level.registryAccess();
        HolderLookup.RegistryLookup<Item> itemLookup = registryAccess.lookupOrThrow(Registries.ITEM);

        return itemLookup.listTagIds().toList();
    }

    public record RestrictionSlot(int x, int y, List<Item> items, String value){
        public boolean isHovering(double mouseX, double mouseY){
            return mouseX >= x - 1 && mouseX <= x + 16 && mouseY >= y - 1 && mouseY <= y + 16;
        }

        public RestrictionSlot withNewValue(String value){
            if (value == null){
                return new RestrictionSlot(x, y, itemsFromString("minecraft:air"), null);
            }
            return new RestrictionSlot(x, y, itemsFromString(value), value);
        }
    }
}
