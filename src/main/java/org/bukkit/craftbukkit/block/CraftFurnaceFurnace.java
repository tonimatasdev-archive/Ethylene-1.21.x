package org.bukkit.craftbukkit.block;

import org.bukkit.Location;
import org.bukkit.World;

public class CraftFurnaceFurnace extends CraftFurnace<net.minecraft.world.level.block.entity.FurnaceBlockEntity> {

    public CraftFurnaceFurnace(World world, net.minecraft.world.level.block.entity.FurnaceBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftFurnaceFurnace(CraftFurnaceFurnace state, Location location) {
        super(state, location);
    }

    @Override
    public CraftFurnaceFurnace copy() {
        return new CraftFurnaceFurnace(this, null);
    }

    @Override
    public CraftFurnaceFurnace copy(Location location) {
        return new CraftFurnaceFurnace(this, location);
    }
}
