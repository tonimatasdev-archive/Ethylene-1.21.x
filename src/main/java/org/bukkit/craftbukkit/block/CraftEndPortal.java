package org.bukkit.craftbukkit.block;

import org.bukkit.Location;
import org.bukkit.World;

public class CraftEndPortal extends CraftBlockEntityState<net.minecraft.world.level.block.entity.TheEndPortalBlockEntity> {

    public CraftEndPortal(World world, net.minecraft.world.level.block.entity.TheEndPortalBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftEndPortal(CraftEndPortal state, Location location) {
        super(state, location);
    }

    @Override
    public CraftEndPortal copy() {
        return new CraftEndPortal(this, null);
    }

    @Override
    public CraftEndPortal copy(Location location) {
        return new CraftEndPortal(this, location);
    }
}
