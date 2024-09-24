package org.bukkit.craftbukkit.block;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.SculkShrieker;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class CraftSculkShrieker extends CraftBlockEntityState<net.minecraft.world.level.block.entity.SculkShriekerBlockEntity> implements SculkShrieker {

    public CraftSculkShrieker(World world, net.minecraft.world.level.block.entity.SculkShriekerBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftSculkShrieker(CraftSculkShrieker state, Location location) {
        super(state, location);
    }

    @Override
    public int getWarningLevel() {
        return getSnapshot().warningLevel;
    }

    @Override
    public void setWarningLevel(int level) {
        getSnapshot().warningLevel = level;
    }

    @Override
    public void tryShriek(Player player) {
        requirePlaced();

        net.minecraft.server.level.ServerPlayer entityPlayer = (player == null) ? null : ((CraftPlayer) player).getHandle();
        getTileEntity().tryShriek(world.getHandle(), entityPlayer);
    }

    @Override
    public CraftSculkShrieker copy() {
        return new CraftSculkShrieker(this, null);
    }

    @Override
    public CraftSculkShrieker copy(Location location) {
        return new CraftSculkShrieker(this, location);
    }
}
