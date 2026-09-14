package com.coolerpromc.restrictedinventory.config.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public record GroupEntry(Identifier group, Optional<DisplayEntry> display) implements Restriction {
    public static final Codec<GroupEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
        RestrictionCodecs.GROUP_ID.fieldOf("group").forGetter(GroupEntry::group),
        RestrictionCodecs.strictOptionalField(DisplayEntry.CODEC, "display").forGetter(GroupEntry::display)
    ).apply(i, GroupEntry::new));

    public GroupEntry(Identifier group) {
        this(group, Optional.empty());
    }

    /** An unknown group accepts nothing, so a typo cannot silently unlock a slot. */
    @Override
    public boolean matches(ItemStack stack, RegistryAccess registries) {
        return RestrictionGroups.get(group).map(value -> value.matches(stack, registries)).orElse(false);
    }

    @Override
    public List<ItemStack> displayStacks(RegistryAccess registries) {
        ItemStack pinned = display.map(entry -> entry.stack(registries)).orElse(ItemStack.EMPTY);
        if (!pinned.isEmpty()) return List.of(pinned);

        return RestrictionGroups.get(group).map(value -> value.displayStacks(registries)).orElseGet(List::of);
    }
}
