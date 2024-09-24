package org.bukkit.craftbukkit.block;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Smoker;

public class CraftSmoker extends CraftFurnace<net.minecraft.world.level.block.entity.SmokerBlockEntity> implements Smoker {

    public CraftSmoker(World world, net.minecraft.world.level.block.entity.SmokerBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftSmoker(CraftSmoker state, Location location) {
        super(state, location);
    }

    @Override
    public CraftSmoker copy() {
        return new CraftSmoker(this, null);
    }

    @Override
    public CraftSmoker copy(Location location) {
        return new CraftSmoker(this, location);
    }
}
