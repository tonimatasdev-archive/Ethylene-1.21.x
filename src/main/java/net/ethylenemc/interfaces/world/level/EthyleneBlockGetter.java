package net.ethylenemc.interfaces.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;

public interface EthyleneBlockGetter {
    BlockHitResult clip(ClipContext raytrace1, BlockPos blockposition);
}
