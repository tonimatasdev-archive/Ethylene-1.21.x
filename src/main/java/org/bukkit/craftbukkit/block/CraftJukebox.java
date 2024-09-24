package org.bukkit.craftbukkit.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Jukebox;
import org.bukkit.craftbukkit.inventory.CraftInventoryJukebox;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.inventory.JukeboxInventory;

public class CraftJukebox extends CraftBlockEntityState<net.minecraft.world.level.block.entity.JukeboxBlockEntity> implements Jukebox {

    public CraftJukebox(World world, net.minecraft.world.level.block.entity.JukeboxBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftJukebox(CraftJukebox state, Location location) {
        super(state, location);
    }

    @Override
    public JukeboxInventory getSnapshotInventory() {
        return new CraftInventoryJukebox(this.getSnapshot());
    }

    @Override
    public JukeboxInventory getInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }

        return new CraftInventoryJukebox(this.getTileEntity());
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);

        if (result && this.isPlaced() && this.getType() == Material.JUKEBOX) {
            getWorldHandle().setBlock(this.getPosition(), data, 3);

            net.minecraft.world.level.block.entity.BlockEntity tileEntity = this.getTileEntityFromWorld();
            if (tileEntity instanceof net.minecraft.world.level.block.entity.JukeboxBlockEntity jukebox) {
                jukebox.setTheItem(jukebox.getTheItem());
            }
        }

        return result;
    }

    @Override
    public Material getPlaying() {
        return getRecord().getType();
    }

    @Override
    public void setPlaying(Material record) {
        if (record == null || CraftItemType.bukkitToMinecraft(record) == null) {
            record = Material.AIR;
        }

        setRecord(new org.bukkit.inventory.ItemStack(record));
    }

    @Override
    public boolean hasRecord() {
        return getHandle().getValue(net.minecraft.world.level.block.JukeboxBlock.HAS_RECORD) && !getPlaying().isAir();
    }

    @Override
    public org.bukkit.inventory.ItemStack getRecord() {
        net.minecraft.world.item.ItemStack record = this.getSnapshot().getTheItem();
        return CraftItemStack.asBukkitCopy(record);
    }

    @Override
    public void setRecord(org.bukkit.inventory.ItemStack record) {
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(record);

        net.minecraft.world.level.block.entity.JukeboxBlockEntity snapshot = this.getSnapshot();
        snapshot.setSongItemWithoutPlaying(nms, snapshot.getSongPlayer().getTicksSinceSongStarted());

        this.data = this.data.setValue(net.minecraft.world.level.block.JukeboxBlock.HAS_RECORD, !nms.isEmpty());
    }

    @Override
    public boolean isPlaying() {
        requirePlaced();

        net.minecraft.world.level.block.entity.BlockEntity tileEntity = this.getTileEntityFromWorld();
        return tileEntity instanceof net.minecraft.world.level.block.entity.JukeboxBlockEntity jukebox && jukebox.getSongPlayer().isPlaying();
    }

    @Override
    public boolean startPlaying() {
        requirePlaced();

        net.minecraft.world.level.block.entity.BlockEntity tileEntity = this.getTileEntityFromWorld();
        if (!(tileEntity instanceof net.minecraft.world.level.block.entity.JukeboxBlockEntity jukebox)) {
            return false;
        }

        net.minecraft.world.item.ItemStack record = jukebox.getTheItem();
        if (record.isEmpty() || isPlaying()) {
            return false;
        }

        jukebox.tryForcePlaySong();
        return true;
    }

    @Override
    public void stopPlaying() {
        requirePlaced();

        net.minecraft.world.level.block.entity.BlockEntity tileEntity = this.getTileEntityFromWorld();
        if (!(tileEntity instanceof net.minecraft.world.level.block.entity.JukeboxBlockEntity jukebox)) {
            return;
        }

        jukebox.getSongPlayer().stop(tileEntity.getLevel(), tileEntity.getBlockState());
    }

    @Override
    public boolean eject() {
        ensureNoWorldGeneration();

        net.minecraft.world.level.block.entity.BlockEntity tileEntity = this.getTileEntityFromWorld();
        if (!(tileEntity instanceof net.minecraft.world.level.block.entity.JukeboxBlockEntity)) return false;

        net.minecraft.world.level.block.entity.JukeboxBlockEntity jukebox = (net.minecraft.world.level.block.entity.JukeboxBlockEntity) tileEntity;
        boolean result = !jukebox.getTheItem().isEmpty();
        jukebox.popOutTheItem();
        return result;
    }

    @Override
    public CraftJukebox copy() {
        return new CraftJukebox(this, null);
    }

    @Override
    public CraftJukebox copy(Location location) {
        return new CraftJukebox(this, location);
    }
}
