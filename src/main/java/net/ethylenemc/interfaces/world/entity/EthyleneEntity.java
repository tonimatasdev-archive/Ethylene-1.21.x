package net.ethylenemc.interfaces.world.entity;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.projectiles.ProjectileSource;

public interface EthyleneEntity {
    CraftEntity getBukkitEntity();

    ProjectileSource getProjectileSource();
    
    void setProjectileSource(ProjectileSource value);

    void discard(EntityRemoveEvent.Cause cause);
    
    void setGeneration(boolean value);
    
    boolean getGeneration();
    
    boolean getValid();
    
    boolean getPersist();
    
    void setPersist(boolean value);
    
    boolean getVisibleByDefault();
    
    void setVisibleByDefault(boolean value);
    
    boolean getInWorld();
    
    float getBukkitYaw();
    
    void setPluginRemoved(boolean value);
    
    boolean isChunkLoaded();
    
    boolean saveAsPassenger(CompoundTag nbttagcompound, boolean includeAll);
}
