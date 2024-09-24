package org.bukkit.craftbukkit.generator;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

public class CustomWorldChunkManager extends net.minecraft.world.level.biome.BiomeSource {

    private final WorldInfo worldInfo;
    private final BiomeProvider biomeProvider;
    private final net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> registry;

    private static List<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> biomeListToBiomeBaseList(List<Biome> biomes, net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> registry) {
        List<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> biomeBases = new ArrayList<>();

        for (Biome biome : biomes) {
            Preconditions.checkArgument(biome != Biome.CUSTOM, "Cannot use the biome %s", biome);
            biomeBases.add(CraftBiome.bukkitToMinecraftHolder(biome));
        }

        return biomeBases;
    }

    public CustomWorldChunkManager(WorldInfo worldInfo, BiomeProvider biomeProvider, net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> registry) {
        this.worldInfo = worldInfo;
        this.biomeProvider = biomeProvider;
        this.registry = registry;
    }

    @Override
    protected MapCodec<? extends net.minecraft.world.level.biome.BiomeSource> codec() {
        throw new UnsupportedOperationException("Cannot serialize CustomWorldChunkManager");
    }

    @Override
    public net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> getNoiseBiome(int x, int y, int z, net.minecraft.world.level.biome.Climate.Sampler sampler) {
        Biome biome = biomeProvider.getBiome(worldInfo, x << 2, y << 2, z << 2, CraftBiomeParameterPoint.createBiomeParameterPoint(sampler, sampler.sample(x, y, z)));
        Preconditions.checkArgument(biome != Biome.CUSTOM, "Cannot set the biome to %s", biome);

        return CraftBiome.bukkitToMinecraftHolder(biome);
    }

    @Override
    protected Stream<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> collectPossibleBiomes() {
        return biomeListToBiomeBaseList(biomeProvider.getBiomes(worldInfo), registry).stream();
    }
}
