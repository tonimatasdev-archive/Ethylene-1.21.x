package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;

import net.ethylenemc.interfaces.world.entity.EthyleneEntity;
import net.ethylenemc.interfaces.world.level.block.entity.EthyleneBeehiveBlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Beehive;
import org.bukkit.craftbukkit.entity.CraftBee;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Bee;

public class CraftBeehive extends CraftBlockEntityState<net.minecraft.world.level.block.entity.BeehiveBlockEntity> implements Beehive {

    public CraftBeehive(World world, net.minecraft.world.level.block.entity.BeehiveBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftBeehive(CraftBeehive state, Location location) {
        super(state, location);
    }

    @Override
    public Location getFlower() {
        net.minecraft.core.BlockPos flower = getSnapshot().savedFlowerPos;
        return (flower == null) ? null : CraftLocation.toBukkit(flower, getWorld());
    }

    @Override
    public void setFlower(Location location) {
        Preconditions.checkArgument(location == null || this.getWorld().equals(location.getWorld()), "Flower must be in same world");
        getSnapshot().savedFlowerPos = (location == null) ? null : CraftLocation.toBlockPosition(location);
    }

    @Override
    public boolean isFull() {
        return getSnapshot().isFull();
    }

    @Override
    public boolean isSedated() {
        return isPlaced() && getTileEntity().isSedated();
    }

    @Override
    public int getEntityCount() {
        return getSnapshot().getOccupantCount();
    }

    @Override
    public int getMaxEntities() {
        return ((EthyleneBeehiveBlockEntity) getSnapshot()).getMaxBees();
    }

    @Override
    public void setMaxEntities(int max) {
        Preconditions.checkArgument(max > 0, "Max bees must be more than 0");

        ((EthyleneBeehiveBlockEntity) getSnapshot()).setMaxBees(max);
    }

    @Override
    public List<Bee> releaseEntities() {
        ensureNoWorldGeneration();

        List<Bee> bees = new ArrayList<>();

        if (isPlaced()) {
            net.minecraft.world.level.block.entity.BeehiveBlockEntity beehive = ((net.minecraft.world.level.block.entity.BeehiveBlockEntity) this.getTileEntityFromWorld());
            for (net.minecraft.world.entity.Entity bee : ((EthyleneBeehiveBlockEntity) beehive).releaseBees(this.getHandle(), net.minecraft.world.level.block.entity.BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED, true)) {
                bees.add((Bee) ((EthyleneEntity) bee).getBukkitEntity());
            }
        }

        return bees;
    }

    @Override
    public void addEntity(Bee entity) {
        Preconditions.checkArgument(entity != null, "net.minecraft.world.entity.Entity must not be null");

        getSnapshot().addOccupant(((CraftBee) entity).getHandle());
    }

    @Override
    public CraftBeehive copy() {
        return new CraftBeehive(this, null);
    }

    @Override
    public CraftBeehive copy(Location location) {
        return new CraftBeehive(this, location);
    }
}
