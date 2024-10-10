package org.bukkit.craftbukkit.entity;

import net.ethylenemc.interfaces.world.entity.vehicle.EthyleneAbstractMinecart;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Minecart;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public abstract class CraftMinecart extends CraftVehicle implements Minecart {
    public CraftMinecart(CraftServer server, net.minecraft.world.entity.vehicle.AbstractMinecart entity) {
        super(server, entity);
    }

    @Override
    public void setDamage(double damage) {
        getHandle().setDamage((float) damage);
    }

    @Override
    public double getDamage() {
        return getHandle().getDamage();
    }

    @Override
    public double getMaxSpeed() {
        return ((EthyleneAbstractMinecart) getHandle()).maxSpeed();
    }

    @Override
    public void setMaxSpeed(double speed) {
        if (speed >= 0D) {
            ((EthyleneAbstractMinecart) getHandle()).maxSpeed(speed);
        }
    }

    @Override
    public boolean isSlowWhenEmpty() {
        return ((EthyleneAbstractMinecart) getHandle()).slowWhenEmpty();
    }

    @Override
    public void setSlowWhenEmpty(boolean slow) {
        ((EthyleneAbstractMinecart) getHandle()).slowWhenEmpty(slow);
    }

    @Override
    public Vector getFlyingVelocityMod() {
        return ((EthyleneAbstractMinecart) getHandle()).getFlyingVelocityMod();
    }

    @Override
    public void setFlyingVelocityMod(Vector flying) {
        ((EthyleneAbstractMinecart) getHandle()).setFlyingVelocityMod(flying);
    }

    @Override
    public Vector getDerailedVelocityMod() {
        return ((EthyleneAbstractMinecart) getHandle()).getDerailedVelocityMod();
    }

    @Override
    public void setDerailedVelocityMod(Vector derailed) {
        ((EthyleneAbstractMinecart) getHandle()).setDerailedVelocityMod(derailed);
    }

    @Override
    public net.minecraft.world.entity.vehicle.AbstractMinecart getHandle() {
        return (net.minecraft.world.entity.vehicle.AbstractMinecart) entity;
    }

    @Override
    public void setDisplayBlock(MaterialData material) {
        if (material != null) {
            net.minecraft.world.level.block.state.BlockState block = CraftMagicNumbers.getBlock(material);
            this.getHandle().setDisplayBlockState(block);
        } else {
            // Set block to air (default) and set the flag to not have a display block.
            this.getHandle().setDisplayBlockState(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            this.getHandle().setCustomDisplay(false);
        }
    }

    @Override
    public void setDisplayBlockData(BlockData blockData) {
        if (blockData != null) {
            net.minecraft.world.level.block.state.BlockState block = ((CraftBlockData) blockData).getState();
            this.getHandle().setDisplayBlockState(block);
        } else {
            // Set block to air (default) and set the flag to not have a display block.
            this.getHandle().setDisplayBlockState(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            this.getHandle().setCustomDisplay(false);
        }
    }

    @Override
    public MaterialData getDisplayBlock() {
        net.minecraft.world.level.block.state.BlockState blockData = getHandle().getDisplayBlockState();
        return CraftMagicNumbers.getMaterial(blockData);
    }

    @Override
    public BlockData getDisplayBlockData() {
        net.minecraft.world.level.block.state.BlockState blockData = getHandle().getDisplayBlockState();
        return CraftBlockData.fromData(blockData);
    }

    @Override
    public void setDisplayBlockOffset(int offset) {
        getHandle().setDisplayOffset(offset);
    }

    @Override
    public int getDisplayBlockOffset() {
        return getHandle().getDisplayOffset();
    }
}
