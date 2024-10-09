package org.bukkit.craftbukkit.block;

import java.util.ArrayList;
import java.util.Collection;

import net.ethylenemc.interfaces.world.entity.EthyleneEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Conduit;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;

public class CraftConduit extends CraftBlockEntityState<net.minecraft.world.level.block.entity.ConduitBlockEntity> implements Conduit {

    public CraftConduit(World world, net.minecraft.world.level.block.entity.ConduitBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftConduit(CraftConduit state, Location location) {
        super(state, location);
    }

    @Override
    public CraftConduit copy() {
        return new CraftConduit(this, null);
    }

    @Override
    public CraftConduit copy(Location location) {
        return new CraftConduit(this, location);
    }

    @Override
    public boolean isActive() {
        ensureNoWorldGeneration();
        net.minecraft.world.level.block.entity.ConduitBlockEntity conduit = (net.minecraft.world.level.block.entity.ConduitBlockEntity) getTileEntityFromWorld();
        return conduit != null && conduit.isActive();
    }

    @Override
    public boolean isHunting() {
        ensureNoWorldGeneration();
        net.minecraft.world.level.block.entity.ConduitBlockEntity conduit = (net.minecraft.world.level.block.entity.ConduitBlockEntity) getTileEntityFromWorld();
        return conduit != null && conduit.isHunting();
    }

    @Override
    public Collection<Block> getFrameBlocks() {
        ensureNoWorldGeneration();
        Collection<Block> blocks = new ArrayList<>();

        net.minecraft.world.level.block.entity.ConduitBlockEntity conduit = (net.minecraft.world.level.block.entity.ConduitBlockEntity) getTileEntityFromWorld();
        if (conduit != null) {
            for (net.minecraft.core.BlockPos position : conduit.effectBlocks) {
                blocks.add(CraftBlock.at(getWorldHandle(), position));
            }
        }

        return blocks;
    }

    @Override
    public int getFrameBlockCount() {
        ensureNoWorldGeneration();
        net.minecraft.world.level.block.entity.ConduitBlockEntity conduit = (net.minecraft.world.level.block.entity.ConduitBlockEntity) getTileEntityFromWorld();
        return (conduit != null) ? conduit.effectBlocks.size() : 0;
    }

    @Override
    public int getRange() {
        ensureNoWorldGeneration();
        net.minecraft.world.level.block.entity.ConduitBlockEntity conduit = (net.minecraft.world.level.block.entity.ConduitBlockEntity) getTileEntityFromWorld();
        return (conduit != null) ? net.minecraft.world.level.block.entity.ConduitBlockEntity.getRange(conduit.effectBlocks) : 0;
    }

    @Override
    public boolean setTarget(LivingEntity target) {
        net.minecraft.world.level.block.entity.ConduitBlockEntity conduit = (net.minecraft.world.level.block.entity.ConduitBlockEntity) getTileEntityFromWorld();
        if (conduit == null) {
            return false;
        }

        net.minecraft.world.entity.LivingEntity currentTarget = conduit.destroyTarget;

        if (target == null) {
            if (currentTarget == null) {
                return false;
            }

            conduit.destroyTarget = null;
            conduit.destroyTargetUUID = null;
        } else {
            if (currentTarget != null && target.getUniqueId().equals(currentTarget.getUUID())) {
                return false;
            }

            conduit.destroyTarget = ((CraftLivingEntity) target).getHandle();
            conduit.destroyTargetUUID = target.getUniqueId();
        }

        net.minecraft.world.level.block.entity.ConduitBlockEntity.updateDestroyTarget(conduit.getLevel(), getPosition(), data, conduit.effectBlocks, conduit, false);
        return true;
    }

    @Override
    public LivingEntity getTarget() {
        net.minecraft.world.level.block.entity.ConduitBlockEntity conduit = (net.minecraft.world.level.block.entity.ConduitBlockEntity) getTileEntityFromWorld();
        if (conduit == null) {
            return null;
        }

        net.minecraft.world.entity.LivingEntity nmsEntity = conduit.destroyTarget;
        return (nmsEntity != null) ? (LivingEntity) ((EthyleneEntity) nmsEntity).getBukkitEntity() : null;
    }

    @Override
    public boolean hasTarget() {
        net.minecraft.world.level.block.entity.ConduitBlockEntity conduit = (net.minecraft.world.level.block.entity.ConduitBlockEntity) getTileEntityFromWorld();
        return conduit != null && conduit.destroyTarget != null && conduit.destroyTarget.isAlive();
    }

    @Override
    public BoundingBox getHuntingArea() {
        net.minecraft.world.phys.AABB bounds = net.minecraft.world.level.block.entity.ConduitBlockEntity.getDestroyRangeAABB(getPosition());
        return new BoundingBox(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ);
    }
}
