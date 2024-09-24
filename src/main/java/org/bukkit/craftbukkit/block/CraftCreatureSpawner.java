package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftLootTable;
import org.bukkit.craftbukkit.entity.CraftEntitySnapshot;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;

public class CraftCreatureSpawner extends CraftBlockEntityState<net.minecraft.world.level.block.entity.SpawnerBlockEntity> implements CreatureSpawner {

    public CraftCreatureSpawner(World world, net.minecraft.world.level.block.entity.SpawnerBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftCreatureSpawner(CraftCreatureSpawner state, Location location) {
        super(state, location);
    }

    @Override
    public EntityType getSpawnedType() {
        net.minecraft.world.level.SpawnData spawnData = this.getSnapshot().getSpawner().nextSpawnData;
        if (spawnData == null) {
            return null;
        }

        Optional<net.minecraft.world.entity.EntityType<?>> type = net.minecraft.world.entity.EntityType.by(spawnData.getEntityToSpawn());
        return type.map(CraftEntityType::minecraftToBukkit).orElse(null);
    }

    @Override
    public void setSpawnedType(EntityType entityType) {
        if (entityType == null) {
            this.getSnapshot().getSpawner().spawnPotentials = net.minecraft.util.random.SimpleWeightedRandomList.empty(); // need clear the spawnPotentials to avoid nextSpawnData being replaced later
            this.getSnapshot().getSpawner().nextSpawnData = new net.minecraft.world.level.SpawnData();
            return;
        }
        Preconditions.checkArgument(entityType != EntityType.UNKNOWN, "Can't spawn EntityType %s from mob spawners!", entityType);

        net.minecraft.util.RandomSource rand = (this.isPlaced()) ? this.getWorldHandle().getRandom() : net.minecraft.util.RandomSource.create();
        this.getSnapshot().setEntityId(CraftEntityType.bukkitToMinecraft(entityType), rand);
    }

    @Override
    public EntitySnapshot getSpawnedEntity() {
        net.minecraft.world.level.SpawnData spawnData = this.getSnapshot().getSpawner().nextSpawnData;
        if (spawnData == null) {
            return null;
        }

        return CraftEntitySnapshot.create(spawnData.getEntityToSpawn());
    }

    @Override
    public void setSpawnedEntity(EntitySnapshot snapshot) {
        setSpawnedEntity(this.getSnapshot().getSpawner(), snapshot, null, null);
    }

    @Override
    public void setSpawnedEntity(SpawnerEntry spawnerEntry) {
        Preconditions.checkArgument(spawnerEntry != null, "Entry cannot be null");

        setSpawnedEntity(this.getSnapshot().getSpawner(), spawnerEntry.getSnapshot(), spawnerEntry.getSpawnRule(), spawnerEntry.getEquipment());
    }

    public static void setSpawnedEntity(net.minecraft.world.level.BaseSpawner spawner, EntitySnapshot snapshot, SpawnRule spawnRule, SpawnerEntry.Equipment equipment) {
        spawner.spawnPotentials = net.minecraft.util.random.SimpleWeightedRandomList.empty(); // need clear the spawnPotentials to avoid nextSpawnData being replaced later

        if (snapshot == null) {
            spawner.nextSpawnData = new net.minecraft.world.level.SpawnData();
            return;
        }
        net.minecraft.nbt.CompoundTag compoundTag = ((CraftEntitySnapshot) snapshot).getData();

        spawner.nextSpawnData = new net.minecraft.world.level.SpawnData(compoundTag, Optional.ofNullable(toMinecraftRule(spawnRule)), getEquipment(equipment));
    }

    @Override
    public void addPotentialSpawn(EntitySnapshot snapshot, int weight, SpawnRule spawnRule) {
        addPotentialSpawn(this.getSnapshot().getSpawner(), snapshot, weight, spawnRule, null);
    }

    public static void addPotentialSpawn(net.minecraft.world.level.BaseSpawner spawner, EntitySnapshot snapshot, int weight, SpawnRule spawnRule, SpawnerEntry.Equipment equipment) {
        Preconditions.checkArgument(snapshot != null, "Snapshot cannot be null");

        net.minecraft.nbt.CompoundTag compoundTag = ((CraftEntitySnapshot) snapshot).getData();

        net.minecraft.util.random.SimpleWeightedRandomList.Builder<net.minecraft.world.level.SpawnData> builder = net.minecraft.util.random.SimpleWeightedRandomList.builder(); // PAIL rename Builder
        spawner.spawnPotentials.unwrap().forEach(entry -> builder.add(entry.data(), entry.getWeight().asInt()));
        builder.add(new net.minecraft.world.level.SpawnData(compoundTag, Optional.ofNullable(toMinecraftRule(spawnRule)), getEquipment(equipment)), weight);
        spawner.spawnPotentials = builder.build();
    }

    @Override
    public void addPotentialSpawn(SpawnerEntry spawnerEntry) {
        Preconditions.checkArgument(spawnerEntry != null, "Entry cannot be null");

        addPotentialSpawn(spawnerEntry.getSnapshot(), spawnerEntry.getSpawnWeight(), spawnerEntry.getSpawnRule());
    }

    @Override
    public void setPotentialSpawns(Collection<SpawnerEntry> entries) {
        setPotentialSpawns(this.getSnapshot().getSpawner(), entries);
    }

    public static void setPotentialSpawns(net.minecraft.world.level.BaseSpawner spawner, Collection<SpawnerEntry> entries) {
        Preconditions.checkArgument(entries != null, "Entries cannot be null");

        net.minecraft.util.random.SimpleWeightedRandomList.Builder<net.minecraft.world.level.SpawnData> builder = net.minecraft.util.random.SimpleWeightedRandomList.builder();
        for (SpawnerEntry spawnerEntry : entries) {
            net.minecraft.nbt.CompoundTag compoundTag = ((CraftEntitySnapshot) spawnerEntry.getSnapshot()).getData();
            builder.add(new net.minecraft.world.level.SpawnData(compoundTag, Optional.ofNullable(toMinecraftRule(spawnerEntry.getSpawnRule())), getEquipment(spawnerEntry.getEquipment())), spawnerEntry.getSpawnWeight());
        }
        spawner.spawnPotentials = builder.build();
    }

    @Override
    public List<SpawnerEntry> getPotentialSpawns() {
        return getPotentialSpawns(this.getSnapshot().getSpawner());
    }

    public static List<SpawnerEntry> getPotentialSpawns(net.minecraft.world.level.BaseSpawner spawner) {
        List<SpawnerEntry> entries = new ArrayList<>();

        for (net.minecraft.util.random.WeightedEntry.Wrapper<net.minecraft.world.level.SpawnData> entry : spawner.spawnPotentials.unwrap()) { // PAIL rename Wrapper
            CraftEntitySnapshot snapshot = CraftEntitySnapshot.create(entry.data().getEntityToSpawn());

            if (snapshot != null) {
                SpawnRule rule = entry.data().customSpawnRules().map(CraftCreatureSpawner::fromMinecraftRule).orElse(null);
                entries.add(new SpawnerEntry(snapshot, entry.getWeight().asInt(), rule, getEquipment(entry.data().equipment())));
            }
        }
        return entries;
    }

    public static net.minecraft.world.level.SpawnData.CustomSpawnRules toMinecraftRule(SpawnRule rule) { // PAIL rename CustomSpawnRules
        if (rule == null) {
            return null;
        }
        return new net.minecraft.world.level.SpawnData.CustomSpawnRules(new net.minecraft.util.InclusiveRange<>(rule.getMinBlockLight(), rule.getMaxBlockLight()), new net.minecraft.util.InclusiveRange<>(rule.getMinSkyLight(), rule.getMaxSkyLight()));
    }

    public static SpawnRule fromMinecraftRule(net.minecraft.world.level.SpawnData.CustomSpawnRules rule) {
        net.minecraft.util.InclusiveRange<Integer> blockLight = rule.blockLightLimit();
        net.minecraft.util.InclusiveRange<Integer> skyLight = rule.skyLightLimit();

        return new SpawnRule(blockLight.maxInclusive(), blockLight.maxInclusive(), skyLight.minInclusive(), skyLight.maxInclusive());
    }

    @Override
    public String getCreatureTypeName() {
        net.minecraft.world.level.SpawnData spawnData = this.getSnapshot().getSpawner().nextSpawnData;
        if (spawnData == null) {
            return null;
        }

        Optional<net.minecraft.world.entity.EntityType<?>> type = net.minecraft.world.entity.EntityType.by(spawnData.getEntityToSpawn());
        return type.map(CraftEntityType::minecraftToBukkit).map(CraftEntityType::bukkitToString).orElse(null);
    }

    @Override
    public void setCreatureTypeByName(String creatureType) {
        // Verify input
        EntityType type = CraftEntityType.stringToBukkit(creatureType);
        if (type == null) {
            setSpawnedType(null);
            return;
        }
        setSpawnedType(type);
    }

    @Override
    public int getDelay() {
        return this.getSnapshot().getSpawner().spawnDelay;
    }

    @Override
    public void setDelay(int delay) {
        this.getSnapshot().getSpawner().spawnDelay = delay;
    }

    @Override
    public int getMinSpawnDelay() {
        return this.getSnapshot().getSpawner().minSpawnDelay;
    }

    @Override
    public void setMinSpawnDelay(int spawnDelay) {
        Preconditions.checkArgument(spawnDelay <= getMaxSpawnDelay(), "Minimum Spawn Delay must be less than or equal to Maximum Spawn Delay");
        this.getSnapshot().getSpawner().minSpawnDelay = spawnDelay;
    }

    @Override
    public int getMaxSpawnDelay() {
        return this.getSnapshot().getSpawner().maxSpawnDelay;
    }

    @Override
    public void setMaxSpawnDelay(int spawnDelay) {
        Preconditions.checkArgument(spawnDelay > 0, "Maximum Spawn Delay must be greater than 0.");
        Preconditions.checkArgument(spawnDelay >= getMinSpawnDelay(), "Maximum Spawn Delay must be greater than or equal to Minimum Spawn Delay");
        this.getSnapshot().getSpawner().maxSpawnDelay = spawnDelay;
    }

    @Override
    public int getMaxNearbyEntities() {
        return this.getSnapshot().getSpawner().maxNearbyEntities;
    }

    @Override
    public void setMaxNearbyEntities(int maxNearbyEntities) {
        this.getSnapshot().getSpawner().maxNearbyEntities = maxNearbyEntities;
    }

    @Override
    public int getSpawnCount() {
        return this.getSnapshot().getSpawner().spawnCount;
    }

    @Override
    public void setSpawnCount(int count) {
        this.getSnapshot().getSpawner().spawnCount = count;
    }

    @Override
    public int getRequiredPlayerRange() {
        return this.getSnapshot().getSpawner().requiredPlayerRange;
    }

    @Override
    public void setRequiredPlayerRange(int requiredPlayerRange) {
        this.getSnapshot().getSpawner().requiredPlayerRange = requiredPlayerRange;
    }

    @Override
    public int getSpawnRange() {
        return this.getSnapshot().getSpawner().spawnRange;
    }

    @Override
    public void setSpawnRange(int spawnRange) {
        this.getSnapshot().getSpawner().spawnRange = spawnRange;
    }

    @Override
    public CraftCreatureSpawner copy() {
        return new CraftCreatureSpawner(this, null);
    }

    @Override
    public CraftCreatureSpawner copy(Location location) {
        return new CraftCreatureSpawner(this, location);
    }

    public static Optional<net.minecraft.world.entity.EquipmentTable> getEquipment(SpawnerEntry.Equipment bukkit) {
        if (bukkit == null) {
            return Optional.empty();
        }

        return Optional.of(new net.minecraft.world.entity.EquipmentTable(
                CraftLootTable.bukkitToMinecraft(bukkit.getEquipmentLootTable()),
                bukkit.getDropChances().entrySet().stream().collect(Collectors.toMap((entry) -> CraftEquipmentSlot.getNMS(entry.getKey()), Map.Entry::getValue)))
        );
    }

    public static SpawnerEntry.Equipment getEquipment(Optional<net.minecraft.world.entity.EquipmentTable> optional) {
        return optional.map((nms) -> new SpawnerEntry.Equipment(
                CraftLootTable.minecraftToBukkit(nms.lootTable()),
                new HashMap<>(nms.slotDropChances().entrySet().stream().collect(Collectors.toMap((entry) -> CraftEquipmentSlot.getSlot(entry.getKey()), Map.Entry::getValue)))
        )).orElse(null);
    }
}
