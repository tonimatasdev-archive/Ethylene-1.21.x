package org.bukkit.craftbukkit.inventory;

import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;

@DelegateDeserialization(SerializableMeta.class)
public class CraftMetaArmorStand extends CraftMetaItem {

    static final ItemMetaKeyType<net.minecraft.world.item.component.CustomData> ENTITY_TAG = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.ENTITY_DATA, "entity-tag");
    net.minecraft.nbt.CompoundTag entityTag;

    CraftMetaArmorStand(CraftMetaItem meta) {
        super(meta);

        if (!(meta instanceof CraftMetaArmorStand)) {
            return;
        }

        CraftMetaArmorStand armorStand = (CraftMetaArmorStand) meta;
        this.entityTag = armorStand.entityTag;
    }

    CraftMetaArmorStand(net.minecraft.core.component.DataComponentPatch tag) {
        super(tag);

        getOrEmpty(tag, ENTITY_TAG).ifPresent((nbt) -> {
            entityTag = nbt.copyTag();
        });
    }

    CraftMetaArmorStand(Map<String, Object> map) {
        super(map);
    }

    @Override
    void deserializeInternal(net.minecraft.nbt.CompoundTag tag, Object context) {
        super.deserializeInternal(tag, context);

        if (tag.contains(ENTITY_TAG.NBT)) {
            entityTag = tag.getCompound(ENTITY_TAG.NBT);
        }
    }

    @Override
    void serializeInternal(Map<String, net.minecraft.nbt.Tag> internalTags) {
        if (entityTag != null && !entityTag.isEmpty()) {
            internalTags.put(ENTITY_TAG.NBT, entityTag);
        }
    }

    @Override
    void applyToItem(CraftMetaItem.Applicator tag) {
        super.applyToItem(tag);

        if (entityTag != null) {
            tag.put(ENTITY_TAG, net.minecraft.world.item.component.CustomData.of(entityTag));
        }
    }

    @Override
    boolean applicableTo(Material type) {
        return type == Material.ARMOR_STAND;
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && isArmorStandEmpty();
    }

    boolean isArmorStandEmpty() {
        return !(entityTag != null);
    }

    @Override
    boolean equalsCommon(CraftMetaItem meta) {
        if (!super.equalsCommon(meta)) {
            return false;
        }
        if (meta instanceof CraftMetaArmorStand) {
            CraftMetaArmorStand that = (CraftMetaArmorStand) meta;

            return entityTag != null ? that.entityTag != null && this.entityTag.equals(that.entityTag) : entityTag == null;
        }
        return true;
    }

    @Override
    boolean notUncommon(CraftMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof CraftMetaArmorStand || isArmorStandEmpty());
    }

    @Override
    int applyHash() {
        final int original;
        int hash = original = super.applyHash();

        if (entityTag != null) {
            hash = 73 * hash + entityTag.hashCode();
        }

        return original != hash ? CraftMetaArmorStand.class.hashCode() ^ hash : hash;
    }

    @Override
    Builder<String, Object> serialize(Builder<String, Object> builder) {
        super.serialize(builder);

        return builder;
    }

    @Override
    public CraftMetaArmorStand clone() {
        CraftMetaArmorStand clone = (CraftMetaArmorStand) super.clone();

        if (entityTag != null) {
            clone.entityTag = entityTag.copy();
        }

        return clone;
    }
}
