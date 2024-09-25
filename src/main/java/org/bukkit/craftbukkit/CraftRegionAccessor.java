package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.ethylenemc.interfaces.world.level.EthyleneLevel;
import net.ethylenemc.interfaces.world.level.EthyleneWorldGenLevel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.RegionAccessor;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftEntityTypes;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.BlockStateListPopulator;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.RandomSourceWrapper;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.SizedFireball;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

public abstract class CraftRegionAccessor implements RegionAccessor {

    public abstract net.minecraft.world.level.WorldGenLevel getHandle();

    public boolean isNormalWorld() {
        return getHandle() instanceof net.minecraft.server.level.ServerLevel;
    }

    @Override
    public Biome getBiome(Location location) {
        return getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        return CraftBiome.minecraftHolderToBukkit(getHandle().getNoiseBiome(x >> 2, y >> 2, z >> 2));
    }

    @Override
    public void setBiome(Location location, Biome biome) {
        setBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ(), biome);
    }

    @Override
    public void setBiome(int x, int y, int z, Biome biome) {
        Preconditions.checkArgument(biome != Biome.CUSTOM, "Cannot set the biome to %s", biome);
        net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biomeBase = CraftBiome.bukkitToMinecraftHolder(biome);
        setBiome(x, y, z, biomeBase);
    }

    public abstract void setBiome(int x, int y, int z, net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biomeBase);

    @Override
    public BlockState getBlockState(Location location) {
        return getBlockState(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return CraftBlock.at(getHandle(), new net.minecraft.core.BlockPos(x, y, z)).getState();
    }

    @Override
    public BlockData getBlockData(Location location) {
        return getBlockData(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public BlockData getBlockData(int x, int y, int z) {
        return CraftBlockData.fromData(getData(x, y, z));
    }

    @Override
    public Material getType(Location location) {
        return getType(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public Material getType(int x, int y, int z) {
        return CraftBlockType.minecraftToBukkit(getData(x, y, z).getBlock());
    }

    private net.minecraft.world.level.block.state.BlockState getData(int x, int y, int z) {
        return getHandle().getBlockState(new net.minecraft.core.BlockPos(x, y, z));
    }

    @Override
    public void setBlockData(Location location, BlockData blockData) {
        setBlockData(location.getBlockX(), location.getBlockY(), location.getBlockZ(), blockData);
    }

    @Override
    public void setBlockData(int x, int y, int z, BlockData blockData) {
        net.minecraft.world.level.WorldGenLevel world = getHandle();
        net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x, y, z);
        net.minecraft.world.level.block.state.BlockState old = getHandle().getBlockState(pos);

        CraftBlock.setTypeAndData(world, pos, old, ((CraftBlockData) blockData).getState(), true);
    }

    @Override
    public void setType(Location location, Material material) {
        setType(location.getBlockX(), location.getBlockY(), location.getBlockZ(), material);
    }

    @Override
    public void setType(int x, int y, int z, Material material) {
        setBlockData(x, y, z, material.createBlockData());
    }

    @Override
    public int getHighestBlockYAt(int x, int z) {
        return getHighestBlockYAt(x, z, org.bukkit.HeightMap.MOTION_BLOCKING);
    }

    @Override
    public int getHighestBlockYAt(Location location) {
        return getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public int getHighestBlockYAt(int x, int z, org.bukkit.HeightMap heightMap) {
        return getHandle().getHeight(CraftHeightMap.toNMS(heightMap), x, z);
    }

    @Override
    public int getHighestBlockYAt(Location location, org.bukkit.HeightMap heightMap) {
        return getHighestBlockYAt(location.getBlockX(), location.getBlockZ(), heightMap);
    }

    @Override
    public boolean generateTree(Location location, Random random, TreeType treeType) {
        net.minecraft.core.BlockPos pos = CraftLocation.toBlockPosition(location);
        return generateTree(getHandle(), ((EthyleneWorldGenLevel) getHandle()).getMinecraftWorld().getChunkSource().getGenerator(), pos, new RandomSourceWrapper(random), treeType);
    }

    @Override
    public boolean generateTree(Location location, Random random, TreeType treeType, Consumer<? super BlockState> consumer) {
        return generateTree(location, random, treeType, (consumer == null) ? null : (block) -> {
            consumer.accept(block);
            return true;
        });
    }

    @Override
    public boolean generateTree(Location location, Random random, TreeType treeType, Predicate<? super BlockState> predicate) {
        net.minecraft.core.BlockPos pos = CraftLocation.toBlockPosition(location);
        BlockStateListPopulator populator = new BlockStateListPopulator(getHandle());
        boolean result = generateTree(populator, ((EthyleneWorldGenLevel) getHandle()).getMinecraftWorld().getChunkSource().getGenerator(), pos, new RandomSourceWrapper(random), treeType);
        populator.refreshTiles();

        for (BlockState blockState : populator.getList()) {
            if (predicate == null || predicate.test(blockState)) {
                blockState.update(true, true);
            }
        }

        return result;
    }

    public boolean generateTree(net.minecraft.world.level.WorldGenLevel access, net.minecraft.world.level.chunk.ChunkGenerator chunkGenerator, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random, TreeType treeType) {
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> gen;
        switch (treeType) {
            case BIG_TREE:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.FANCY_OAK;
                break;
            case BIRCH:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.BIRCH;
                break;
            case REDWOOD:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.SPRUCE;
                break;
            case TALL_REDWOOD:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.PINE;
                break;
            case JUNGLE:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.MEGA_JUNGLE_TREE;
                break;
            case SMALL_JUNGLE:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.JUNGLE_TREE_NO_VINE;
                break;
            case COCOA_TREE:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.JUNGLE_TREE;
                break;
            case JUNGLE_BUSH:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.JUNGLE_BUSH;
                break;
            case RED_MUSHROOM:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.HUGE_RED_MUSHROOM;
                break;
            case BROWN_MUSHROOM:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.HUGE_BROWN_MUSHROOM;
                break;
            case SWAMP:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.SWAMP_OAK;
                break;
            case ACACIA:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.ACACIA;
                break;
            case DARK_OAK:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.DARK_OAK;
                break;
            case MEGA_REDWOOD:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.MEGA_SPRUCE;
                break;
            case MEGA_PINE:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.MEGA_PINE;
                break;
            case TALL_BIRCH:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.SUPER_BIRCH_BEES_0002;
                break;
            case CHORUS_PLANT:
                ((net.minecraft.world.level.block.ChorusFlowerBlock) net.minecraft.world.level.block.Blocks.CHORUS_FLOWER).generatePlant(access, pos, random, 8);
                return true;
            case CRIMSON_FUNGUS:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.CRIMSON_FUNGUS_PLANTED;
                break;
            case WARPED_FUNGUS:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.WARPED_FUNGUS_PLANTED;
                break;
            case AZALEA:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.AZALEA_TREE;
                break;
            case MANGROVE:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.MANGROVE;
                break;
            case TALL_MANGROVE:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.TALL_MANGROVE;
                break;
            case CHERRY:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.CHERRY;
                break;
            case TREE:
            default:
                gen = net.minecraft.data.worldgen.features.TreeFeatures.OAK;
                break;
        }

        net.minecraft.core.Holder<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> holder = access.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE).getHolder(gen).orElse(null);
        return (holder != null) ? holder.value().place(access, chunkGenerator, random, pos) : false;
    }

    @Override
    public Entity spawnEntity(Location location, EntityType entityType) {
        return spawn(location, entityType.getEntityClass());
    }

    @Override
    public Entity spawnEntity(Location loc, EntityType type, boolean randomizeData) {
        return spawn(loc, type.getEntityClass(), null, CreatureSpawnEvent.SpawnReason.CUSTOM, randomizeData);
    }

    @Override
    public List<Entity> getEntities() {
        List<Entity> list = new ArrayList<Entity>();

        getNMSEntities().forEach(entity -> {
            Entity bukkitEntity = entity.getBukkitEntity();

            // Assuming that bukkitEntity isn't null
            if (bukkitEntity != null && (!isNormalWorld() || bukkitEntity.isValid())) {
                list.add(bukkitEntity);
            }
        });

        return list;
    }

    @Override
    public List<LivingEntity> getLivingEntities() {
        List<LivingEntity> list = new ArrayList<LivingEntity>();

        getNMSEntities().forEach(entity -> {
            Entity bukkitEntity = entity.getBukkitEntity();

            // Assuming that bukkitEntity isn't null
            if (bukkitEntity != null && bukkitEntity instanceof LivingEntity && (!isNormalWorld() || bukkitEntity.isValid())) {
                list.add((LivingEntity) bukkitEntity);
            }
        });

        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> clazz) {
        Collection<T> list = new ArrayList<T>();

        getNMSEntities().forEach(entity -> {
            Entity bukkitEntity = entity.getBukkitEntity();

            if (bukkitEntity == null) {
                return;
            }

            Class<?> bukkitClass = bukkitEntity.getClass();

            if (clazz.isAssignableFrom(bukkitClass) && (!isNormalWorld() || bukkitEntity.isValid())) {
                list.add((T) bukkitEntity);
            }
        });

        return list;
    }

    @Override
    public Collection<Entity> getEntitiesByClasses(Class<?>... classes) {
        Collection<Entity> list = new ArrayList<Entity>();

        getNMSEntities().forEach(entity -> {
            Entity bukkitEntity = entity.getBukkitEntity();

            if (bukkitEntity == null) {
                return;
            }

            Class<?> bukkitClass = bukkitEntity.getClass();

            for (Class<?> clazz : classes) {
                if (clazz.isAssignableFrom(bukkitClass)) {
                    if (!isNormalWorld() || bukkitEntity.isValid()) {
                        list.add(bukkitEntity);
                    }
                    break;
                }
            }
        });

        return list;
    }

    public abstract Iterable<net.minecraft.world.entity.Entity> getNMSEntities();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> T createEntity(Location location, Class<T> clazz) throws IllegalArgumentException {
        net.minecraft.world.entity.Entity entity = createEntity(location, clazz, true);

        if (!isNormalWorld()) {
            entity.generation = true;
        }

        return (T) entity.getBukkitEntity();
    }

    @Override
    public <T extends Entity> T spawn(Location location, Class<T> clazz) throws IllegalArgumentException {
        return spawn(location, clazz, null, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<? super T> function) throws IllegalArgumentException {
        return spawn(location, clazz, function, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public <T extends Entity> T spawn(Location location, Class<T> clazz, boolean randomizeData, Consumer<? super T> function) throws IllegalArgumentException {
        return spawn(location, clazz, function, CreatureSpawnEvent.SpawnReason.CUSTOM, randomizeData);
    }

    public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<? super T> function, CreatureSpawnEvent.SpawnReason reason) throws IllegalArgumentException {
        return spawn(location, clazz, function, reason, true);
    }

    public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<? super T> function, CreatureSpawnEvent.SpawnReason reason, boolean randomizeData) throws IllegalArgumentException {
        net.minecraft.world.entity.Entity entity = createEntity(location, clazz, randomizeData);

        return addEntity(entity, reason, function, randomizeData);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> T addEntity(T entity) {
        Preconditions.checkArgument(!entity.isInWorld(), "Entity has already been added to a world");
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (nmsEntity.level() != getHandle().getLevel()) {
            nmsEntity = nmsEntity.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(getHandle().getLevel(), nmsEntity, net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));
        }

        addEntityWithPassengers(nmsEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return (T) nmsEntity.getBukkitEntity();
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T addEntity(net.minecraft.world.entity.Entity entity, CreatureSpawnEvent.SpawnReason reason) throws IllegalArgumentException {
        return addEntity(entity, reason, null, true);
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T addEntity(net.minecraft.world.entity.Entity entity, CreatureSpawnEvent.SpawnReason reason, Consumer<? super T> function, boolean randomizeData) throws IllegalArgumentException {
        Preconditions.checkArgument(entity != null, "Cannot spawn null entity");

        if (randomizeData && entity instanceof net.minecraft.world.entity.Mob) {
            ((net.minecraft.world.entity.Mob) entity).finalizeSpawn(getHandle(), getHandle().getCurrentDifficultyAt(entity.blockPosition()), net.minecraft.world.entity.MobSpawnType.COMMAND, (net.minecraft.world.entity.SpawnGroupData) null);
        }

        if (!isNormalWorld()) {
            entity.generation = true;
        }

        if (function != null) {
            function.accept((T) entity.getBukkitEntity());
        }

        addEntityToWorld(entity, reason);
        return (T) entity.getBukkitEntity();
    }

    public abstract void addEntityToWorld(net.minecraft.world.entity.Entity entity, CreatureSpawnEvent.SpawnReason reason);

    public abstract void addEntityWithPassengers(net.minecraft.world.entity.Entity entity, CreatureSpawnEvent.SpawnReason reason);

    @SuppressWarnings("unchecked")
    public net.minecraft.world.entity.Entity makeEntity(Location location, Class<? extends Entity> clazz) throws IllegalArgumentException {
        return createEntity(location, clazz, true);
    }

    @SuppressWarnings("unchecked")
    public net.minecraft.world.entity.Entity createEntity(Location location, Class<? extends Entity> clazz, boolean randomizeData) throws IllegalArgumentException {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(clazz != null, "Entity class cannot be null");

        // Convert classes which have no direct entity type, but where spawn able by the if cases
        Consumer<net.minecraft.world.entity.Entity> runOld = other -> { };
        if (clazz == AbstractArrow.class) {
            clazz = Arrow.class;
        } else if (clazz == AbstractHorse.class) {
            clazz = Horse.class;
        } else if (clazz == Fireball.class) {
            clazz = LargeFireball.class;
        } else if (clazz == LingeringPotion.class) {
            clazz = ThrownPotion.class;
            runOld = other -> ((net.minecraft.world.entity.projectile.ThrownPotion) other).setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.LINGERING_POTION, 1)));
        } else if (clazz == Minecart.class) {
            clazz = RideableMinecart.class;
        } else if (clazz == SizedFireball.class) {
            clazz = LargeFireball.class;
        } else if (clazz == SplashPotion.class) {
            clazz = ThrownPotion.class;
        } else if (clazz == TippedArrow.class) {
            clazz = Arrow.class;
            runOld = other -> ((Arrow) other.getBukkitEntity()).setBasePotionType(PotionType.WATER);
        }

        CraftEntityTypes.EntityTypeData<?, ?> entityTypeData = CraftEntityTypes.getEntityTypeData(clazz);

        if (entityTypeData == null || entityTypeData.spawnFunction() == null) {
            if (CraftEntity.class.isAssignableFrom(clazz)) {
                // SPIGOT-7565: Throw a more descriptive error message when a developer tries to spawn an entity from a CraftBukkit class
                throw new IllegalArgumentException(String.format("Cannot spawn an entity from its CraftBukkit implementation class '%s' use the Bukkit class instead. "
                        + "You can get the Bukkit representation via Entity#getType()#getEntityClass()", clazz.getName()));
            } else {
                throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName());
            }
        }

        if (!entityTypeData.entityType().isEnabledByFeature(((EthyleneLevel) ((EthyleneWorldGenLevel) getHandle()).getMinecraftWorld()).getWorld())) {
            throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName() + " because it is not an enabled feature");
        }

        net.minecraft.world.entity.Entity entity = entityTypeData.spawnFunction().apply(new CraftEntityTypes.SpawnData(getHandle(), location, randomizeData, isNormalWorld()));

        if (entity != null) {
            runOld.accept(entity);
            return entity;
        }

        throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName());
    }
}
