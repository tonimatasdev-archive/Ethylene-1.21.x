package org.bukkit.craftbukkit.block;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Vault;

public class CraftVault extends CraftBlockEntityState<net.minecraft.world.level.block.entity.vault.VaultBlockEntity> implements Vault {

    public CraftVault(World world, net.minecraft.world.level.block.entity.vault.VaultBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftVault(CraftVault state, Location location) {
        super(state, location);
    }

    @Override
    public CraftVault copy() {
        return new CraftVault(this, null);
    }

    @Override
    public CraftVault copy(Location location) {
        return new CraftVault(this, location);
    }
}
