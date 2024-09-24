package org.bukkit.craftbukkit.inventory;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Axolotl;
import org.bukkit.inventory.meta.AxolotlBucketMeta;

@DelegateDeserialization(SerializableMeta.class)
public class CraftMetaAxolotlBucket extends CraftMetaItem implements AxolotlBucketMeta {

    static final ItemMetaKey VARIANT = new ItemMetaKey("Variant", "axolotl-variant");
    static final ItemMetaKeyType<net.minecraft.world.item.component.CustomData> ENTITY_TAG = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.ENTITY_DATA, "entity-tag");
    static final ItemMetaKeyType<net.minecraft.world.item.component.CustomData> BUCKET_ENTITY_TAG = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.BUCKET_ENTITY_DATA, "bucket-entity-tag");

    private Integer variant;
    private net.minecraft.nbt.CompoundTag entityTag;
    private net.minecraft.nbt.CompoundTag bucketEntityTag;

    CraftMetaAxolotlBucket(CraftMetaItem meta) {
        super(meta);

        if (!(meta instanceof CraftMetaAxolotlBucket)) {
            return;
        }

        CraftMetaAxolotlBucket bucket = (CraftMetaAxolotlBucket) meta;
        this.variant = bucket.variant;
        this.entityTag = bucket.entityTag;
        this.bucketEntityTag = bucket.bucketEntityTag;
    }

    CraftMetaAxolotlBucket(net.minecraft.core.component.DataComponentPatch tag) {
        super(tag);

        getOrEmpty(tag, ENTITY_TAG).ifPresent((nbt) -> {
            entityTag = nbt.copyTag();

            if (entityTag.contains(VARIANT.NBT, CraftMagicNumbers.NBT.TAG_INT)) {
                this.variant = entityTag.getInt(VARIANT.NBT);
            }
        });
        getOrEmpty(tag, BUCKET_ENTITY_TAG).ifPresent((nbt) -> {
            bucketEntityTag = nbt.copyTag();

            if (bucketEntityTag.contains(VARIANT.NBT, CraftMagicNumbers.NBT.TAG_INT)) {
                this.variant = bucketEntityTag.getInt(VARIANT.NBT);
            }
        });
    }

    CraftMetaAxolotlBucket(Map<String, Object> map) {
        super(map);

        Integer variant = SerializableMeta.getObject(Integer.class, map, VARIANT.BUKKIT, true);
        if (variant != null) {
            this.variant = variant;
        }
    }

    @Override
    void deserializeInternal(net.minecraft.nbt.CompoundTag tag, Object context) {
        super.deserializeInternal(tag, context);

        if (tag.contains(ENTITY_TAG.NBT)) {
            entityTag = tag.getCompound(ENTITY_TAG.NBT);
        }
        if (tag.contains(BUCKET_ENTITY_TAG.NBT)) {
            bucketEntityTag = tag.getCompound(BUCKET_ENTITY_TAG.NBT);
        }
    }

    @Override
    void serializeInternal(Map<String, net.minecraft.nbt.Tag> internalTags) {
        if (entityTag != null && !entityTag.isEmpty()) {
            internalTags.put(ENTITY_TAG.NBT, entityTag);
        }
        if (bucketEntityTag != null && !bucketEntityTag.isEmpty()) {
            internalTags.put(BUCKET_ENTITY_TAG.NBT, bucketEntityTag);
        }
    }

    @Override
    void applyToItem(CraftMetaItem.Applicator tag) {
        super.applyToItem(tag);

        if (entityTag != null) {
            tag.put(ENTITY_TAG, net.minecraft.world.item.component.CustomData.of(entityTag));
        }

        net.minecraft.nbt.CompoundTag bucketEntityTag = (this.bucketEntityTag != null) ? this.bucketEntityTag.copy() : null;
        if (hasVariant()) {
            if (bucketEntityTag == null) {
                bucketEntityTag = new net.minecraft.nbt.CompoundTag();
            }
            bucketEntityTag.putInt(VARIANT.NBT, variant);
        }

        if (bucketEntityTag != null) {
            tag.put(BUCKET_ENTITY_TAG, net.minecraft.world.item.component.CustomData.of(bucketEntityTag));
        }
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && isBucketEmpty();
    }

    boolean isBucketEmpty() {
        return !(hasVariant() || entityTag != null || bucketEntityTag != null);
    }

    @Override
    public Axolotl.Variant getVariant() {
        return Axolotl.Variant.values()[variant];
    }

    @Override
    public void setVariant(Axolotl.Variant variant) {
        if (variant == null) {
            variant = Axolotl.Variant.LUCY;
        }
        this.variant = variant.ordinal();
    }

    @Override
    public boolean hasVariant() {
        return variant != null;
    }

    @Override
    boolean equalsCommon(CraftMetaItem meta) {
        if (!super.equalsCommon(meta)) {
            return false;
        }
        if (meta instanceof CraftMetaAxolotlBucket) {
            CraftMetaAxolotlBucket that = (CraftMetaAxolotlBucket) meta;

            return (hasVariant() ? that.hasVariant() && this.variant.equals(that.variant) : !that.hasVariant())
                    && (entityTag != null ? that.entityTag != null && this.entityTag.equals(that.entityTag) : that.entityTag == null)
                    && (bucketEntityTag != null ? that.bucketEntityTag != null && this.bucketEntityTag.equals(that.bucketEntityTag) : that.bucketEntityTag == null);
        }
        return true;
    }

    @Override
    boolean notUncommon(CraftMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof CraftMetaAxolotlBucket || isBucketEmpty());
    }

    @Override
    int applyHash() {
        final int original;
        int hash = original = super.applyHash();

        if (hasVariant()) {
            hash = 61 * hash + variant;
        }
        if (entityTag != null) {
            hash = 61 * hash + entityTag.hashCode();
        }
        if (bucketEntityTag != null) {
            hash = 61 * hash + bucketEntityTag.hashCode();
        }

        return original != hash ? CraftMetaAxolotlBucket.class.hashCode() ^ hash : hash;
    }

    @Override
    public CraftMetaAxolotlBucket clone() {
        CraftMetaAxolotlBucket clone = (CraftMetaAxolotlBucket) super.clone();

        if (entityTag != null) {
            clone.entityTag = entityTag.copy();
        }
        if (bucketEntityTag != null) {
            clone.bucketEntityTag = bucketEntityTag.copy();
        }

        return clone;
    }

    @Override
    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        super.serialize(builder);

        if (hasVariant()) {
            builder.put(VARIANT.BUKKIT, variant);
        }

        return builder;
    }
}
