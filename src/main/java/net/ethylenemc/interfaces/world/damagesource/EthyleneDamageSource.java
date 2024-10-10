package net.ethylenemc.interfaces.world.damagesource;

import net.minecraft.world.entity.Entity;

public interface EthyleneDamageSource {
    boolean isSweep();

    boolean isMelting();

    boolean isPoison();

    Entity getDamager();

    Entity getCausingDamager();

    org.bukkit.block.Block getDirectBlock();

    org.bukkit.block.BlockState getDirectBlockState();
}
