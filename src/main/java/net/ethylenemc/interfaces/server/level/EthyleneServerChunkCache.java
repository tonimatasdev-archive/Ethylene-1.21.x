package net.ethylenemc.interfaces.server.level;

public interface EthyleneServerChunkCache {
    boolean isChunkLoaded(int chunkX, int chunkZ);

    void purgeUnload();
}
