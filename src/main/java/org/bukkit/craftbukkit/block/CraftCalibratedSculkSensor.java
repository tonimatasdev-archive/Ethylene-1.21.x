package org.bukkit.craftbukkit.block;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CalibratedSculkSensor;

public class CraftCalibratedSculkSensor extends CraftSculkSensor<net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity> implements CalibratedSculkSensor {

    public CraftCalibratedSculkSensor(World world, net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftCalibratedSculkSensor(CraftCalibratedSculkSensor state, Location location) {
        super(state, location);
    }

    @Override
    public CraftCalibratedSculkSensor copy() {
        return new CraftCalibratedSculkSensor(this, null);
    }

    @Override
    public CraftCalibratedSculkSensor copy(Location location) {
        return new CraftCalibratedSculkSensor(this, location);
    }
}
