package org.bukkit.craftbukkit;

import org.bukkit.FluidCollisionMode;

public final class CraftFluidCollisionMode {

    private CraftFluidCollisionMode() {}

    public static net.minecraft.world.level.ClipContext.Fluid toNMS(FluidCollisionMode fluidCollisionMode) {
        if (fluidCollisionMode == null) return null;

        switch (fluidCollisionMode) {
            case ALWAYS:
                return net.minecraft.world.level.ClipContext.Fluid.ANY;
            case SOURCE_ONLY:
                return net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY;
            case NEVER:
                return net.minecraft.world.level.ClipContext.Fluid.NONE;
            default:
                return null;
        }
    }
}
