package net.ethylenemc.interfaces.world.entity;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.projectiles.ProjectileSource;

public interface EthyleneEntity {
    CraftEntity getBukkitEntity();

    ProjectileSource getProjectileSource();
    
    void setProjectileSource(ProjectileSource value);

    void discard(EntityRemoveEvent.Cause cause);
}
