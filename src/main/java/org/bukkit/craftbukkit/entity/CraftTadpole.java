package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;

public class CraftTadpole extends CraftFish implements org.bukkit.entity.Tadpole {

    public CraftTadpole(CraftServer server, net.minecraft.world.entity.animal.frog.Tadpole entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.frog.Tadpole getHandle() {
        return (net.minecraft.world.entity.animal.frog.Tadpole) entity;
    }

    @Override
    public String toString() {
        return "CraftTadpole";
    }

    @Override
    public int getAge() {
        return getHandle().age;
    }

    @Override
    public void setAge(int age) {
        getHandle().age = age;
    }
}
