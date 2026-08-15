package com.coolerpromc.restrictedinventory.config.util;

import com.coolerpromc.restrictedinventory.Constants;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public sealed interface Restriction permits ItemEntry, GroupEntry {
    Codec<Restriction> CODEC = RestrictionCodecs.alternative(GroupEntry.CODEC, ItemEntry.ENTRY_CODEC).xmap(
        value -> value.map(group -> (Restriction) group, entry -> (Restriction) entry),
        restriction -> restriction instanceof GroupEntry group ? Either.left(group) : Either.right((ItemEntry) restriction)
    );

    Codec<Map<String, Restriction>> CONFIG_CODEC = Codec.unboundedMap(RestrictionCodecs.SLOT_INDEX, CODEC);

    boolean matches(ItemStack stack);
    Optional<DisplayEntry> display();
    List<ItemStack> displayStacks();

    default ItemStack displayStack() {
        List<ItemStack> stacks = displayStacks();
        if (stacks.isEmpty()) return ItemStack.EMPTY;

        return stacks.get((int) ((System.currentTimeMillis() / 1000) % stacks.size()));
    }

    default Tag serializeNbt() {
        return CODEC.encodeStart(NbtOps.INSTANCE, this)
                .resultOrPartial(error -> Constants.LOGGER.warn("Could not write restriction {}: {}", this, error))
                .orElseGet(CompoundTag::new);
    }

    static Optional<Restriction> read(@Nullable Tag tag) {
        if (tag == null) return Optional.empty();

        return CODEC.parse(NbtOps.INSTANCE, tag).resultOrPartial(error -> Constants.LOGGER.warn("Dropping unreadable restriction entry {}: {}", tag, error));
    }
}
