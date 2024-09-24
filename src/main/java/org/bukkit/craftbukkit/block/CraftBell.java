package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Bell;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class CraftBell extends CraftBlockEntityState<net.minecraft.world.level.block.entity.BellBlockEntity> implements Bell {

    public CraftBell(World world, net.minecraft.world.level.block.entity.BellBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftBell(CraftBell state, Location location) {
        super(state, location);
    }

    @Override
    public boolean ring(Entity entity, BlockFace direction) {
        Preconditions.checkArgument(direction == null || direction.isCartesian(), "direction must be cartesian, given %s", direction);

        net.minecraft.world.level.block.entity.BlockEntity tileEntity = getTileEntityFromWorld();
        if (tileEntity == null) {
            return false;
        }

        net.minecraft.world.entity.Entity nmsEntity = (entity != null) ? ((CraftEntity) entity).getHandle() : null;
        net.minecraft.core.Direction enumDirection = CraftBlock.blockFaceToNotch(direction);

        return ((net.minecraft.world.level.block.BellBlock) net.minecraft.world.level.block.Blocks.BELL).attemptToRing(nmsEntity, world.getHandle(), getPosition(), enumDirection);
    }

    @Override
    public boolean ring(Entity entity) {
        return ring(entity, null);
    }

    @Override
    public boolean ring(BlockFace direction) {
        return ring(null, direction);
    }

    @Override
    public boolean ring() {
        return ring(null, null);
    }

    @Override
    public boolean isShaking() {
        return getSnapshot().shaking;
    }

    @Override
    public int getShakingTicks() {
        return getSnapshot().ticks;
    }

    @Override
    public boolean isResonating() {
        return getSnapshot().resonating;
    }

    @Override
    public int getResonatingTicks() {
        return isResonating() ? getSnapshot().ticks : 0;
    }

    @Override
    public CraftBell copy() {
        return new CraftBell(this, null);
    }

    @Override
    public CraftBell copy(Location location) {
        return new CraftBell(this, location);
    }
}
