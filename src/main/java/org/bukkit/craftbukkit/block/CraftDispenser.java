package org.bukkit.craftbukkit.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.projectiles.CraftBlockProjectileSource;
import org.bukkit.inventory.Inventory;
import org.bukkit.projectiles.BlockProjectileSource;

public class CraftDispenser extends CraftLootable<net.minecraft.world.level.block.entity.DispenserBlockEntity> implements Dispenser {

    public CraftDispenser(World world, net.minecraft.world.level.block.entity.DispenserBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftDispenser(CraftDispenser state, Location location) {
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
    public BlockProjectileSource getBlockProjectileSource() {
        Block block = getBlock();

        if (block.getType() != Material.DISPENSER) {
            return null;
        }

        return new CraftBlockProjectileSource((net.minecraft.world.level.block.entity.DispenserBlockEntity) this.getTileEntityFromWorld());
    }

    @Override
    public boolean dispense() {
        ensureNoWorldGeneration();
        Block block = getBlock();
        if (block.getType() == Material.DISPENSER) {
            CraftWorld world = (CraftWorld) this.getWorld();
            net.minecraft.world.level.block.DispenserBlock dispense = (net.minecraft.world.level.block.DispenserBlock) net.minecraft.world.level.block.Blocks.DISPENSER;

            dispense.dispenseFrom(world.getHandle(), this.getHandle(), this.getPosition());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public CraftDispenser copy() {
        return new CraftDispenser(this, null);
    }

    @Override
    public CraftDispenser copy(Location location) {
        return new CraftDispenser(this, location);
    }
}
