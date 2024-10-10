package net.ethylenemc.interfaces.server.level;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelStorageSource;

public interface EthyleneServerLevel {
    LevelStorageSource.LevelStorageAccess getConvertable();
    
    LevelChunk getChunkIfLoaded(int x, int z);
}
