package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public class CraftBiome {

    public static Biome minecraftToBukkit(net.minecraft.world.level.biome.Biome minecraft) {
        Preconditions.checkArgument(minecraft != null);

        net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> registry = CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.BIOME);
        Biome bukkit = Registry.BIOME.get(CraftNamespacedKey.fromMinecraft(registry.getResourceKey(minecraft).orElseThrow().location()));

        if (bukkit == null) {
            return Biome.CUSTOM;
        }

        return bukkit;
    }

    public static Biome minecraftHolderToBukkit(net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> minecraft) {
        return minecraftToBukkit(minecraft.value());
    }

    public static net.minecraft.world.level.biome.Biome bukkitToMinecraft(Biome bukkit) {
        if (bukkit == null || bukkit == Biome.CUSTOM) {
            return null;
        }

        return CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.BIOME)
                .getOptional(CraftNamespacedKey.toMinecraft(bukkit.getKey())).orElseThrow();
    }

    public static net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> bukkitToMinecraftHolder(Biome bukkit) {
        if (bukkit == null || bukkit == Biome.CUSTOM) {
            return null;
        }

        net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> registry = CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.BIOME);

        if (registry.wrapAsHolder(bukkitToMinecraft(bukkit)) instanceof net.minecraft.core.Holder.Reference<net.minecraft.world.level.biome.Biome> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own biome base with out properly registering it.");
    }
}
