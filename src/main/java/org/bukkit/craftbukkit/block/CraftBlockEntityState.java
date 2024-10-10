package org.bukkit.craftbukkit.block;

import java.util.Set;

import net.ethylenemc.EthyleneStatic;
import net.ethylenemc.interfaces.world.level.chunk.EthyleneChunkAccess;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.TileState;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftBlockEntityState<T extends net.minecraft.world.level.block.entity.BlockEntity> extends CraftBlockState implements TileState {

    private final T tileEntity;
    private final T snapshot;

    public CraftBlockEntityState(World world, T tileEntity) {
        super(world, tileEntity.getBlockPos(), tileEntity.getBlockState());

        this.tileEntity = tileEntity;

        // copy tile entity data:
        this.snapshot = this.createSnapshot(tileEntity);
        this.load(snapshot);
    }

    protected CraftBlockEntityState(CraftBlockEntityState<T> state, Location location) {
        super(state, location);
        this.tileEntity = createSnapshot(state.snapshot);
        this.snapshot = tileEntity;
        loadData(state.getSnapshotNBT());
    }

    public void refreshSnapshot() {
        this.load(tileEntity);
    }

    private net.minecraft.core.RegistryAccess getRegistryAccess() {
        net.minecraft.world.level.LevelAccessor worldHandle = getWorldHandle();
        return (worldHandle != null) ? worldHandle.registryAccess() : EthyleneStatic.getDefaultRegistryAccess();
    }

    private T createSnapshot(T tileEntity) {
        if (tileEntity == null) {
            return null;
        }

        net.minecraft.nbt.CompoundTag nbtTagCompound = tileEntity.saveWithFullMetadata(getRegistryAccess());
        T snapshot = (T) net.minecraft.world.level.block.entity.BlockEntity.loadStatic(getPosition(), getHandle(), nbtTagCompound, getRegistryAccess());

        return snapshot;
    }

    public Set<net.minecraft.core.component.DataComponentType<?>> applyComponents(net.minecraft.core.component.DataComponentMap datacomponentmap, net.minecraft.core.component.DataComponentPatch datacomponentpatch) {
        Set<net.minecraft.core.component.DataComponentType<?>> result = snapshot.applyComponentsSet(datacomponentmap, datacomponentpatch);
        load(snapshot);
        return result;
    }

    public net.minecraft.core.component.DataComponentMap collectComponents() {
        return snapshot.collectComponents();
    }

    // Loads the specified data into the snapshot net.minecraft.world.level.block.entity.BlockEntity.
    public void loadData(net.minecraft.nbt.CompoundTag nbtTagCompound) {
        snapshot.loadWithComponents(nbtTagCompound, getRegistryAccess());
        load(snapshot);
    }

    // copies the net.minecraft.world.level.block.entity.BlockEntity-specific data, retains the position
    private void copyData(T from, T to) {
        net.minecraft.nbt.CompoundTag nbtTagCompound = from.saveWithFullMetadata(getRegistryAccess());
        to.loadWithComponents(nbtTagCompound, getRegistryAccess());
    }

    // gets the wrapped net.minecraft.world.level.block.entity.BlockEntity
    protected T getTileEntity() {
        return tileEntity;
    }

    // gets the cloned net.minecraft.world.level.block.entity.BlockEntity which is used to store the captured data
    protected T getSnapshot() {
        return snapshot;
    }

    // gets the current net.minecraft.world.level.block.entity.BlockEntity from the world at this position
    protected net.minecraft.world.level.block.entity.BlockEntity getTileEntityFromWorld() {
        requirePlaced();

        return getWorldHandle().getBlockEntity(this.getPosition());
    }

    // gets the NBT data of the net.minecraft.world.level.block.entity.BlockEntity represented by this block state
    public net.minecraft.nbt.CompoundTag getSnapshotNBT() {
        // update snapshot
        applyTo(snapshot);

        return snapshot.saveWithFullMetadata(getRegistryAccess());
    }

    public net.minecraft.nbt.CompoundTag getItemNBT() {
        // update snapshot
        applyTo(snapshot);

        // See net.minecraft.world.level.block.entity.BlockEntity#saveToItem
        net.minecraft.nbt.CompoundTag nbt = snapshot.saveCustomOnly(getRegistryAccess());
        snapshot.removeComponentsFromTag(nbt);
        return nbt;
    }

    public void addEntityType(net.minecraft.nbt.CompoundTag nbt) {
        net.minecraft.world.level.block.entity.BlockEntity.addEntityType(nbt, snapshot.getType());
    }

    // gets the packet data of the net.minecraft.world.level.block.entity.BlockEntity represented by this block state
    public net.minecraft.nbt.CompoundTag getUpdateNBT() {
        // update snapshot
        applyTo(snapshot);

        return snapshot.getUpdateTag(getRegistryAccess());
    }

    // copies the data of the given tile entity to this block state
    protected void load(T tileEntity) {
        if (tileEntity != null && tileEntity != snapshot) {
            copyData(tileEntity, snapshot);
        }
    }

    // applies the net.minecraft.world.level.block.entity.BlockEntity data of this block state to the given net.minecraft.world.level.block.entity.BlockEntity
    protected void applyTo(T tileEntity) {
        if (tileEntity != null && tileEntity != snapshot) {
            copyData(snapshot, tileEntity);
        }
    }

    protected boolean isApplicable(net.minecraft.world.level.block.entity.BlockEntity tileEntity) {
        return tileEntity != null && this.tileEntity.getClass() == tileEntity.getClass();
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);

        if (result && this.isPlaced()) {
            net.minecraft.world.level.block.entity.BlockEntity tile = getTileEntityFromWorld();

            if (isApplicable(tile)) {
                applyTo((T) tile);
                tile.setChanged();
            }
        }

        return result;
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return ((EthyleneChunkAccess) this.getSnapshot()).getPersistentDataContainer();
    }

    @Nullable
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket(@NotNull Location location) {
        return new net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket(CraftLocation.toBlockPosition(location), snapshot.getType(), getUpdateNBT());
    }

    @Override
    public CraftBlockEntityState<T> copy() {
        return new CraftBlockEntityState<>(this, null);
    }

    @Override
    public CraftBlockEntityState<T> copy(Location location) {
        return new CraftBlockEntityState<>(this, location);
    }
}
