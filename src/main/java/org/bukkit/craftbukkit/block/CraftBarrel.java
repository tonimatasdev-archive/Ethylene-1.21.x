package org.bukkit.craftbukkit.block;

import net.ethylenemc.interfaces.world.level.block.entity.EthyleneContainerOpenersCounter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;

public class CraftBarrel extends CraftLootable<net.minecraft.world.level.block.entity.BarrelBlockEntity> implements Barrel {

    public CraftBarrel(World world, net.minecraft.world.level.block.entity.BarrelBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftBarrel(CraftBarrel state, Location location) {
        super(state, location);
    }

    @Override
    public Inventory getSnapshotInventory() {
        return new CraftInventory(this.getSnapshot());
    }

    @Override
    public Inventory getInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }

        return new CraftInventory(this.getTileEntity());
    }

    @Override
    public void open() {
        requirePlaced();
        if (!((EthyleneContainerOpenersCounter) getTileEntity().openersCounter).getOpened()) {
            net.minecraft.world.level.block.state.BlockState blockData = getTileEntity().getBlockState();
            boolean open = blockData.getValue(net.minecraft.world.level.block.BarrelBlock.OPEN);

            if (!open) {
                getTileEntity().updateBlockState(blockData, true);
                if (getWorldHandle() instanceof net.minecraft.world.level.Level) {
                    getTileEntity().playSound(blockData, net.minecraft.sounds.SoundEvents.BARREL_OPEN);
                }
            }
        }
        ((EthyleneContainerOpenersCounter) getTileEntity().openersCounter).setOpened(true);
    }

    @Override
    public void close() {
        requirePlaced();
        if (((EthyleneContainerOpenersCounter) getTileEntity().openersCounter).getOpened()) {
            net.minecraft.world.level.block.state.BlockState blockData = getTileEntity().getBlockState();
            getTileEntity().updateBlockState(blockData, false);
            if (getWorldHandle() instanceof net.minecraft.world.level.Level) {
                getTileEntity().playSound(blockData, net.minecraft.sounds.SoundEvents.BARREL_CLOSE);
            }
        }
        ((EthyleneContainerOpenersCounter) getTileEntity().openersCounter).setOpened(false);
    }

    @Override
    public CraftBarrel copy() {
        return new CraftBarrel(this, null);
    }

    @Override
    public CraftBarrel copy(Location location) {
        return new CraftBarrel(this, location);
    }
}
