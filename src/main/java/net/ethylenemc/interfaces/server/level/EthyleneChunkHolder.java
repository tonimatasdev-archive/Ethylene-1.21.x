package net.ethylenemc.interfaces.server.level;

import net.minecraft.world.level.chunk.LevelChunk;

public interface EthyleneChunkHolder {
    LevelChunk getFullChunkNow();
}
