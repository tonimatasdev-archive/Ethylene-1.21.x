package net.ethylenemc.interfaces.world.entity.projectile;

import net.minecraft.world.entity.Entity;

public interface EthyleneShulkerBullet {
    Entity getTarget();

    void setTarget(Entity e);
}
