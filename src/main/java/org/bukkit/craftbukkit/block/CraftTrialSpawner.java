package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.TrialSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.spawner.TrialSpawnerConfiguration;

public class CraftTrialSpawner extends CraftBlockEntityState<net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity> implements TrialSpawner {

    private final CraftTrialSpawnerConfiguration normalConfig;
    private final CraftTrialSpawnerConfiguration ominousConfig;

    public CraftTrialSpawner(World world, net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity tileEntity) {
        super(world, tileEntity);
        this.normalConfig = new CraftTrialSpawnerConfiguration(tileEntity.getTrialSpawner().getNormalConfig(), getSnapshot());
        this.ominousConfig = new CraftTrialSpawnerConfiguration(tileEntity.getTrialSpawner().getOminousConfig(), getSnapshot());
    }

    protected CraftTrialSpawner(CraftTrialSpawner state, Location location) {
        super(state, location);
        this.normalConfig = state.normalConfig;
        this.ominousConfig = state.ominousConfig;
    }

    @Override
    public int getCooldownLength() {
        return getSnapshot().trialSpawner.getTargetCooldownLength();
    }

    @Override
    public void setCooldownLength(int ticks) {
        getSnapshot().trialSpawner.targetCooldownLength = ticks;
    }

    @Override
    public int getRequiredPlayerRange() {
      return getSnapshot().trialSpawner.getRequiredPlayerRange();
    }

    @Override
    public void setRequiredPlayerRange(int requiredPlayerRange) {
        getSnapshot().trialSpawner.requiredPlayerRange = requiredPlayerRange;
    }

    @Override
    public Collection<Player> getTrackedPlayers() {
        ImmutableSet.Builder<Player> players = ImmutableSet.builder();

        for (UUID uuid : getTrialData().detectedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                players.add(player);
            }
        }
        return players.build();
    }

    @Override
    public boolean isTrackingPlayer(Player player) {
        Preconditions.checkArgument(player != null, "Player cannot be null");

        return getTrialData().detectedPlayers.contains(player.getUniqueId());
    }

    @Override
    public void startTrackingPlayer(Player player) {
        Preconditions.checkArgument(player != null, "Player cannot be null");

        getTrialData().detectedPlayers.add(player.getUniqueId());
    }

    @Override
    public void stopTrackingPlayer(Player player) {
        Preconditions.checkArgument(player != null, "Player cannot be null");

        getTrialData().detectedPlayers.remove(player.getUniqueId());
    }

    @Override
    public Collection<Entity> getTrackedEntities() {
        ImmutableSet.Builder<Entity> entities = ImmutableSet.builder();

        for (UUID uuid : getTrialData().currentMobs) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities.build();
    }

    @Override
    public boolean isTrackingEntity(Entity entity) {
        Preconditions.checkArgument(entity != null, "Entity cannot be null");

        return getTrialData().currentMobs.contains(entity.getUniqueId());
    }

    @Override
    public void startTrackingEntity(Entity entity) {
        Preconditions.checkArgument(entity != null, "Entity cannot be null");

        getTrialData().currentMobs.add(entity.getUniqueId());
    }

    @Override
    public void stopTrackingEntity(Entity entity) {
        Preconditions.checkArgument(entity != null, "Entity cannot be null");

        getTrialData().currentMobs.remove(entity.getUniqueId());
    }

    @Override
    public boolean isOminous() {
        return getHandle().getValue(net.minecraft.world.level.block.TrialSpawnerBlock.OMINOUS);
    }

    @Override
    public void setOminous(boolean ominous) {
        getSnapshot().trialSpawner.isOminous = ominous;
        if (ominous) {
            setData(getHandle().setValue(net.minecraft.world.level.block.TrialSpawnerBlock.OMINOUS, true));
            // TODO: Consider calling net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData#resetAfterBecomingOminous in update(...), but note that method also removes entities
            return;
        }

        setData(getHandle().setValue(net.minecraft.world.level.block.TrialSpawnerBlock.OMINOUS, false));
    }

    @Override
    public TrialSpawnerConfiguration getNormalConfiguration() {
       return normalConfig;
    }

    @Override
    public TrialSpawnerConfiguration getOminousConfiguration() {
       return ominousConfig;
    }

    @Override
    protected void applyTo(net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity tileEntity) {
        super.applyTo(tileEntity);

        tileEntity.trialSpawner.normalConfig = normalConfig.toMinecraft();
        tileEntity.trialSpawner.ominousConfig = ominousConfig.toMinecraft();
    }

    private net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData getTrialData() {
        return getSnapshot().getTrialSpawner().getData();
    }

    @Override
    public CraftTrialSpawner copy() {
        return new CraftTrialSpawner(this, null);
    }

    @Override
    public CraftTrialSpawner copy(Location location) {
        return new CraftTrialSpawner(this, location);
    }
}
