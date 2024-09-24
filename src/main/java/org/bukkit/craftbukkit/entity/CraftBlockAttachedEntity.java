package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;

public class CraftBlockAttachedEntity extends CraftEntity {
    public CraftBlockAttachedEntity(CraftServer server, net.minecraft.world.entity.decoration.BlockAttachedEntity entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.decoration.BlockAttachedEntity getHandle() {
        return (net.minecraft.world.entity.decoration.BlockAttachedEntity) entity;
    }

    @Override
    public String toString() {
        return "CraftBlockAttachedEntity";
    }
}
