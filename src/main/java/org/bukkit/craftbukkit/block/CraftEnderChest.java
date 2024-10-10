package org.bukkit.craftbukkit.block;

import net.ethylenemc.interfaces.world.level.block.entity.EthyleneContainerOpenersCounter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.EnderChest;

public class CraftEnderChest extends CraftBlockEntityState<net.minecraft.world.level.block.entity.EnderChestBlockEntity> implements EnderChest {

    public CraftEnderChest(World world, net.minecraft.world.level.block.entity.EnderChestBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftEnderChest(CraftEnderChest state, Location location) {
        super(state, location);
    }

    @Override
    public void open() {
        requirePlaced();
        if (!((EthyleneContainerOpenersCounter) getTileEntity().openersCounter).getOpened() && getWorldHandle() instanceof net.minecraft.world.level.Level) {
            net.minecraft.world.level.block.state.BlockState block = getTileEntity().getBlockState();
            int openCount = getTileEntity().openersCounter.getOpenerCount();

            ((EthyleneContainerOpenersCounter) getTileEntity().openersCounter).onAPIOpen((net.minecraft.world.level.Level) getWorldHandle(), getPosition(), block);
            ((EthyleneContainerOpenersCounter) getTileEntity().openersCounter).openerAPICountChanged((net.minecraft.world.level.Level) getWorldHandle(), getPosition(), block, openCount, openCount + 1);
        }
        ((EthyleneContainerOpenersCounter) getTileEntity().openersCounter).setOpened(true);
    }

    @Override
    public void close() {
        requirePlaced();
        if (((EthyleneContainerOpenersCounter) getTileEntity().openersCounter).getOpened() && getWorldHandle() instanceof net.minecraft.world.level.Level) {
            net.minecraft.world.level.block.state.BlockState block = getTileEntity().getBlockState();
            int openCount = getTileEntity().openersCounter.getOpenerCount();

            ((EthyleneContainerOpenersCounter) getTileEntity().openersCounter).onAPIClose((net.minecraft.world.level.Level) getWorldHandle(), getPosition(), block);
            ((EthyleneContainerOpenersCounter) getTileEntity().openersCounter).openerAPICountChanged((net.minecraft.world.level.Level) getWorldHandle(), getPosition(), block, openCount, 0);
        }
        ((EthyleneContainerOpenersCounter) getTileEntity().openersCounter).setOpened(false);
    }

    @Override
    public CraftEnderChest copy() {
        return new CraftEnderChest(this, null);
    }

    @Override
    public CraftEnderChest copy(Location location) {
        return new CraftEnderChest(this, location);
    }
}
