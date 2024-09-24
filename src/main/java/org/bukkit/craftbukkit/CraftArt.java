package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import org.bukkit.Art;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public class CraftArt {

    public static Art minecraftToBukkit(net.minecraft.world.entity.decoration.PaintingVariant minecraft) {
        Preconditions.checkArgument(minecraft != null);

        net.minecraft.core.Registry<net.minecraft.world.entity.decoration.PaintingVariant> registry = CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.PAINTING_VARIANT);
        Art bukkit = Registry.ART.get(CraftNamespacedKey.fromMinecraft(registry.getResourceKey(minecraft).orElseThrow().location()));

        Preconditions.checkArgument(bukkit != null);

        return bukkit;
    }

    public static Art minecraftHolderToBukkit(net.minecraft.core.Holder<net.minecraft.world.entity.decoration.PaintingVariant> minecraft) {
        return minecraftToBukkit(minecraft.value());
    }

    public static net.minecraft.world.entity.decoration.PaintingVariant bukkitToMinecraft(Art bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.PAINTING_VARIANT)
                .getOptional(CraftNamespacedKey.toMinecraft(bukkit.getKey())).orElseThrow();
    }

    public static net.minecraft.core.Holder<net.minecraft.world.entity.decoration.PaintingVariant> bukkitToMinecraftHolder(Art bukkit) {
        Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<net.minecraft.world.entity.decoration.PaintingVariant> registry = CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.PAINTING_VARIANT);

        if (registry.wrapAsHolder(bukkitToMinecraft(bukkit)) instanceof net.minecraft.core.Holder.Reference<net.minecraft.world.entity.decoration.PaintingVariant> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own painting variant with out properly registering it.");
    }
}
