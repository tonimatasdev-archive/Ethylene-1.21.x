package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.craftbukkit.CraftLootTable;
import org.bukkit.craftbukkit.entity.CraftEntitySnapshot;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.loot.LootTable;
import org.bukkit.spawner.TrialSpawnerConfiguration;

public class CraftTrialSpawnerConfiguration implements TrialSpawnerConfiguration {
    private final net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity snapshot;

    private int spawnRange;
    private float totalMobs;
    private float simultaneousMobs;
    private float totalMobsAddedPerPlayer;
    private float simultaneousMobsAddedPerPlayer;
    private int ticksBetweenSpawn;
    private net.minecraft.util.random.SimpleWeightedRandomList<net.minecraft.world.level.SpawnData> spawnPotentialsDefinition;
    private net.minecraft.util.random.SimpleWeightedRandomList<net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> lootTablesToEject;
    private net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> itemsToDropWhenOminous;

    public CraftTrialSpawnerConfiguration(net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig minecraft, net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity snapshot) {
        this.snapshot = snapshot;

        this.spawnRange = minecraft.spawnRange();
        this.totalMobs = minecraft.totalMobs();
        this.simultaneousMobs = minecraft.simultaneousMobs();
        this.totalMobsAddedPerPlayer = minecraft.totalMobsAddedPerPlayer();
        this.simultaneousMobsAddedPerPlayer = minecraft.simultaneousMobsAddedPerPlayer();
        this.ticksBetweenSpawn = minecraft.ticksBetweenSpawn();
        this.spawnPotentialsDefinition = minecraft.spawnPotentialsDefinition();
        this.lootTablesToEject = minecraft.lootTablesToEject();
        this.itemsToDropWhenOminous = minecraft.itemsToDropWhenOminous();
    }

    @Override
    public EntityType getSpawnedType() {
       if (spawnPotentialsDefinition.isEmpty()) {
           return null;
       }

       Optional<net.minecraft.world.entity.EntityType<?>> type = net.minecraft.world.entity.EntityType.by(spawnPotentialsDefinition.unwrap().get(0).data().getEntityToSpawn());
       return type.map(CraftEntityType::minecraftToBukkit).orElse(null);
    }

    @Override
    public void setSpawnedType(EntityType entityType) {
        if (entityType == null) {
            getTrialData().nextSpawnData = Optional.empty();
            spawnPotentialsDefinition = net.minecraft.util.random.SimpleWeightedRandomList.empty(); // need clear the spawnPotentials to avoid nextSpawnData being replaced later
            return;
        }
        Preconditions.checkArgument(entityType != EntityType.UNKNOWN, "Can't spawn EntityType %s from mob spawners!", entityType);

        net.minecraft.world.level.SpawnData data = new net.minecraft.world.level.SpawnData();
        data.getEntityToSpawn().putString("id", net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(CraftEntityType.bukkitToMinecraft(entityType)).toString());
        getTrialData().nextSpawnData = Optional.of(data);
        spawnPotentialsDefinition = net.minecraft.util.random.SimpleWeightedRandomList.single(data);
    }

    @Override
    public float getBaseSpawnsBeforeCooldown() {
        return totalMobs;
    }

    @Override
    public void setBaseSpawnsBeforeCooldown(float amount) {
        totalMobs = amount;
    }

    @Override
    public float getBaseSimultaneousEntities() {
        return simultaneousMobs;
    }

    @Override
    public void setBaseSimultaneousEntities(float amount) {
        simultaneousMobs = amount;
    }

    @Override
    public float getAdditionalSpawnsBeforeCooldown() {
        return totalMobsAddedPerPlayer;
    }

    @Override
    public void setAdditionalSpawnsBeforeCooldown(float amount) {
        totalMobsAddedPerPlayer = amount;
    }

    @Override
    public float getAdditionalSimultaneousEntities() {
        return simultaneousMobsAddedPerPlayer;
    }

    @Override
    public void setAdditionalSimultaneousEntities(float amount) {
        simultaneousMobsAddedPerPlayer = amount;
    }

    @Override
    public int getDelay() {
      return ticksBetweenSpawn;
    }

    @Override
    public void setDelay(int delay) {
        Preconditions.checkArgument(delay >= 0, "Delay cannot be less than 0");

        ticksBetweenSpawn = delay;
    }

