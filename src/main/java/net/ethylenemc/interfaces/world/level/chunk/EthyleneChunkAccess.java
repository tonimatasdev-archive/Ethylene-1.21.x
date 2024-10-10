package net.ethylenemc.interfaces.world.level.chunk;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public interface EthyleneChunkAccess {
    void setBiome(int i, int j, int k, Holder<Biome> biome);
}
