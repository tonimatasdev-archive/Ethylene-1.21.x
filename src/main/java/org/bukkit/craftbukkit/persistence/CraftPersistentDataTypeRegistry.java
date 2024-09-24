package org.bukkit.craftbukkit.persistence;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * The craft persistent data type registry, at its core, is responsible for the
 * conversion process between a {@link PersistentDataType} and a respective
 * {@link net.minecraft.nbt.Tag} instance.
 * <p>
 * It does so by creating {@link TagAdapter} instances that are capable of
 * mappings the supported "primitive types" of {@link PersistentDataType}s to
 * their respective {@link net.minecraft.nbt.Tag} instances.
 * <p>
 * To accomplish this, the class makes <b>heavy</b> use of raw arguments. Their
 * validity is enforced by the mapping of class to {@link TagAdapter}
 * internally.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class CraftPersistentDataTypeRegistry {

    private final Function<Class, TagAdapter> CREATE_ADAPTER = this::createAdapter;

    /**
     * A tag adapter is a closely related type to a specific implementation of
     * the {@link net.minecraft.nbt.Tag} interface. It exists to convert from and to the
     * respective value of a {@link net.minecraft.nbt.Tag} to a "primitive type" for later
     * usage in {@link PersistentDataType}.
     *
     * @param primitiveType the class of the primitive type, e.g.
     * {@link String}.
     * @param nbtBaseType the class of the tag implementation that is used to
     * store this primitive type, e.g {@link net.minecraft.nbt.StringTag}.
     * @param nmsTypeByte the byte identifier of the tag as defined by
     * {@link net.minecraft.nbt.Tag#getId()}.
     * @param builder a bi function that is responsible for mapping a "primitive
     * type" and its respective {@link PersistentDataType} to a {@link net.minecraft.nbt.Tag}.
     * @param extractor a bi function that is responsible for extracting a
     * "primitive type" from a {@link net.minecraft.nbt.Tag} given a
     * {@link PersistentDataType}.
     * @param matcher a bi predicate that is responsible for computing if the
     * passed {@link net.minecraft.nbt.Tag} holds a value that the {@link PersistentDataType}
     * can extract.
     * @param <P> the generic type of the primitive the persistent data type
     * expects.
     * @param <T> the generic type of the concrete {@link net.minecraft.nbt.Tag}
     * implementation that the primitive type is mapped into.
     */
    private record TagAdapter<P, T extends net.minecraft.nbt.Tag>(
            Class<P> primitiveType,
            Class<T> nbtBaseType,
            byte nmsTypeByte,
            BiFunction<PersistentDataType<P, ?>, P, T> builder,
            BiFunction<PersistentDataType<P, ?>, T, P> extractor,
            BiPredicate<PersistentDataType<P, ?>, net.minecraft.nbt.Tag> matcher) {

        /**
         * Extract the primitive value from the {@link net.minecraft.nbt.Tag}.
         *
         * @param base the base to extract from
         * @return the value stored inside the tag
         * @throws ClassCastException if the passed base is not an instanced of
         * the defined base type and therefore is not applicable to the
         * extractor function.
         */
        private P extract(final PersistentDataType<P, ?> dataType, final net.minecraft.nbt.Tag base) {
            Preconditions.checkArgument(this.nbtBaseType.isInstance(base), "The provided net.minecraft.nbt.Tag was of the type %s. Expected type %s", base.getClass().getSimpleName(), this.nbtBaseType.getSimpleName());
            return this.extractor.apply(dataType, this.nbtBaseType.cast(base));
        }

        /**
         * Builds a tag instance wrapping around the provided primitive value.
         *
         * @param value the value to store inside the created tag
         * @return the new tag instance
         * @throws ClassCastException if the passed value object is not of the
         * defined primitive type and therefore is not applicable to the builder
         * function.
         */
        private T build(final PersistentDataType<P, ?> dataType, final Object value) {
            Preconditions.checkArgument(this.primitiveType.isInstance(value), "The provided value was of the type %s. Expected type %s", value.getClass().getSimpleName(), this.primitiveType.getSimpleName());
            return this.builder.apply(dataType, this.primitiveType.cast(value));
        }

        /**
         * Computes if the provided persistent data type's primitive type is a
         * representation of the {@link net.minecraft.nbt.Tag}.
         *
         * @param base the base tag instance to check against
         * @return if the tag was an instance of the set type
         */
        private boolean isInstance(final PersistentDataType<P, ?> persistentDataType, final net.minecraft.nbt.Tag base) {
            return this.matcher.test(persistentDataType, base);
        }
    }

    private final Map<Class, TagAdapter> adapters = new HashMap<>();

    /**
     * Creates a suitable adapter instance for the primitive class type.
     *
     * @param type the type to create an adapter for
     * @param <T> the generic type of the primitive type
     * @return the created adapter instance
     * @throws IllegalArgumentException if no suitable tag type adapter for this
     * type was found
     */
    private <T> TagAdapter createAdapter(Class<T> type) {
        if (!Primitives.isWrapperType(type)) {
            type = Primitives.wrap(type); //Make sure we will always "switch" over the wrapper types
        }

        // Primitives
        if (Objects.equals(Byte.class, type)) {
            return this.createAdapter(
                    Byte.class, net.minecraft.nbt.ByteTag.class, net.minecraft.nbt.Tag.TAG_BYTE,
                    net.minecraft.nbt.ByteTag::valueOf, net.minecraft.nbt.ByteTag::getAsByte
            );
        }
        if (Objects.equals(Short.class, type)) {
            return this.createAdapter(
                    Short.class, net.minecraft.nbt.ShortTag.class, net.minecraft.nbt.Tag.TAG_SHORT, net.minecraft.nbt.ShortTag::valueOf, net.minecraft.nbt.ShortTag::getAsShort
            );
        }
        if (Objects.equals(Integer.class, type)) {
            return this.createAdapter(
                    Integer.class, net.minecraft.nbt.IntTag.class, net.minecraft.nbt.Tag.TAG_INT, net.minecraft.nbt.IntTag::valueOf, net.minecraft.nbt.IntTag::getAsInt
            );
        }
        if (Objects.equals(Long.class, type)) {
            return this.createAdapter(
                    Long.class, net.minecraft.nbt.LongTag.class, net.minecraft.nbt.Tag.TAG_LONG, net.minecraft.nbt.LongTag::valueOf, net.minecraft.nbt.LongTag::getAsLong
            );
        }
        if (Objects.equals(Float.class, type)) {
            return this.createAdapter(
                    Float.class, net.minecraft.nbt.FloatTag.class, net.minecraft.nbt.Tag.TAG_FLOAT,
                    net.minecraft.nbt.FloatTag::valueOf, net.minecraft.nbt.FloatTag::getAsFloat
            );
        }
        if (Objects.equals(Double.class, type)) {
            return this.createAdapter(
                    Double.class, net.minecraft.nbt.DoubleTag.class, net.minecraft.nbt.Tag.TAG_DOUBLE,
                    net.minecraft.nbt.DoubleTag::valueOf, net.minecraft.nbt.DoubleTag::getAsDouble
            );
        }
        if (Objects.equals(String.class, type)) {
            return this.createAdapter(
                    String.class, net.minecraft.nbt.StringTag.class, net.minecraft.nbt.Tag.TAG_STRING,
                    net.minecraft.nbt.StringTag::valueOf, net.minecraft.nbt.StringTag::getAsString
            );
        }

        // Primitive non-list arrays
        if (Objects.equals(byte[].class, type)) {
            return this.createAdapter(
                    byte[].class, net.minecraft.nbt.ByteArrayTag.class, net.minecraft.nbt.Tag.TAG_BYTE_ARRAY,
                    array -> new net.minecraft.nbt.ByteArrayTag(Arrays.copyOf(array, array.length)),
                    n -> Arrays.copyOf(n.getAsByteArray(), n.size())
            );
        }
        if (Objects.equals(int[].class, type)) {
            return this.createAdapter(
                    int[].class, net.minecraft.nbt.IntArrayTag.class, net.minecraft.nbt.Tag.TAG_INT_ARRAY,
                    array -> new net.minecraft.nbt.IntArrayTag(Arrays.copyOf(array, array.length)),
                    n -> Arrays.copyOf(n.getAsIntArray(), n.size())
            );
        }
        if (Objects.equals(long[].class, type)) {
            return this.createAdapter(
                    long[].class, net.minecraft.nbt.LongArrayTag.class, net.minecraft.nbt.Tag.TAG_LONG_ARRAY,
                    array -> new net.minecraft.nbt.LongArrayTag(Arrays.copyOf(array, array.length)),
                    n -> Arrays.copyOf(n.getAsLongArray(), n.size())
            );
        }

        // Previously "emulated" compound lists, now useless as a proper list type exists.
        if (Objects.equals(PersistentDataContainer[].class, type)) {
            return this.createAdapter(
                    PersistentDataContainer[].class, net.minecraft.nbt.ListTag.class, net.minecraft.nbt.Tag.TAG_LIST,
                    (containerArray) -> {
                        final net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
                        for (final PersistentDataContainer persistentDataContainer : containerArray) {
                            list.add(((CraftPersistentDataContainer) persistentDataContainer).toTagCompound());
                        }
                        return list;
                    },
                    (tag) -> {
                        final PersistentDataContainer[] containerArray = new CraftPersistentDataContainer[tag.size()];
                        for (int i = 0; i < tag.size(); i++) {
                            final CraftPersistentDataContainer container = new CraftPersistentDataContainer(this);
                            final net.minecraft.nbt.CompoundTag compound = tag.getCompound(i);
                            for (final String key : compound.getAllKeys()) {
                                container.put(key, compound.get(key));
                            }
                            containerArray[i] = container;
                        }
                        return containerArray;
                    }
            );
        }

        // Note that this will map the interface PersistentMetadataContainer directly to the CraftBukkit implementation
        // Passing any other instance of this form to the tag type registry will throw a ClassCastException
        // as defined in TagAdapter#build.
        if (Objects.equals(PersistentDataContainer.class, type)) {
            return this.createAdapter(
                    CraftPersistentDataContainer.class, net.minecraft.nbt.CompoundTag.class, net.minecraft.nbt.Tag.TAG_COMPOUND,
                    CraftPersistentDataContainer::toTagCompound,
                    tag -> {
                        final CraftPersistentDataContainer container = new CraftPersistentDataContainer(this);
                        for (final String key : tag.getAllKeys()) {
                            container.put(key, tag.get(key));
                        }
                        return container;
                    });
        }

        if (Objects.equals(List.class, type)) {
            return createAdapter(
                    List.class,
                    net.minecraft.nbt.ListTag.class,
                    net.minecraft.nbt.Tag.TAG_LIST,
                    this::constructList,
                    this::extractList,
                    this::matchesListTag
            );
        }

        throw new IllegalArgumentException("Could not find a valid TagAdapter implementation for the requested type " + type.getSimpleName());
    }

    // Plain constructor helper method.
    private <T, Z extends net.minecraft.nbt.Tag> TagAdapter<T, Z> createAdapter(
            final Class<T> primitiveType, final Class<Z> nbtBaseType, final byte nmsTypeByte,
            final Function<T, Z> builder, final Function<Z, T> extractor
    ) {
        return createAdapter(
                primitiveType,
                nbtBaseType,
                nmsTypeByte,
                (type, t) -> builder.apply(t),
                (type, z) -> extractor.apply(z),
                (type, t) -> nbtBaseType.isInstance(t)
        );
    }

    // Plain constructor helper method.
    private <T, Z extends net.minecraft.nbt.Tag> TagAdapter<T, Z> createAdapter(
            final Class<T> primitiveType, final Class<Z> nbtBaseType, final byte nmsTypeByte,
            final BiFunction<PersistentDataType<T, ?>, T, Z> builder,
            final BiFunction<PersistentDataType<T, ?>, Z, T> extractor,
            final BiPredicate<PersistentDataType<T, ?>, net.minecraft.nbt.Tag> matcher
    ) {
        return new TagAdapter<>(primitiveType, nbtBaseType, nmsTypeByte, builder, extractor, matcher);
    }

    /**
     * Wraps the passed primitive value into a tag instance.
     *
     * @param type the type of the passed value
     * @param value the value to be stored in the tag
     * @param <T> the generic type of the value
     * @return the created tag instance
     * @throws IllegalArgumentException if no suitable tag type adapter for this
     * type was found.
     */
    public <T> net.minecraft.nbt.Tag wrap(final PersistentDataType<T, ?> type, final T value) {
        return this.getOrCreateAdapter(type).build(type, value);
    }

    /**
     * Returns if the tag instance matches the provided primitive type.
     *
     * @param type the type of the primitive value
     * @param base the base instance to check
     * @param <T> the generic type of the type
     * @return if the base stores values of the primitive type passed
     * @throws IllegalArgumentException if no suitable tag type adapter for this
     * type was found.
     */
    public <T> boolean isInstanceOf(final PersistentDataType<T, ?> type, final net.minecraft.nbt.Tag base) {
        return this.getOrCreateAdapter(type).isInstance(type, base);
    }

    /**
     * Fetches or creates an adapter for the requested persistent data type.
     *
     * @param type the persistent data type to find or create an adapter for.
     * @param <T> the generic type of the primitive type of the persistent data
     * type.
     * @param <Z> the generic type of the complex type of the persistent data
     * type.
     * @return the tag adapter instance that was found or created.
     * @throws IllegalArgumentException if no adapter can be created for the
     * persistent data type.
     */
    @NotNull
    private <T, Z extends net.minecraft.nbt.Tag> TagAdapter<T, Z> getOrCreateAdapter(@NotNull final PersistentDataType<T, ?> type) {
        return this.adapters.computeIfAbsent(type.getPrimitiveType(), CREATE_ADAPTER);
    }

    /**
     * Extracts the value out of the provided tag.
     *
     * @param type the type of the value to extract
     * @param tag the tag to extract the value from
     * @param <T> the generic type of the value stored inside the tag
     * @return the extracted value
     * @throws IllegalArgumentException if the passed base is not an instanced
     * of the defined base type and therefore is not applicable to the extractor
     * function.
     * @throws IllegalArgumentException if the found object is not of type
     * passed.
     * @throws IllegalArgumentException if no suitable tag type adapter for this
     * type was found.
     */
    public <T, Z extends net.minecraft.nbt.Tag> T extract(final PersistentDataType<T, ?> type, final net.minecraft.nbt.Tag tag) throws ClassCastException, IllegalArgumentException {
        final Class<T> primitiveType = type.getPrimitiveType();
        final TagAdapter<T, Z> adapter = this.getOrCreateAdapter(type);
        Preconditions.checkArgument(adapter.isInstance(type, tag), "The found tag instance (%s) cannot store %s", tag.getClass().getSimpleName(), primitiveType.getSimpleName());

        final Object foundValue = adapter.extract(type, tag);
        Preconditions.checkArgument(primitiveType.isInstance(foundValue), "The found object is of the type %s. Expected type %s", foundValue.getClass().getSimpleName(), primitiveType.getSimpleName());
        return primitiveType.cast(foundValue);
    }

    /**
     * Constructs a {@link net.minecraft.nbt.ListTag} from a {@link List} instance by using the
     * passed persistent data type.
     *
     * @param type the persistent data type of the list.
     * @param list the list or primitive values.
     * @param <P> the generic type of the primitive values in the list.
     * @return the constructed {@link net.minecraft.nbt.ListTag}.
     */
    private <P, T extends List<P>> net.minecraft.nbt.ListTag constructList(@NotNull final PersistentDataType<T, ?> type, @NotNull final List<P> list) {
        Preconditions.checkArgument(type instanceof ListPersistentDataType<?, ?>, "The passed list cannot be written to the PDC with a %s (expected a list data type)", type.getClass().getSimpleName());
        final ListPersistentDataType<P, ?> listPersistentDataType = (ListPersistentDataType<P, ?>) type;

        final TagAdapter<P, net.minecraft.nbt.Tag> elementAdapter = this.getOrCreateAdapter(listPersistentDataType.elementType());

        final List<net.minecraft.nbt.Tag> values = Lists.newArrayListWithCapacity(list.size());
        for (final P primitiveValue : list) {
            values.add(this.wrap(listPersistentDataType.elementType(), primitiveValue));
        }

        return new net.minecraft.nbt.ListTag(values, values.isEmpty() ? net.minecraft.nbt.ListTag.TAG_END : elementAdapter.nmsTypeByte());
    }

    /**
     * Extracts a {@link List} from a {@link net.minecraft.nbt.ListTag} and a respective
     * {@link PersistentDataType}.
     *
     * @param type the persistent data type of the list.
     * @param listTag the list tag to extract the {@link List} from.
     * @param <P> the generic type of the primitive values stored in the
     * {@link List}.
     * @return the extracted {@link List} instance.
     * @throws IllegalArgumentException if the passed {@link PersistentDataType}
     * is not a {@link ListPersistentDataType} and can hence not be used to
     * extract a {@link List}.
     */
    private <P> List<P> extractList(@NotNull final PersistentDataType<P, ?> type,
            @NotNull final net.minecraft.nbt.ListTag listTag) {
        Preconditions.checkArgument(type instanceof ListPersistentDataType<?, ?>, "The found list tag cannot be read with a %s (expected a list data type)", type.getClass().getSimpleName());
        final ListPersistentDataType<P, ?> listPersistentDataType = (ListPersistentDataType<P, ?>) type;

        final List<P> output = new ObjectArrayList<>(listTag.size());
        for (final net.minecraft.nbt.Tag tag : listTag) {
            output.add(this.extract(listPersistentDataType.elementType(), tag));
        }

        return output;
    }

    /**
     * Computes if the passed {@link net.minecraft.nbt.Tag} is a {@link net.minecraft.nbt.ListTag} and it,
     * including its elements, can be read/written via the passed
     * {@link PersistentDataType}.
     * <p>
     * As empty lists do not explicitly store their type, an empty nbt list can be matched to any
     * ListPersistentDataType.
     * As the persistent data container API does a full copy, this is a non-issue as callers to
     * PDC#get(key, LIST.strings()) and PDC#get(key, LIST.ints()) will receive individual copies, avoiding interference.
     *
     * @param type the persistent data type for which to check if the tag
     * matches.
     * @param tag the tag that is to be checked if it matches the data type.
     * @return whether the passed tag can be read/written via the passed type.
     */
    private boolean matchesListTag(final PersistentDataType<List, ?> type, final net.minecraft.nbt.Tag tag) {
        if ((!(type instanceof final ListPersistentDataType listPersistentDataType))) {
            return false;
        }
        if (!(tag instanceof final net.minecraft.nbt.ListTag listTag)) {
            return false;
        }

        final byte elementType = listTag.getElementType();
        final TagAdapter elementAdapter = this.getOrCreateAdapter(listPersistentDataType.elementType());

        return elementAdapter.nmsTypeByte() == elementType || elementType == net.minecraft.nbt.ListTag.TAG_END;
    }
}
