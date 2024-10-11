package net.ethylenemc.interfaces.server.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

import java.util.UUID;

public interface EthyleneServerLevel {
    UUID uuid();
    
    LevelStorageSource.LevelStorageAccess getConvertable();
    
    LevelChunk getChunkIfLoaded(int x, int z);

    boolean strikeLightning(Entity entitylightning, LightningStrikeEvent.Cause cause);

    boolean addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason);

    boolean tryAddFreshEntityWithPassengers(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason);
}
