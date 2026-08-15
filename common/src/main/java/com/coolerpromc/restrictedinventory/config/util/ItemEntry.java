package com.coolerpromc.restrictedinventory.config.util;

import com.coolerpromc.restrictedinventory.Constants;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class ItemEntry implements Restriction {
    public static final Codec<ItemEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
        RestrictionCodecs.ITEM_OR_TAG_ID.fieldOf("item").forGetter(ItemEntry::item),
        RestrictionCodecs.COMPONENTS_FIELD.forGetter(ItemEntry::components),
        RestrictionCodecs.strictOptionalField(DisplayEntry.CODEC, "display").forGetter(ItemEntry::display)
    ).apply(i, ItemEntry::new));

    public static final Codec<ItemEntry> ENTRY_CODEC = RestrictionCodecs.alternative(RestrictionCodecs.ITEM_OR_TAG_ID, CODEC).xmap(
        value -> value.map(ItemEntry::new, Function.identity()),
        entry -> entry.isPlainId() ? Either.left(entry.item()) : Either.right(entry)
    );

    private final String item;
    private final Optional<CompoundTag> components;
    private final Optional<DisplayEntry> display;

    private @Nullable DataComponentExactPredicate predicate;
    private @Nullable RegistryAccess predicateRegistries;

    public ItemEntry(String item, Optional<CompoundTag> components, Optional<DisplayEntry> display) {
        this.item = item;
        this.components = components;
        this.display = display;
    }

    public ItemEntry(String item, Optional<CompoundTag> components) {
        this(item, components, Optional.empty());
    }

    public ItemEntry(String item) {
        this(item, Optional.empty(), Optional.empty());
    }

    public String item() {
        return item;
    }

    public Optional<CompoundTag> components() {
        return components;
    }

    @Override
    public Optional<DisplayEntry> display() {
        return display;
    }

    public boolean isPlainId() {
        return components.isEmpty() && display.isEmpty();
    }

    public boolean isTag() {
        return item.startsWith("#");
    }

    public @Nullable Identifier id() {
        return Identifier.tryParse(isTag() ? item.substring(1) : item);
    }

    public @Nullable TagKey<Item> tagKey() {
        Identifier id = id();
        return isTag() && id != null ? TagKey.create(Registries.ITEM, id) : null;
    }

    public List<Item> items() {
        Identifier id = id();
        if (id == null) return List.of();

        if (isTag()) {
            return BuiltInRegistries.ITEM.getOrThrow(TagKey.create(Registries.ITEM, id)).stream().map(Holder::value).toList();
        }
        return BuiltInRegistries.ITEM.getOptional(id).<List<Item>>map(List::of).orElseGet(List::of);
    }

    @Override
    public boolean matches(ItemStack stack, RegistryAccess registries) {
        Identifier id = id();
        if (id == null) return false;

        boolean matchesItem = isTag() ? stack.is(TagKey.create(Registries.ITEM, id)) : BuiltInRegistries.ITEM.getOptional(id).map(stack::is).orElse(false);

        if (!matchesItem) return false;
        if (components.isEmpty()) return true;

        DataComponentExactPredicate expected = predicate(registries);
        return expected != null && expected.test(stack);
    }

    @Override
    public List<ItemStack> displayStacks(RegistryAccess registries) {
        ItemStack pinned = display.map(entry -> entry.stack(registries)).orElse(ItemStack.EMPTY);
        if (!pinned.isEmpty()) return List.of(pinned);

        return items().stream().map(value -> stackOf(value, registries)).toList();
    }

    private @Nullable DataComponentExactPredicate predicate(RegistryAccess registries) {
        if (predicate != null && predicateRegistries == registries) return predicate;

        DataResult<DataComponentExactPredicate> result = DataComponentExactPredicate.CODEC.parse(ops(registries), components.orElseThrow());
        result.error().ifPresent(error -> Constants.LOGGER.warn("Ignoring unreadable component filter on {}: {}", item, error.message()));

        this.predicate = result.result().orElse(null);
        this.predicateRegistries = registries;
        return this.predicate;
    }

    public static Optional<CompoundTag> componentsOf(ItemStack stack, RegistryAccess registries) {
        DataComponentPatch patch = stack.getComponentsPatch();
        if (patch.isEmpty()) return Optional.empty();

        DataResult<Tag> result = DataComponentPatch.CODEC.encodeStart(ops(registries), patch);
        result.error().ifPresent(error -> Constants.LOGGER.warn("Could not read components off {}: {}", stack, error.message()));

        return result.result().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast);
    }

    public ItemStack stackOf(Item item, RegistryAccess registries) {
        ItemStack stack = new ItemStack(item);

        components.ifPresent(tag -> {
            DataResult<DataComponentPatch> result = DataComponentPatch.CODEC.parse(ops(registries), tag);
            result.error().ifPresent(error -> Constants.LOGGER.warn("Could not preview component filter on {}: {}", this.item, error.message()));
            result.result().ifPresent(stack::applyComponents);
        });

        return stack;
    }

    private static RegistryOps<Tag> ops(RegistryAccess registries) {
        return registries.createSerializationContext(NbtOps.INSTANCE);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ItemEntry entry && item.equals(entry.item) && components.equals(entry.components) && display.equals(entry.display);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, components, display);
    }

    @Override
    public String toString() {
        return components.map(tag -> item + tag).orElse(item);
    }
}
