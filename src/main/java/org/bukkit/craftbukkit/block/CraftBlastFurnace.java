package org.bukkit.craftbukkit.block;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlastFurnace;

public class CraftBlastFurnace extends CraftFurnace<net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity> implements BlastFurnace {

    public CraftBlastFurnace(World world, net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftBlastFurnace(CraftBlastFurnace state, Location location) {
        super(state, location);
    }

    @Override
    public CraftBlastFurnace copy() {
        return new CraftBlastFurnace(this, null);
    }

    @Override
    public CraftBlastFurnace copy(Location location) {
        return new CraftBlastFurnace(this, location);
    }
}