    @Override
    public int getSpawnRange() {
        return spawnRange;
    }

    @Override
    public void setSpawnRange(int spawnRange) {
        this.spawnRange = spawnRange;
    }

    @Override
    public EntitySnapshot getSpawnedEntity() {
        net.minecraft.util.random.SimpleWeightedRandomList<net.minecraft.world.level.SpawnData> potentials = spawnPotentialsDefinition;
        if (potentials.isEmpty()) {
            return null;
        }

        return CraftEntitySnapshot.create(potentials.unwrap().get(0).data().getEntityToSpawn());
    }

    @Override
    public void setSpawnedEntity(EntitySnapshot snapshot) {
        setSpawnedEntity(snapshot, null, null);
    }

    @Override
    public void setSpawnedEntity(SpawnerEntry spawnerEntry) {
        Preconditions.checkArgument(spawnerEntry != null, "Entry cannot be null");

        setSpawnedEntity(spawnerEntry.getSnapshot(), spawnerEntry.getSpawnRule(), spawnerEntry.getEquipment());
    }

    private void setSpawnedEntity(EntitySnapshot snapshot, SpawnRule spawnRule, SpawnerEntry.Equipment equipment) {
        if (snapshot == null) {
            getTrialData().nextSpawnData = Optional.empty();
            spawnPotentialsDefinition = net.minecraft.util.random.SimpleWeightedRandomList.empty(); // need clear the spawnPotentials to avoid nextSpawnData being replaced later
            return;
        }

        net.minecraft.nbt.CompoundTag compoundTag = ((CraftEntitySnapshot) snapshot).getData();
        net.minecraft.world.level.SpawnData data = new net.minecraft.world.level.SpawnData(compoundTag, Optional.ofNullable(CraftCreatureSpawner.toMinecraftRule(spawnRule)), CraftCreatureSpawner.getEquipment(equipment));

        getTrialData().nextSpawnData = Optional.of(data);
        spawnPotentialsDefinition = net.minecraft.util.random.SimpleWeightedRandomList.single(data);
    }

    @Override
    public void addPotentialSpawn(EntitySnapshot snapshot, int weight, SpawnRule spawnRule) {
        addPotentialSpawn(snapshot, weight, spawnRule, null);
    }

    private void addPotentialSpawn(EntitySnapshot snapshot, int weight, SpawnRule spawnRule, SpawnerEntry.Equipment equipment) {
        Preconditions.checkArgument(snapshot != null, "Snapshot cannot be null");

        net.minecraft.nbt.CompoundTag compoundTag = ((CraftEntitySnapshot) snapshot).getData();

        net.minecraft.util.random.SimpleWeightedRandomList.Builder<net.minecraft.world.level.SpawnData> builder = net.minecraft.util.random.SimpleWeightedRandomList.builder(); // PAIL rename Builder
        spawnPotentialsDefinition.unwrap().forEach(entry -> builder.add(entry.data(), entry.getWeight().asInt()));
        builder.add(new net.minecraft.world.level.SpawnData(compoundTag, Optional.ofNullable(CraftCreatureSpawner.toMinecraftRule(spawnRule)), CraftCreatureSpawner.getEquipment(equipment)), weight);
        spawnPotentialsDefinition = builder.build();
    }

    @Override
    public void addPotentialSpawn(SpawnerEntry spawnerEntry) {
        Preconditions.checkArgument(spawnerEntry != null, "Entry cannot be null");

        addPotentialSpawn(spawnerEntry.getSnapshot(), spawnerEntry.getSpawnWeight(), spawnerEntry.getSpawnRule(), spawnerEntry.getEquipment());
    }

    @Override
    public void setPotentialSpawns(Collection<SpawnerEntry> entries) {
        Preconditions.checkArgument(entries != null, "Entries cannot be null");

        net.minecraft.util.random.SimpleWeightedRandomList.Builder<net.minecraft.world.level.SpawnData> builder = net.minecraft.util.random.SimpleWeightedRandomList.builder();
        for (SpawnerEntry spawnerEntry : entries) {
            net.minecraft.nbt.CompoundTag compoundTag = ((CraftEntitySnapshot) spawnerEntry.getSnapshot()).getData();
            builder.add(new net.minecraft.world.level.SpawnData(compoundTag, Optional.ofNullable(CraftCreatureSpawner.toMinecraftRule(spawnerEntry.getSpawnRule())), CraftCreatureSpawner.getEquipment(spawnerEntry.getEquipment())), spawnerEntry.getSpawnWeight());
        }
        spawnPotentialsDefinition = builder.build();
    }

