package org.bukkit.craftbukkit.entity;

import net.ethylenemc.interfaces.world.entity.EthyleneEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;

public abstract class CraftProjectile extends AbstractProjectile implements Projectile {
    public CraftProjectile(CraftServer server, net.minecraft.world.entity.projectile.Projectile entity) {
        super(server, entity);
    }

    @Override
    public ProjectileSource getShooter() {
        return ((EthyleneEntity) getHandle()).getProjectileSource();
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        if (shooter instanceof CraftLivingEntity) {
            getHandle().setOwner((net.minecraft.world.entity.LivingEntity) ((CraftLivingEntity) shooter).entity);
        } else {
            getHandle().setOwner(null);
        }
        ((EthyleneEntity) getHandle()).setProjectileSource(shooter);
    }

    @Override
    public net.minecraft.world.entity.projectile.Projectile getHandle() {
        return (net.minecraft.world.entity.projectile.Projectile) entity;
    }

    @Override
    public String toString() {
        return "CraftProjectile";
    }
}
