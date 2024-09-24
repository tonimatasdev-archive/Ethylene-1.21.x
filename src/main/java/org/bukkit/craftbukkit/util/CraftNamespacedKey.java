package org.bukkit.craftbukkit.util;

import org.bukkit.NamespacedKey;

public final class CraftNamespacedKey {

    public CraftNamespacedKey() {
    }

    public static NamespacedKey fromStringOrNull(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        net.minecraft.resources.ResourceLocation minecraft = net.minecraft.resources.ResourceLocation.tryParse(string);
        return (minecraft == null) ? null : fromMinecraft(minecraft);
    }

    public static NamespacedKey fromString(String string) {
        return fromMinecraft(net.minecraft.resources.ResourceLocation.parse(string));
    }

    public static NamespacedKey fromMinecraft(net.minecraft.resources.ResourceLocation minecraft) {
        return new NamespacedKey(minecraft.getNamespace(), minecraft.getPath());
    }

    public static net.minecraft.resources.ResourceLocation toMinecraft(NamespacedKey key) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey());
    }
}
