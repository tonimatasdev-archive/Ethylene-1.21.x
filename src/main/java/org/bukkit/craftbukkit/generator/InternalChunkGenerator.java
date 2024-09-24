package org.bukkit.craftbukkit.generator;

import java.util.function.Function;

// Do not implement functions to this class, add to NormalChunkGenerator
public abstract class InternalChunkGenerator extends net.minecraft.world.level.chunk.ChunkGenerator {

    public InternalChunkGenerator(net.minecraft.world.level.biome.BiomeSource worldchunkmanager, Function<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>, net.minecraft.world.level.biome.BiomeGenerationSettings> function) {
        super(worldchunkmanager, function);
    }
}
