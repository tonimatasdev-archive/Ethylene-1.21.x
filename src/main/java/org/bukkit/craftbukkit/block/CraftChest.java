package org.bukkit.craftbukkit.block;

import net.ethylenemc.interfaces.EthyleneDoubleInventory;
import net.ethylenemc.interfaces.world.level.block.entity.EthyleneContainerOpenersCounter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.inventory.Inventory;

public class CraftChest extends CraftLootable<net.minecraft.world.level.block.entity.ChestBlockEntity> implements Chest {

    public CraftChest(World world, net.minecraft.world.level.block.entity.ChestBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftChest(CraftChest state, Location location) {
        super(state, location);
    }

    @Override
    public Inventory getSnapshotInventory() {
        return new CraftInventory(this.getSnapshot());
    }

    @Override
    public Inventory getBlockInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }

        return new CraftInventory(this.getTileEntity());
    }

    @Override
    public Inventory getInventory() {
        CraftInventory inventory = (CraftInventory) this.getBlockInventory();
        if (!isPlaced() || isWorldGeneration()) {
            return inventory;
        }

        // The logic here is basically identical to the logic in net.minecraft.world.level.block.ChestBlock.interact
        CraftWorld world = (CraftWorld) this.getWorld();

        net.minecraft.world.level.block.ChestBlock blockChest = (net.minecraft.world.level.block.ChestBlock) (this.getType() == Material.CHEST ? net.minecraft.world.level.block.Blocks.CHEST : net.minecraft.world.level.block.Blocks.TRAPPED_CHEST);
        net.minecraft.world.MenuProvider nms = blockChest.getMenuProvider(data, world.getHandle(), this.getPosition(), true);

        if (nms instanceof EthyleneDoubleInventory) {
            inventory = new CraftInventoryDoubleChest((EthyleneDoubleInventory) nms);
        }
        return inventory;
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
    public CraftChest copy() {
        return new CraftChest(this, null);
    }

    @Override
    public CraftChest copy(Location location) {
        return new CraftChest(this, location);
    }
}
