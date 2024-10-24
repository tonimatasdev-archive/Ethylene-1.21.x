package net.ethylenemc.interfaces.world.level;

import net.minecraft.world.entity.Entity;

public interface EthyleneServerLevelAccessor {
    void addFreshEntityWithPassengers(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason);
}
