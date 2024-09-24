package org.bukkit.craftbukkit.block;

import org.bukkit.Location;
import org.bukkit.World;

public class CraftMovingPiston extends CraftBlockEntityState<net.minecraft.world.level.block.piston.PistonMovingBlockEntity> {

    public CraftMovingPiston(World world, net.minecraft.world.level.block.piston.PistonMovingBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftMovingPiston(CraftMovingPiston state, Location location) {
        super(state, location);
    }

    @Override
    public CraftMovingPiston copy() {
        return new CraftMovingPiston(this, null);
    }

    @Override
    public CraftMovingPiston copy(Location location) {
        return new CraftMovingPiston(this, location);
    }
}
