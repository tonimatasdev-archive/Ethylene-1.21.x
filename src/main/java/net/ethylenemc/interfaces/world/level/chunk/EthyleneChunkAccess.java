package net.ethylenemc.interfaces.world.level.chunk;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.craftbukkit.persistence.DirtyCraftPersistentDataContainer;

public interface EthyleneChunkAccess {
    DirtyCraftPersistentDataContainer getPersistentDataContainer();
    
    void setBiome(int i, int j, int k, Holder<Biome> biome);
}
