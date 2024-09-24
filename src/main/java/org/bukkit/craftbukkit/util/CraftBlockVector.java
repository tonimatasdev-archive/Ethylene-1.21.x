package org.bukkit.craftbukkit.util;

import org.bukkit.util.BlockVector;

public final class CraftBlockVector {

    private CraftBlockVector() {
    }

    public static net.minecraft.core.BlockPos toBlockPosition(BlockVector blockVector) {
        return new net.minecraft.core.BlockPos(blockVector.getBlockX(), blockVector.getBlockY(), blockVector.getBlockZ());
    }

    public static BlockVector toBukkit(net.minecraft.core.Vec3i baseBlockPosition) {
        return new BlockVector(baseBlockPosition.getX(), baseBlockPosition.getY(), baseBlockPosition.getZ());
    }
}
