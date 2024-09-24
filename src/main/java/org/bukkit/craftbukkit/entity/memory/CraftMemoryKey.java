package org.bukkit.craftbukkit.entity.memory;

import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.memory.MemoryKey;

public final class CraftMemoryKey {

    private CraftMemoryKey() {}

    public static <T, U> MemoryKey<U> minecraftToBukkit(net.minecraft.world.entity.ai.memory.MemoryModuleType<T> minecraft) {
        if (minecraft == null) {
            return null;
        }

        net.minecraft.core.Registry<net.minecraft.world.entity.ai.memory.MemoryModuleType<?>> registry = CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.MEMORY_MODULE_TYPE);
        MemoryKey<U> bukkit = Registry.MEMORY_MODULE_TYPE.get(CraftNamespacedKey.fromMinecraft(registry.getResourceKey(minecraft).orElseThrow().location()));

        return bukkit;
    }

    public static <T, U> net.minecraft.world.entity.ai.memory.MemoryModuleType<U> bukkitToMinecraft(MemoryKey<T> bukkit) {
        if (bukkit == null) {
            return null;
        }

        return (net.minecraft.world.entity.ai.memory.MemoryModuleType<U>) CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.MEMORY_MODULE_TYPE)
                .getOptional(CraftNamespacedKey.toMinecraft(bukkit.getKey())).orElseThrow();
    }
}
