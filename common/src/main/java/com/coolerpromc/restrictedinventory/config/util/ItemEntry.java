package com.coolerpromc.restrictedinventory.config.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record ItemEntry(String item, Optional<CompoundTag> tag, Optional<DisplayEntry> display) implements Restriction {
    public static final Codec<ItemEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
        RestrictionCodecs.ITEM_OR_TAG_ID.fieldOf("item").forGetter(ItemEntry::item),
        RestrictionCodecs.NBT_FIELD.forGetter(ItemEntry::tag),
        RestrictionCodecs.strictOptionalField(DisplayEntry.CODEC, "display").forGetter(ItemEntry::display)
    ).apply(i, ItemEntry::new));

    public static final Codec<ItemEntry> ENTRY_CODEC = RestrictionCodecs.alternative(RestrictionCodecs.ITEM_OR_TAG_ID, CODEC).xmap(
        value -> value.map(ItemEntry::new, Function.identity()),
        entry -> entry.isPlainId() ? Either.left(entry.item()) : Either.right(entry)
    );

    public ItemEntry(String item) {
        this(item, Optional.empty(), Optional.empty());
    }

    public ItemEntry(String item, Optional<CompoundTag> tag) {
        this(item, tag, Optional.empty());
    }

    public boolean isPlainId() {
        return tag.isEmpty() && display.isEmpty();
    }

    public boolean isTag() {
        return item.startsWith("#");
    }

    public @Nullable ResourceLocation id() {
        return ResourceLocation.tryParse(isTag() ? item.substring(1) : item);
    }

    public @Nullable TagKey<Item> tagKey() {
        ResourceLocation id = id();
        return isTag() && id != null ? TagKey.create(Registries.ITEM, id) : null;
    }

    public List<Item> items() {
        ResourceLocation id = id();
        if (id == null) return List.of();

        if (isTag()) {
            return BuiltInRegistries.ITEM.getOrCreateTag(TagKey.create(Registries.ITEM, id)).stream().map(Holder::value).toList();
        }
        return BuiltInRegistries.ITEM.getOptional(id).<List<Item>>map(List::of).orElseGet(List::of);
    }

    @Override
    public boolean matches(ItemStack stack) {
        ResourceLocation id = id();
        if (id == null) return false;

        boolean matchesItem = isTag() ? stack.is(TagKey.create(Registries.ITEM, id)) : BuiltInRegistries.ITEM.getOptional(id).map(stack::is).orElse(false);

        return matchesItem && (tag.isEmpty() || hasTag(stack, tag.get()));
    }

    @Override
    public List<ItemStack> displayStacks() {
        ItemStack pinned = display.map(DisplayEntry::stack).orElse(ItemStack.EMPTY);
        if (!pinned.isEmpty()) return List.of(pinned);

        return items().stream().map(this::stackOf).toList();
    }

    public ItemStack stackOf(Item item) {
        ItemStack stack = new ItemStack(item);
        tag.ifPresent(t -> stack.setTag(t.copy()));
        return stack;
    }

    private static boolean hasTag(ItemStack stack, CompoundTag tag) {
        return contains(tag, stack.getTag());
    }

    private static boolean contains(@Nullable Tag expected, @Nullable Tag actual) {
        if (expected == null) return true;
        if (actual == null) return false;

        if (expected instanceof NumericTag expectedNumber && actual instanceof NumericTag actualNumber) {
            return expectedNumber.getAsDouble() == actualNumber.getAsDouble();
        }

        if (expected instanceof CompoundTag expectedCompound) {
            if (!(actual instanceof CompoundTag actualCompound)) return false;
            for (String key : expectedCompound.getAllKeys()) {
                if (!contains(expectedCompound.get(key), actualCompound.get(key))) return false;
            }
            return true;
        }

        if (expected instanceof CollectionTag<?> expectedElements) {
            if (!(actual instanceof CollectionTag<?> actualElements)) return false;
            if (expectedElements.isEmpty()) return actualElements.isEmpty();

            for (Tag element : expectedElements) {
                boolean matched = false;
                for (Tag candidate : actualElements) {
                    if (contains(element, candidate)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) return false;
            }
            return true;
        }

        return expected.equals(actual);
    }
}
