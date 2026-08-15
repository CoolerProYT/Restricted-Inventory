package com.coolerpromc.restrictedinventory.config.util;

import com.coolerpromc.restrictedinventory.Constants;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Function;

public record DisplayEntry(String item, Optional<CompoundTag> components) {
    public static final Codec<DisplayEntry> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(
        RestrictionCodecs.ITEM_ID.fieldOf("item").forGetter(DisplayEntry::item),
        RestrictionCodecs.COMPONENTS_FIELD.forGetter(DisplayEntry::components)
    ).apply(i, DisplayEntry::new));

    public static final Codec<DisplayEntry> CODEC = RestrictionCodecs.alternative(RestrictionCodecs.ITEM_ID, FULL_CODEC).xmap(
        value -> value.map(DisplayEntry::new, Function.identity()),
        entry -> entry.components().isEmpty() ? Either.left(entry.item()) : Either.right(entry)
    );

    public DisplayEntry(String item) {
        this(item, Optional.empty());
    }

    public ItemStack stack(RegistryAccess registries) {
        Identifier id = Identifier.tryParse(item);
        if (id == null) return ItemStack.EMPTY;

        return BuiltInRegistries.ITEM.getOptional(id).map(value -> {
            ItemStack stack = new ItemStack(value);

            components.ifPresent(tag -> {
                RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
                DataResult<DataComponentPatch> result = DataComponentPatch.CODEC.parse(ops, tag);
                result.error().ifPresent(error -> Constants.LOGGER.warn("Could not read display components on {}: {}", item, error.message()));
                result.result().ifPresent(stack::applyComponents);
            });

            return stack;
        }).orElse(ItemStack.EMPTY);
    }
}
