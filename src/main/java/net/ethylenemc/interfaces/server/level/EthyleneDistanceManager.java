package net.ethylenemc.interfaces.server.level;

import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

public interface EthyleneDistanceManager {
    <T> boolean addRegionTicketAtDistance(TicketType<T> tickettype, ChunkPos chunkcoordintpair, int i, T t0);

    <T> boolean removeRegionTicketAtDistance(TicketType<T> tickettype, ChunkPos chunkcoordintpair, int i, T t0);

    <T> void removeAllTicketsFor(TicketType<T> ticketType, int ticketLevel, T ticketIdentifier);
}
