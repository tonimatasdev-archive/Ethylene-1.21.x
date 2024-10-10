package net.ethylenemc.interfaces.world.level;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

public interface EthyleneLevel {
    CraftWorld getWorld();

    CraftServer getCraftServer();

    ResourceKey<LevelStem> getTypeKey();
}
