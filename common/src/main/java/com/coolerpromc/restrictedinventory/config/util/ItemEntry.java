package com.coolerpromc.restrictedinventory.config.util;

import com.coolerpromc.restrictedinventory.Constants;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record ItemEntry(String item, Optional<CompoundTag> tag) {
    public static final Codec<String> ITEM_ID_CODEC = ExtraCodecs.validate(Codec.STRING, ItemEntry::validateId);

    public static final Codec<ItemEntry> CODEC = RecordCodecBuilder.create(i -> i.group(ITEM_ID_CODEC.fieldOf("item").forGetter(ItemEntry::item), CompoundTag.CODEC.optionalFieldOf("tag").forGetter(ItemEntry::tag)).apply(i, ItemEntry::new));

    public ItemEntry(String item) {
        this(item, Optional.empty());
    }

    public static final Codec<Map<String, Either<String, ItemEntry>>> CONFIG_CODEC = Codec.unboundedMap(ExtraCodecs.validate(Codec.STRING, s -> s.matches("\\d+") && Integer.parseInt(s) >= 0 && Integer.parseInt(s) <= 35 ? DataResult.success(s) : DataResult.error(() -> "Index must between 0 and 35")), ExtraCodecs.xor(ITEM_ID_CODEC, ItemEntry.CODEC));

    public static final Codec<ItemEntry> STORAGE_CODEC = Codec.either(ITEM_ID_CODEC, CODEC).xmap(
        ItemEntry::of,
        entry -> entry.tag().isEmpty() ? Either.left(entry.item()) : Either.right(entry)
    );

    private static DataResult<String> validateId(String value) {
        String id = value.startsWith("#") ? value.substring(1) : value;
        return ResourceLocation.isValidResourceLocation(id) ? DataResult.success(value) : DataResult.error(() -> "Not a valid item or tag id: " + value);
    }

    public static ItemEntry of(Either<String, ItemEntry> value) {
        return value.map(ItemEntry::new, Function.identity());
    }

    public static Optional<ItemEntry> read(@Nullable Tag tag) {
        if (tag == null) return Optional.empty();

        return STORAGE_CODEC.parse(NbtOps.INSTANCE, tag).resultOrPartial(error -> Constants.LOGGER.warn("Dropping unreadable restriction entry {}: {}", tag, error));
    }

    public Tag serializeNbt() {
        return STORAGE_CODEC.encodeStart(NbtOps.INSTANCE, this)
                .resultOrPartial(error -> Constants.LOGGER.warn("Could not write restriction entry for {}: {}", item, error))
                .orElseGet(() -> StringTag.valueOf(item));
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

    public boolean matches(ItemStack stack) {
        ResourceLocation id = id();
        if (id == null) return false;

        boolean matchesItem = isTag() ? stack.is(TagKey.create(Registries.ITEM, id)) : BuiltInRegistries.ITEM.getOptional(id).map(stack::is).orElse(false);

        return matchesItem && (tag.isEmpty() || hasTag(stack, tag.get()));
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

    public ItemStack display(Item item) {
        ItemStack stack = new ItemStack(item);
        tag.ifPresent(t -> stack.setTag(t.copy()));
        return stack;
    }
}
