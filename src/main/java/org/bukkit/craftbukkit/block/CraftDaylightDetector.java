package org.bukkit.craftbukkit.block;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.DaylightDetector;

public class CraftDaylightDetector extends CraftBlockEntityState<net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity> implements DaylightDetector {

    public CraftDaylightDetector(World world, net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftDaylightDetector(CraftDaylightDetector state, Location location) {
        super(state, location);
    }

    @Override
    public CraftDaylightDetector copy() {
        return new CraftDaylightDetector(this, null);
    }

    @Override
    public CraftDaylightDetector copy(Location location) {
        return new CraftDaylightDetector(this, location);
    }
}
