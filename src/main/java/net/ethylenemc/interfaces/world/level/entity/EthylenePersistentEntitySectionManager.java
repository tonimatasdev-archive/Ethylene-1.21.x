package net.ethylenemc.interfaces.world.level.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

import java.io.IOException;
import java.util.List;

public interface EthylenePersistentEntitySectionManager {
    List<Entity> getEntities(ChunkPos chunkCoordIntPair);

    boolean isPending(long pair);

    void close(boolean save) throws IOException;
}
