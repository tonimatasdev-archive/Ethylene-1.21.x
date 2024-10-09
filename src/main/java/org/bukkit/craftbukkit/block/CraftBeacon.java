package org.bukkit.craftbukkit.block;

import java.util.ArrayList;
import java.util.Collection;

import net.ethylenemc.interfaces.world.entity.EthyleneEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CraftBeacon extends CraftBlockEntityState<net.minecraft.world.level.block.entity.BeaconBlockEntity> implements Beacon {

    public CraftBeacon(World world, net.minecraft.world.level.block.entity.BeaconBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftBeacon(CraftBeacon state, Location location) {
        super(state, location);
    }

    @Override
    public Collection<LivingEntity> getEntitiesInRange() {
        ensureNoWorldGeneration();

        net.minecraft.world.level.block.entity.BlockEntity tileEntity = this.getTileEntityFromWorld();
        if (tileEntity instanceof net.minecraft.world.level.block.entity.BeaconBlockEntity) {
            net.minecraft.world.level.block.entity.BeaconBlockEntity beacon = (net.minecraft.world.level.block.entity.BeaconBlockEntity) tileEntity;

            Collection<net.minecraft.world.entity.player.Player> nms = net.minecraft.world.level.block.entity.BeaconBlockEntity.getHumansInRange(beacon.getLevel(), beacon.getBlockPos(), beacon.levels);
            Collection<LivingEntity> bukkit = new ArrayList<LivingEntity>(nms.size());

            for (net.minecraft.world.entity.player.Player human : nms) {
                bukkit.add((LivingEntity) ((EthyleneEntity) human).getBukkitEntity());
            }

            return bukkit;
        }

        // block is no longer a beacon
        return new ArrayList<LivingEntity>();
    }

    @Override
    public int getTier() {
        return this.getSnapshot().levels;
    }

    @Override
    public PotionEffect getPrimaryEffect() {
        return this.getSnapshot().getPrimaryEffect();
    }

    @Override
    public void setPrimaryEffect(PotionEffectType effect) {
        this.getSnapshot().primaryPower = (effect != null) ? CraftPotionEffectType.bukkitToMinecraftHolder(effect) : null;
    }

    @Override
    public PotionEffect getSecondaryEffect() {
        return this.getSnapshot().getSecondaryEffect();
    }

    @Override
    public void setSecondaryEffect(PotionEffectType effect) {
        this.getSnapshot().secondaryPower = (effect != null) ? CraftPotionEffectType.bukkitToMinecraftHolder(effect) : null;
    }

    @Override
    public String getCustomName() {
        net.minecraft.world.level.block.entity.BeaconBlockEntity beacon = this.getSnapshot();
        return beacon.name != null ? CraftChatMessage.fromComponent(beacon.name) : null;
    }

    @Override
    public void setCustomName(String name) {
        this.getSnapshot().setCustomName(CraftChatMessage.fromStringOrNull(name));
    }

    @Override
    public boolean isLocked() {
        return !this.getSnapshot().lockKey.key().isEmpty();
    }

    @Override
    public String getLock() {
        return this.getSnapshot().lockKey.key();
    }

    @Override
    public void setLock(String key) {
        this.getSnapshot().lockKey = (key == null) ? net.minecraft.world.LockCode.NO_LOCK : new net.minecraft.world.LockCode(key);
    }

    @Override
    public CraftBeacon copy() {
        return new CraftBeacon(this, null);
    }

    @Override
    public CraftBeacon copy(Location location) {
        return new CraftBeacon(this, location);
    }
}