    @Override
    public List<SpawnerEntry> getPotentialSpawns() {
        List<SpawnerEntry> entries = new ArrayList<>();

        for (net.minecraft.util.random.WeightedEntry.Wrapper<net.minecraft.world.level.SpawnData> entry : spawnPotentialsDefinition.unwrap()) { // PAIL rename Wrapper
            CraftEntitySnapshot snapshot = CraftEntitySnapshot.create(entry.data().getEntityToSpawn());

            if (snapshot != null) {
                SpawnRule rule = entry.data().customSpawnRules().map(CraftCreatureSpawner::fromMinecraftRule).orElse(null);
                entries.add(new SpawnerEntry(snapshot, entry.getWeight().asInt(), rule));
            }
        }
        return entries;
    }

    @Override
    public Map<LootTable, Integer> getPossibleRewards() {
        Map<LootTable, Integer> tables = new HashMap<>();

        for (net.minecraft.util.random.WeightedEntry.Wrapper<net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> entry : lootTablesToEject.unwrap()) {
            LootTable table = CraftLootTable.minecraftToBukkit(entry.data());
            if (table != null) {
                tables.put(table, entry.getWeight().asInt());
            }
        }

        return tables;
    }

    @Override
    public void addPossibleReward(LootTable table, int weight) {
        Preconditions.checkArgument(table != null, "Table cannot be null");
        Preconditions.checkArgument(weight >= 1, "Weight must be at least 1");

        net.minecraft.util.random.SimpleWeightedRandomList.Builder<net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> builder = net.minecraft.util.random.SimpleWeightedRandomList.builder();
        lootTablesToEject.unwrap().forEach(entry -> builder.add(entry.data(), entry.getWeight().asInt()));
        builder.add(CraftLootTable.bukkitToMinecraft(table), weight);
        lootTablesToEject = builder.build();
    }

    @Override
    public void removePossibleReward(LootTable table) {
        Preconditions.checkArgument(table != null, "Key cannot be null");

        net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> minecraftKey = CraftLootTable.bukkitToMinecraft(table);
        net.minecraft.util.random.SimpleWeightedRandomList.Builder<net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> builder = net.minecraft.util.random.SimpleWeightedRandomList.builder();

        for (net.minecraft.util.random.WeightedEntry.Wrapper<net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> entry : lootTablesToEject.unwrap()) {
            if (!entry.data().equals(minecraftKey)) {
                builder.add(entry.data(), entry.getWeight().asInt());
            }
        }
        lootTablesToEject = builder.build();
    }

    @Override
    public void setPossibleRewards(Map<LootTable, Integer> rewards) {
        if (rewards == null || rewards.isEmpty()) {
            lootTablesToEject = net.minecraft.util.random.SimpleWeightedRandomList.empty();
            return;
        }

        net.minecraft.util.random.SimpleWeightedRandomList.Builder<net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> builder = net.minecraft.util.random.SimpleWeightedRandomList.builder();
        rewards.forEach((table, weight) -> {
            Preconditions.checkArgument(table != null, "Table cannot be null");
            Preconditions.checkArgument(weight >= 1, "Weight must be at least 1");

            builder.add(CraftLootTable.bukkitToMinecraft(table), weight);
        });

        lootTablesToEject = builder.build();
    }

    @Override
    public int getRequiredPlayerRange() {
      return snapshot.trialSpawner.getRequiredPlayerRange();
    }

    @Override
    public void setRequiredPlayerRange(int requiredPlayerRange) {
        snapshot.trialSpawner.requiredPlayerRange = requiredPlayerRange;
    }

    private net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData getTrialData() {
        return snapshot.getTrialSpawner().getData();
    }

    protected net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig toMinecraft() {
        return new net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig(spawnRange, totalMobs, simultaneousMobs, totalMobsAddedPerPlayer, simultaneousMobsAddedPerPlayer, ticksBetweenSpawn, spawnPotentialsDefinition, lootTablesToEject, itemsToDropWhenOminous);
    }
}
