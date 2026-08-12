package com.coolerpromc.restrictedinventory.config.util;

import com.coolerpromc.restrictedinventory.Constants;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class ItemEntry {
    public static final Codec<String> ITEM_ID_CODEC = Codec.STRING.validate(ItemEntry::validateId);

    public static final Codec<ItemEntry> CODEC = RecordCodecBuilder.create(i -> i.group(ITEM_ID_CODEC.fieldOf("item").forGetter(ItemEntry::item), CompoundTag.CODEC.optionalFieldOf("components").forGetter(ItemEntry::components)).apply(i, ItemEntry::new));

    public static final Codec<Map<String, Either<String, ItemEntry>>> CONFIG_CODEC = Codec.unboundedMap(Codec.STRING.validate(s -> s.matches("\\d+") && Integer.parseInt(s) >= 0 && Integer.parseInt(s) <= 35 ? DataResult.success(s) : DataResult.error(() -> "Index must between 0 and 35")), Codec.xor(ITEM_ID_CODEC, ItemEntry.CODEC));

    public static final Codec<ItemEntry> STORAGE_CODEC = Codec.either(ITEM_ID_CODEC, CODEC).xmap(ItemEntry::of, ItemEntry::compact);

    public static final StreamCodec<ByteBuf, ItemEntry> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ItemEntry::item, ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), ItemEntry::components, ItemEntry::new);

    private final String item;
    private final Optional<CompoundTag> components;

    private @Nullable DataComponentPredicate predicate;
    private @Nullable RegistryAccess predicateRegistries;

    public ItemEntry(String item, Optional<CompoundTag> components) {
        this.item = item;
        this.components = components;
    }

    public ItemEntry(String item) {
        this(item, Optional.empty());
    }

    public String item() {
        return item;
    }

    public Optional<CompoundTag> components() {
        return components;
    }

    private static DataResult<String> validateId(String value) {
        String id = value.startsWith("#") ? value.substring(1) : value;
        return ResourceLocation.tryParse(id) != null ? DataResult.success(value) : DataResult.error(() -> "Not a valid item or tag id: " + value);
    }

    public static ItemEntry of(Either<String, ItemEntry> value) {
        return value.map(ItemEntry::new, Function.identity());
    }

    public Either<String, ItemEntry> compact() {
        return components.isEmpty() ? Either.left(item) : Either.right(this);
    }

    public static Optional<ItemEntry> read(@Nullable Tag tag) {
        if (tag == null) return Optional.empty();

        DataResult<ItemEntry> result = STORAGE_CODEC.parse(NbtOps.INSTANCE, tag);
        result.error().ifPresent(error -> Constants.LOGGER.warn("Dropping unreadable restriction entry {}: {}", tag, error.message()));
        return result.result();
    }

    public Tag serializeNbt() {
        DataResult<Tag> result = STORAGE_CODEC.encodeStart(NbtOps.INSTANCE, this);
        result.error().ifPresent(error -> Constants.LOGGER.warn("Could not write restriction entry for {}: {}", item, error.message()));
        return result.result().orElseGet(() -> StringTag.valueOf(item));
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

    public boolean matches(ItemStack stack, RegistryAccess registries) {
        ResourceLocation id = id();
        if (id == null) return false;

        boolean matchesItem = isTag() ? stack.is(TagKey.create(Registries.ITEM, id)) : BuiltInRegistries.ITEM.getOptional(id).map(stack::is).orElse(false);

        if (!matchesItem) return false;
        if (components.isEmpty()) return true;

        DataComponentPredicate expected = predicate(registries);
        return expected != null && expected.test(stack);
    }

    private @Nullable DataComponentPredicate predicate(RegistryAccess registries) {
        if (predicate != null && predicateRegistries == registries) return predicate;

        DataResult<DataComponentPredicate> result = DataComponentPredicate.CODEC.parse(ops(registries), components.orElseThrow());
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

    public ItemStack display(Item item, RegistryAccess registries) {
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
        return other instanceof ItemEntry entry && item.equals(entry.item) && components.equals(entry.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, components);
    }

    @Override
    public String toString() {
        return components.map(tag -> item + tag).orElse(item);
    }
}
