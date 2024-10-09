package net.ethylenemc.interfaces.network.syncher;

import net.minecraft.network.syncher.EntityDataAccessor;

public interface EthyleneSynchedEntityData {
    <T> void markDirty(EntityDataAccessor<T> datawatcherobject);
}
