package org.bukkit.craftbukkit.block;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Comparator;

public class CraftComparator extends CraftBlockEntityState<net.minecraft.world.level.block.entity.ComparatorBlockEntity> implements Comparator {

    public CraftComparator(World world, net.minecraft.world.level.block.entity.ComparatorBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftComparator(CraftComparator state, Location location) {
        super(state, location);
    }

    @Override
    public CraftComparator copy() {
        return new CraftComparator(this, null);
    }

    @Override
    public CraftComparator copy(Location location) {
        return new CraftComparator(this, location);
    }
}
