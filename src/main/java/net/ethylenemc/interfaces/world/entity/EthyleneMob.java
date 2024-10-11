package net.ethylenemc.interfaces.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTransformEvent;

public interface EthyleneMob {
    boolean aware();
    
    void aware(boolean value);
    
    boolean setTarget(LivingEntity entityliving, EntityTargetEvent.TargetReason reason, boolean fireEvent);

    <T extends Mob> T convertTo(EntityType<T> entitytypes, boolean flag, EntityTransformEvent.TransformReason transformReason, CreatureSpawnEvent.SpawnReason spawnReason);

    void setPersistenceRequired(boolean persistenceRequired);
}
