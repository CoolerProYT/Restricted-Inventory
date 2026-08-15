package com.coolerpromc.restrictedinventory.config.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record RestrictionGroup(List<ItemEntry> entries, Optional<DisplayEntry> display) {
    public static final Codec<List<ItemEntry>> ENTRIES_CODEC = ExtraCodecs.validate(
        ItemEntry.ENTRY_CODEC.listOf(),
        entries -> entries.isEmpty() ? DataResult.error(() -> "A restriction group must contain at least one entry") : DataResult.success(entries)
    );

    public static final Codec<RestrictionGroup> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(
        ENTRIES_CODEC.fieldOf("entries").forGetter(RestrictionGroup::entries),
        RestrictionCodecs.strictOptionalField(DisplayEntry.CODEC, "display").forGetter(RestrictionGroup::display)
    ).apply(i, RestrictionGroup::new));

    public static final Codec<RestrictionGroup> CODEC = RestrictionCodecs.alternative(ENTRIES_CODEC, FULL_CODEC).xmap(
        value -> value.map(RestrictionGroup::new, Function.identity()),
        group -> group.display().isEmpty() ? Either.left(group.entries()) : Either.right(group)
    );

    public static final Codec<Map<String, RestrictionGroup>> CONFIG_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);

    public RestrictionGroup(List<ItemEntry> entries) {
        this(entries, Optional.empty());
    }

    public boolean matches(ItemStack stack) {
        for (ItemEntry entry : entries) {
            if (entry.matches(stack)) return true;
        }
        return false;
    }

    public List<ItemStack> displayStacks() {
        ItemStack pinned = display.map(DisplayEntry::stack).orElse(ItemStack.EMPTY);
        if (!pinned.isEmpty()) return List.of(pinned);

        return entries.stream().flatMap(entry -> entry.displayStacks().stream()).toList();
    }
}
