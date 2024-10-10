package org.bukkit.craftbukkit.util;


import net.ethylenemc.interfaces.server.level.EthyleneServerLevel;
import net.ethylenemc.interfaces.world.level.EthyleneLevel;

public class CraftDimensionUtil {

    private CraftDimensionUtil() {
    }

    public static net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getMainDimensionKey(net.minecraft.world.level.Level world) {
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.dimension.LevelStem> typeKey = ((EthyleneLevel) world).getTypeKey();
        if (typeKey == net.minecraft.world.level.dimension.LevelStem.OVERWORLD) {
            return net.minecraft.world.level.Level.OVERWORLD;
        } else if (typeKey == net.minecraft.world.level.dimension.LevelStem.NETHER) {
            return net.minecraft.world.level.Level.NETHER;
        } else if (typeKey == net.minecraft.world.level.dimension.LevelStem.END) {
            return net.minecraft.world.level.Level.END;
        }

        return world.dimension();
    }
}
