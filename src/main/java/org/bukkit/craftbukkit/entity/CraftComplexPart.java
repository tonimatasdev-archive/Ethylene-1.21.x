package org.bukkit.craftbukkit.entity;

import net.ethylenemc.interfaces.world.entity.EthyleneEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

public class CraftComplexPart extends CraftEntity implements ComplexEntityPart {
    public CraftComplexPart(CraftServer server, net.minecraft.world.entity.boss.EnderDragonPart entity) {
        super(server, entity);
    }

    @Override
    public ComplexLivingEntity getParent() {
        return (ComplexLivingEntity) ((EthyleneEntity) getHandle().parentMob).getBukkitEntity();
    }

    @Override
    public void setLastDamageCause(EntityDamageEvent cause) {
        getParent().setLastDamageCause(cause);
    }

    @Override
    public EntityDamageEvent getLastDamageCause() {
        return getParent().getLastDamageCause();
    }

    @Override
    public boolean isValid() {
        return getParent().isValid();
    }

    @Override
    public net.minecraft.world.entity.boss.EnderDragonPart getHandle() {
        return (net.minecraft.world.entity.boss.EnderDragonPart) entity;
    }

    @Override
    public String toString() {
        return "CraftComplexPart";
    }
}
