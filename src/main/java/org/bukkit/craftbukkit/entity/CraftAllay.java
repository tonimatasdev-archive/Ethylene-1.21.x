package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.ethylenemc.EthyleneCaptures;
import net.ethylenemc.interfaces.world.entity.EthyleneEntity;
import net.ethylenemc.interfaces.world.entity.animal.allay.EthyleneAllay;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.inventory.Inventory;

public class CraftAllay extends CraftCreature implements org.bukkit.entity.Allay {

    public CraftAllay(CraftServer server, net.minecraft.world.entity.animal.allay.Allay entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.allay.Allay getHandle() {
        return (net.minecraft.world.entity.animal.allay.Allay) entity;
    }

    @Override
    public String toString() {
        return "CraftAllay";
    }

    @Override
    public Inventory getInventory() {
        return new CraftInventory(getHandle().getInventory());
    }

    @Override
    public boolean canDuplicate() {
        return getHandle().canDuplicate();
    }

    @Override
    public void setCanDuplicate(boolean canDuplicate) {
        ((EthyleneAllay) getHandle()).setCanDuplicate(canDuplicate);
    }

    @Override
    public long getDuplicationCooldown() {
        return getHandle().duplicationCooldown;
    }

    @Override
    public void setDuplicationCooldown(long l) {
        getHandle().duplicationCooldown = l;
    }

    @Override
    public void resetDuplicationCooldown() {
        getHandle().resetDuplicationCooldown();
    }

    @Override
    public boolean isDancing() {
        return getHandle().isDancing();
    }

    @Override
    public void startDancing(Location location) {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(location.getBlock().getType().equals(Material.JUKEBOX), "The Block in the Location need to be a JukeBox");
        getHandle().setJukeboxPlaying(CraftLocation.toBlockPosition(location), true);
    }

    @Override
    public void startDancing() {
        ((EthyleneAllay) getHandle()).setForceDancing(true);
        getHandle().setDancing(true);
    }

    @Override
    public void stopDancing() {
        ((EthyleneAllay) getHandle()).setForceDancing(false);
        getHandle().jukeboxPos = null;
        getHandle().setJukeboxPlaying(null, false);
    }

    @Override
    public org.bukkit.entity.Allay duplicateAllay() {
        // Ethylene start - Allay#duplicateAllay is void method.
        getHandle().duplicateAllay();
        net.minecraft.world.entity.animal.allay.Allay nmsAllay = EthyleneCaptures.duplicateAllay.getAndSet(null);
        return (nmsAllay != null) ? (org.bukkit.entity.Allay) ((EthyleneEntity) nmsAllay).getBukkitEntity() : null;
        // Ethylene end
    }

    public Location getJukebox() {
        net.minecraft.core.BlockPos nmsJukeboxPos = getHandle().jukeboxPos;
        return (nmsJukeboxPos != null) ? CraftLocation.toBukkit(nmsJukeboxPos, getWorld()) : null;
    }
}
