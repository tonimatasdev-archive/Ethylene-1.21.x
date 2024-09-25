package net.ethylenemc.interfaces.world.level;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

public interface EthyleneLevel {
    CraftWorld getWorld();

    CraftServer getCraftServer();
}
