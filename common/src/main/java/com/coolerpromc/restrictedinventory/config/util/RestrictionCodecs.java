package com.coolerpromc.restrictedinventory.config.util;

import com.coolerpromc.restrictedinventory.Constants;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.stream.Stream;

public final class RestrictionCodecs {
    public static final Codec<String> ITEM_OR_TAG_ID = Codec.STRING.validate(RestrictionCodecs::validateItemOrTagId);

    public static final Codec<String> ITEM_ID = Codec.STRING.validate(RestrictionCodecs::validateItemId);

    public static final Codec<ResourceLocation> GROUP_ID = Codec.STRING.comapFlatMap(RestrictionCodecs::parseGroupId, ResourceLocation::toString);

    public static final Codec<String> SLOT_INDEX = Codec.STRING.validate(RestrictionCodecs::validateSlotIndex);

    public static final MapCodec<Optional<CompoundTag>> COMPONENTS_FIELD = CompoundTag.CODEC.optionalFieldOf("components");

    private RestrictionCodecs() {
    }

    public static <F, S> Codec<Either<F, S>> alternative(Codec<F> first, Codec<S> second) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> ops, T input) {
                DataResult<Pair<Either<F, S>, T>> firstRead = first.decode(ops, input).map(pair -> pair.mapFirst(Either::left));
                DataResult<Pair<Either<F, S>, T>> secondRead = second.decode(ops, input).map(pair -> pair.mapFirst(Either::right));

                if (firstRead.result().isPresent() && secondRead.result().isPresent()) {
                    return DataResult.error(() -> "Ambiguous entry, it reads as both alternatives: " + input);
                }
                if (firstRead.result().isPresent()) return firstRead;
                if (secondRead.result().isPresent()) return secondRead;

                String firstError = firstRead.error().map(DataResult.Error::message).orElse("unknown");
                String secondError = secondRead.error().map(DataResult.Error::message).orElse("unknown");
                return DataResult.error(() -> firstError + "; or " + secondError);
            }

            @Override
            public <T> DataResult<T> encode(Either<F, S> input, DynamicOps<T> ops, T prefix) {
                return input.map(value -> first.encode(value, ops, prefix), value -> second.encode(value, ops, prefix));
            }

            @Override
            public String toString() {
                return "Alternative[" + first + ", " + second + "]";
            }
        };
    }

    public static <T> MapCodec<Optional<T>> strictOptionalField(Codec<T> codec, String name) {
        return new MapCodec<>() {
            @Override
            public <O> DataResult<Optional<T>> decode(DynamicOps<O> ops, MapLike<O> input) {
                O value = input.get(name);
                if (value == null) return DataResult.success(Optional.empty());

                return codec.parse(ops, value).map(Optional::of).mapError(error -> "Invalid '" + name + "': " + error);
            }

            @Override
            public <O> RecordBuilder<O> encode(Optional<T> value, DynamicOps<O> ops, RecordBuilder<O> prefix) {
                return value.map(present -> prefix.add(name, codec.encodeStart(ops, present))).orElse(prefix);
            }

            @Override
            public <O> Stream<O> keys(DynamicOps<O> ops) {
                return Stream.of(ops.createString(name));
            }

            @Override
            public String toString() {
                return "StrictOptionalField[" + name + "]";
            }
        };
    }

    public static DataResult<ResourceLocation> parseGroupId(String value) {
        ResourceLocation id = ResourceLocation.tryParse(qualify(value));
        return id != null ? DataResult.success(id) : DataResult.error(() -> "Not a valid group id: " + value);
    }

    public static String qualify(String value) {
        return value.indexOf(':') >= 0 ? value : Constants.MODID + ":" + value;
    }

    private static DataResult<String> validateItemOrTagId(String value) {
        String id = value.startsWith("#") ? value.substring(1) : value;
        return ResourceLocation.tryParse(id) != null ? DataResult.success(value) : DataResult.error(() -> "Not a valid item or tag id: " + value);
    }

    private static DataResult<String> validateItemId(String value) {
        if (value.startsWith("#")) {
            return DataResult.error(() -> "A display must name a single item, not the tag: " + value);
        }
        return ResourceLocation.tryParse(value) != null ? DataResult.success(value) : DataResult.error(() -> "Not a valid item id: " + value);
    }

    private static DataResult<String> validateSlotIndex(String value) {
        if (!value.matches("\\d{1,9}")) return DataResult.error(() -> "Slot index must be a number: " + value);

        int index = Integer.parseInt(value);
        return index >= 0 && index <= 35 ? DataResult.success(value) : DataResult.error(() -> "Index must between 0 and 35");
    }
}
