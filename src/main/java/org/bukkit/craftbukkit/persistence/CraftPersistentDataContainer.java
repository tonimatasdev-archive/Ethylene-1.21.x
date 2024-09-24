package org.bukkit.craftbukkit.persistence;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNBTTagConfigSerializer;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class CraftPersistentDataContainer implements PersistentDataContainer {

    private final Map<String, net.minecraft.nbt.Tag> customDataTags = new HashMap<>();
    private final CraftPersistentDataTypeRegistry registry;
    private final CraftPersistentDataAdapterContext adapterContext;

    public CraftPersistentDataContainer(Map<String, net.minecraft.nbt.Tag> customTags, CraftPersistentDataTypeRegistry registry) {
        this(registry);
        this.customDataTags.putAll(customTags);
    }

    public CraftPersistentDataContainer(CraftPersistentDataTypeRegistry registry) {
        this.registry = registry;
        this.adapterContext = new CraftPersistentDataAdapterContext(this.registry);
    }


    @Override
    public <T, Z> void set(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z value) {
        Preconditions.checkArgument(key != null, "The NamespacedKey key cannot be null");
        Preconditions.checkArgument(type != null, "The provided type cannot be null");
        Preconditions.checkArgument(value != null, "The provided value cannot be null");

        this.customDataTags.put(key.toString(), this.registry.wrap(type, type.toPrimitive(value, adapterContext)));
    }

    @Override
    public <T, Z> boolean has(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
        Preconditions.checkArgument(key != null, "The NamespacedKey key cannot be null");
        Preconditions.checkArgument(type != null, "The provided type cannot be null");

        net.minecraft.nbt.Tag value = this.customDataTags.get(key.toString());
        if (value == null) {
            return false;
        }

        return this.registry.isInstanceOf(type, value);
    }

    @Override
    public boolean has(NamespacedKey key) {
        return this.customDataTags.get(key.toString()) != null;
    }

    @Override
    public <T, Z> Z get(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
        Preconditions.checkArgument(key != null, "The NamespacedKey key cannot be null");
        Preconditions.checkArgument(type != null, "The provided type cannot be null");

        net.minecraft.nbt.Tag value = this.customDataTags.get(key.toString());
        if (value == null) {
            return null;
        }

        return type.fromPrimitive(this.registry.extract(type, value), adapterContext);
    }

    @NotNull
    @Override
    public <T, Z> Z getOrDefault(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z defaultValue) {
        Z z = this.get(key, type);
        return z != null ? z : defaultValue;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getKeys() {
        Set<NamespacedKey> keys = new HashSet<>();

        this.customDataTags.keySet().forEach(key -> {
            String[] keyData = key.split(":", 2);
            if (keyData.length == 2) {
                keys.add(new NamespacedKey(keyData[0], keyData[1]));
            }
        });

        return keys;
    }

    @Override
    public void remove(@NotNull NamespacedKey key) {
        Preconditions.checkArgument(key != null, "The NamespacedKey key cannot be null");

        this.customDataTags.remove(key.toString());
    }

    @Override
    public boolean isEmpty() {
        return this.customDataTags.isEmpty();
    }

    @NotNull
    @Override
    public void copyTo(PersistentDataContainer other, boolean replace) {
        Preconditions.checkArgument(other != null, "The target container cannot be null");

        CraftPersistentDataContainer target = (CraftPersistentDataContainer) other;
        if (replace) {
            target.customDataTags.putAll(customDataTags);
        } else {
            customDataTags.forEach(target.customDataTags::putIfAbsent);
        }
    }

    @Override
    public PersistentDataAdapterContext getAdapterContext() {
        return this.adapterContext;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CraftPersistentDataContainer)) {
            return false;
        }

        Map<String, net.minecraft.nbt.Tag> myRawMap = this.getRaw();
        Map<String, net.minecraft.nbt.Tag> theirRawMap = ((CraftPersistentDataContainer) obj).getRaw();

        return Objects.equals(myRawMap, theirRawMap);
    }

    public net.minecraft.nbt.CompoundTag toTagCompound() {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        for (Entry<String, net.minecraft.nbt.Tag> entry : this.customDataTags.entrySet()) {
            tag.put(entry.getKey(), entry.getValue());
        }
        return tag;
    }

    public void put(String key, net.minecraft.nbt.Tag base) {
        this.customDataTags.put(key, base);
    }

    public void putAll(Map<String, net.minecraft.nbt.Tag> map) {
        this.customDataTags.putAll(map);
    }

    public void putAll(net.minecraft.nbt.CompoundTag compound) {
        for (String key : compound.getAllKeys()) {
            this.customDataTags.put(key, compound.get(key));
        }
    }

    public Map<String, net.minecraft.nbt.Tag> getRaw() {
        return this.customDataTags;
    }

    public CraftPersistentDataTypeRegistry getDataTagTypeRegistry() {
        return this.registry;
    }

    @Override
    public int hashCode() {
        int hashCode = 3;
        hashCode += this.customDataTags.hashCode(); // We will simply add the maps hashcode
        return hashCode;
    }

    public String serialize() {
        return CraftNBTTagConfigSerializer.serialize(toTagCompound());
    }
}
