package org.bukkit.craftbukkit.tag;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public abstract class CraftTag<N, B extends Keyed> implements Tag<B> {

    protected final net.minecraft.core.Registry<N> registry;
    protected final net.minecraft.tags.TagKey<N> tag;
    //
    private net.minecraft.core.HolderSet.Named<N> handle;

    public CraftTag(net.minecraft.core.Registry<N> registry, net.minecraft.tags.TagKey<N> tag) {
        this.registry = registry;
        this.tag = tag;
        this.handle = registry.getTag(this.tag).orElseThrow();
    }

    public net.minecraft.core.HolderSet.Named<N> getHandle() {
        return handle;
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(tag.location());
    }
}
