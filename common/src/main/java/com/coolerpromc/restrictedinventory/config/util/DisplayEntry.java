package com.coolerpromc.restrictedinventory.config.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Function;

public record DisplayEntry(String item, Optional<CompoundTag> tag) {
    public static final Codec<DisplayEntry> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(
        RestrictionCodecs.ITEM_ID.fieldOf("item").forGetter(DisplayEntry::item),
        RestrictionCodecs.NBT_FIELD.forGetter(DisplayEntry::tag)
    ).apply(i, DisplayEntry::new));

    public static final Codec<DisplayEntry> CODEC = RestrictionCodecs.alternative(RestrictionCodecs.ITEM_ID, FULL_CODEC).xmap(
        value -> value.map(DisplayEntry::new, Function.identity()),
        entry -> entry.tag().isEmpty() ? Either.left(entry.item()) : Either.right(entry)
    );

    public DisplayEntry(String item) {
        this(item, Optional.empty());
    }

    public ItemStack stack() {
        ResourceLocation id = ResourceLocation.tryParse(item);
        if (id == null) return ItemStack.EMPTY;

        return BuiltInRegistries.ITEM.getOptional(id).map(value -> {
            ItemStack stack = new ItemStack(value);
            tag.ifPresent(nbt -> stack.setTag(nbt.copy()));
            return stack;
        }).orElse(ItemStack.EMPTY);
    }
}
