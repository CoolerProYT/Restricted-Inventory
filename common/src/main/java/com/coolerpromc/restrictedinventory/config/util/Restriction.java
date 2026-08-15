package com.coolerpromc.restrictedinventory.config.util;

import com.coolerpromc.restrictedinventory.Constants;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public sealed interface Restriction permits ItemEntry, GroupEntry {
    Codec<Restriction> CODEC = RestrictionCodecs.alternative(GroupEntry.CODEC, ItemEntry.ENTRY_CODEC).xmap(
        value -> value.map(group -> (Restriction) group, entry -> (Restriction) entry),
        restriction -> restriction instanceof GroupEntry group ? Either.left(group) : Either.right((ItemEntry) restriction)
    );

    Codec<Map<String, Restriction>> CONFIG_CODEC = Codec.unboundedMap(RestrictionCodecs.SLOT_INDEX, CODEC);

    StreamCodec<ByteBuf, Restriction> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    boolean matches(ItemStack stack, RegistryAccess registries);

    Optional<DisplayEntry> display();

    List<ItemStack> displayStacks(RegistryAccess registries);

    default ItemStack displayStack(RegistryAccess registries) {
        List<ItemStack> stacks = displayStacks(registries);
        if (stacks.isEmpty()) return ItemStack.EMPTY;

        return stacks.get((int) ((System.currentTimeMillis() / 1000) % stacks.size()));
    }

    default Tag serializeNbt() {
        DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, this);
        result.error().ifPresent(error -> Constants.LOGGER.warn("Could not write restriction {}: {}", this, error.message()));
        return result.result().orElseGet(CompoundTag::new);
    }

    static Optional<Restriction> read(@Nullable Tag tag) {
        if (tag == null) return Optional.empty();

        DataResult<Restriction> result = CODEC.parse(NbtOps.INSTANCE, tag);
        result.error().ifPresent(error -> Constants.LOGGER.warn("Dropping unreadable restriction entry {}: {}", tag, error.message()));
        return result.result();
    }
}
