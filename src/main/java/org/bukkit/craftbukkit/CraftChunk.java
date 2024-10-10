package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import net.ethylenemc.interfaces.server.level.EthyleneServerLevel;
import net.ethylenemc.interfaces.world.entity.EthyleneEntity;
import net.ethylenemc.interfaces.world.level.EthyleneLevel;
import net.ethylenemc.interfaces.world.level.chunk.EthyleneChunkAccess;
import net.ethylenemc.interfaces.world.level.entity.EthylenePersistentEntitySectionManager;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;

public class CraftChunk implements Chunk {
    private final net.minecraft.server.level.ServerLevel worldServer;
    private final int x;
    private final int z;
    private static final net.minecraft.world.level.chunk.PalettedContainer<net.minecraft.world.level.block.state.BlockState> emptyBlockIDs = new net.minecraft.world.level.chunk.PalettedContainer<>(net.minecraft.world.level.block.Block.BLOCK_STATE_REGISTRY, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), net.minecraft.world.level.chunk.PalettedContainer.Strategy.SECTION_STATES);
    private static final byte[] FULL_LIGHT = new byte[2048];
    private static final byte[] EMPTY_LIGHT = new byte[2048];

    public CraftChunk(net.minecraft.world.level.chunk.LevelChunk chunk) {
        worldServer = (ServerLevel) chunk.level;
        x = chunk.getPos().x;
        z = chunk.getPos().z;
    }

    public CraftChunk(net.minecraft.server.level.ServerLevel worldServer, int x, int z) {
        this.worldServer = worldServer;
        this.x = x;
        this.z = z;
    }

    @Override
    public World getWorld() {
        return ((EthyleneLevel) worldServer).getWorld();
    }

    public CraftWorld getCraftWorld() {
        return (CraftWorld) getWorld();
    }

    public net.minecraft.world.level.chunk.ChunkAccess getHandle(net.minecraft.world.level.chunk.status.ChunkStatus chunkStatus) {
        net.minecraft.world.level.chunk.ChunkAccess chunkAccess = worldServer.getChunk(x, z, chunkStatus);

        // SPIGOT-7332: Get unwrapped extension
        if (chunkAccess instanceof net.minecraft.world.level.chunk.ImposterProtoChunk extension) {
            return extension.getWrapped();
        }

        return chunkAccess;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "CraftChunk{" + "x=" + getX() + "z=" + getZ() + '}';
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        validateChunkCoordinates(worldServer.getMinBuildHeight(), worldServer.getMaxBuildHeight(), x, y, z);

        return new CraftBlock(worldServer, new net.minecraft.core.BlockPos((this.x << 4) | x, y, (this.z << 4) | z));
    }

    @Override
    public boolean isEntitiesLoaded() {
        return getCraftWorld().getHandle().entityManager.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x, z));
    }

    @Override
    public Entity[] getEntities() {
        if (!isLoaded()) {
            getWorld().getChunkAt(x, z); // Transient load for this tick
        }

        net.minecraft.world.level.entity.PersistentEntitySectionManager<net.minecraft.world.entity.Entity> entityManager = getCraftWorld().getHandle().entityManager;
        long pair = net.minecraft.world.level.ChunkPos.asLong(x, z);

        if (entityManager.areEntitiesLoaded(pair)) {
            return ((EthylenePersistentEntitySectionManager) entityManager).getEntities(new net.minecraft.world.level.ChunkPos(x, z)).stream()
                    .map(entity -> ((EthyleneEntity) entity).getBukkitEntity())
                    .filter(Objects::nonNull).toArray(Entity[]::new);
        }

        entityManager.ensureChunkQueuedForLoad(pair); // Start entity loading

        // SPIGOT-6772: Use entity mailbox and re-schedule entities if they get unloaded
        net.minecraft.util.thread.ProcessorMailbox<Runnable> mailbox = ((net.minecraft.world.level.chunk.storage.EntityStorage) entityManager.permanentStorage).entityDeserializerQueue;
        BooleanSupplier supplier = () -> {
            // only execute inbox if our entities are not present
            if (entityManager.areEntitiesLoaded(pair)) {
                return true;
            }

            if (!((EthylenePersistentEntitySectionManager) entityManager).isPending(pair)) {
                // Our entities got unloaded, this should normally not happen.
                entityManager.ensureChunkQueuedForLoad(pair); // Re-start entity loading
            }

            // tick loading inbox, which loads the created entities to the world
            // (if present)
            entityManager.tick();
            // check if our entities are loaded
            return entityManager.areEntitiesLoaded(pair);
        };

        // now we wait until the entities are loaded,
        // the converting from NBT to entity object is done on the main Thread which is why we wait
        while (!supplier.getAsBoolean()) {
            if (mailbox.size() != 0) {
                mailbox.run();
            } else {
                Thread.yield();
                LockSupport.parkNanos("waiting for entity loading", 100000L);
            }
        }

        return ((EthylenePersistentEntitySectionManager) entityManager).getEntities(new net.minecraft.world.level.ChunkPos(x, z)).stream()
                .map(entity -> ((EthyleneEntity) entity).getBukkitEntity())
                .filter(Objects::nonNull).toArray(Entity[]::new);
    }

    @Override
    public BlockState[] getTileEntities() {
        if (!isLoaded()) {
            getWorld().getChunkAt(x, z); // Transient load for this tick
        }
        int index = 0;
        net.minecraft.world.level.chunk.ChunkAccess chunk = getHandle(net.minecraft.world.level.chunk.status.ChunkStatus.FULL);

        BlockState[] entities = new BlockState[chunk.blockEntities.size()];

        for (net.minecraft.core.BlockPos position : chunk.blockEntities.keySet()) {
            entities[index++] = ((EthyleneLevel) worldServer).getWorld().getBlockAt(position.getX(), position.getY(), position.getZ()).getState();
        }

        return entities;
    }

    @Override
    public boolean isGenerated() {
        net.minecraft.world.level.chunk.ChunkAccess chunk = getHandle(net.minecraft.world.level.chunk.status.ChunkStatus.EMPTY);
        return chunk.getPersistedStatus().isOrAfter(net.minecraft.world.level.chunk.status.ChunkStatus.FULL);
    }

    @Override
    public boolean isLoaded() {
        return getWorld().isChunkLoaded(this);
    }

    @Override
    public boolean load() {
        return getWorld().loadChunk(getX(), getZ(), true);
    }

    @Override
    public boolean load(boolean generate) {
        return getWorld().loadChunk(getX(), getZ(), generate);
    }

    @Override
    public boolean unload() {
        return getWorld().unloadChunk(getX(), getZ());
    }

    @Override
    public boolean isSlimeChunk() {
        // 987234911L is deterimined in EntitySlime when seeing if a slime can spawn in a chunk
        return net.minecraft.world.level.levelgen.WorldgenRandom.seedSlimeChunk(getX(), getZ(), getWorld().getSeed(), 987234911L).nextInt(10) == 0;
    }

    @Override
    public boolean unload(boolean save) {
        return getWorld().unloadChunk(getX(), getZ(), save);
    }

    @Override
    public boolean isForceLoaded() {
        return getWorld().isChunkForceLoaded(getX(), getZ());
    }

    @Override
    public void setForceLoaded(boolean forced) {
        getWorld().setChunkForceLoaded(getX(), getZ(), forced);
    }

    @Override
    public boolean addPluginChunkTicket(Plugin plugin) {
        return getWorld().addPluginChunkTicket(getX(), getZ(), plugin);
    }

    @Override
    public boolean removePluginChunkTicket(Plugin plugin) {
        return getWorld().removePluginChunkTicket(getX(), getZ(), plugin);
    }

    @Override
    public Collection<Plugin> getPluginChunkTickets() {
        return getWorld().getPluginChunkTickets(getX(), getZ());
    }

    @Override
    public long getInhabitedTime() {
        return getHandle(net.minecraft.world.level.chunk.status.ChunkStatus.EMPTY).getInhabitedTime();
    }

    @Override
    public void setInhabitedTime(long ticks) {
        Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");

        getHandle(net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_STARTS).setInhabitedTime(ticks);
    }

    @Override
    public boolean contains(BlockData block) {
        Preconditions.checkArgument(block != null, "Block cannot be null");

        Predicate<net.minecraft.world.level.block.state.BlockState> nms = Predicates.equalTo(((CraftBlockData) block).getState());
        for (net.minecraft.world.level.chunk.LevelChunkSection section : getHandle(net.minecraft.world.level.chunk.status.ChunkStatus.FULL).getSections()) {
            if (section != null && section.getStates().maybeHas(nms)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean contains(Biome biome) {
        Preconditions.checkArgument(biome != null, "Biome cannot be null");

        net.minecraft.world.level.chunk.ChunkAccess chunk = getHandle(net.minecraft.world.level.chunk.status.ChunkStatus.BIOMES);
        Predicate<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> nms = Predicates.equalTo(CraftBiome.bukkitToMinecraftHolder(biome));
        for (net.minecraft.world.level.chunk.LevelChunkSection section : chunk.getSections()) {
            if (section != null && section.getBiomes().maybeHas(nms)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ChunkSnapshot getChunkSnapshot() {
        return getChunkSnapshot(true, false, false);
    }

    @Override
    public ChunkSnapshot getChunkSnapshot(boolean includeMaxBlockY, boolean includeBiome, boolean includeBiomeTempRain) {
        net.minecraft.world.level.chunk.ChunkAccess chunk = getHandle(net.minecraft.world.level.chunk.status.ChunkStatus.FULL);

        net.minecraft.world.level.chunk.LevelChunkSection[] cs = chunk.getSections();
        net.minecraft.world.level.chunk.PalettedContainer[] sectionBlockIDs = new net.minecraft.world.level.chunk.PalettedContainer[cs.length];
        byte[][] sectionSkyLights = new byte[cs.length][];
        byte[][] sectionEmitLights = new byte[cs.length][];
        boolean[] sectionEmpty = new boolean[cs.length];
        net.minecraft.world.level.chunk.PalettedContainerRO<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>>[] biome = (includeBiome || includeBiomeTempRain) ? new net.minecraft.world.level.chunk.PalettedContainer[cs.length] : null;

        net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> iregistry = worldServer.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        Codec<net.minecraft.world.level.chunk.PalettedContainerRO<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>>> biomeCodec = net.minecraft.world.level.chunk.PalettedContainer.codecRO(iregistry.asHolderIdMap(), iregistry.holderByNameCodec(), net.minecraft.world.level.chunk.PalettedContainer.Strategy.SECTION_BIOMES, iregistry.getHolderOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS));

        for (int i = 0; i < cs.length; i++) {
            net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();

            data.put("block_states", net.minecraft.world.level.chunk.storage.ChunkSerializer.BLOCK_STATE_CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, cs[i].getStates()).getOrThrow());
            sectionBlockIDs[i] = net.minecraft.world.level.chunk.storage.ChunkSerializer.BLOCK_STATE_CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, data.getCompound("block_states")).getOrThrow(net.minecraft.world.level.chunk.storage.ChunkSerializer.ChunkReadException::new);
            sectionEmpty[i] = cs[i].hasOnlyAir();

            net.minecraft.world.level.lighting.LevelLightEngine lightengine = worldServer.getLightEngine();
            net.minecraft.world.level.chunk.DataLayer skyLightArray = lightengine.getLayerListener(net.minecraft.world.level.LightLayer.SKY).getDataLayerData(net.minecraft.core.SectionPos.of(x, chunk.getSectionYFromSectionIndex(i), z)); // SPIGOT-7498: Convert section index
            if (skyLightArray == null) {
                sectionSkyLights[i] = worldServer.dimensionType().hasSkyLight() ? FULL_LIGHT : EMPTY_LIGHT;
            } else {
                sectionSkyLights[i] = new byte[2048];
                System.arraycopy(skyLightArray.getData(), 0, sectionSkyLights[i], 0, 2048);
            }
            net.minecraft.world.level.chunk.DataLayer emitLightArray = lightengine.getLayerListener(net.minecraft.world.level.LightLayer.BLOCK).getDataLayerData(net.minecraft.core.SectionPos.of(x, chunk.getSectionYFromSectionIndex(i), z)); // SPIGOT-7498: Convert section index
            if (emitLightArray == null) {
                sectionEmitLights[i] = EMPTY_LIGHT;
            } else {
                sectionEmitLights[i] = new byte[2048];
                System.arraycopy(emitLightArray.getData(), 0, sectionEmitLights[i], 0, 2048);
            }

            if (biome != null) {
                data.put("biomes", biomeCodec.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, cs[i].getBiomes()).getOrThrow());
                biome[i] = biomeCodec.parse(net.minecraft.nbt.NbtOps.INSTANCE, data.getCompound("biomes")).getOrThrow(net.minecraft.world.level.chunk.storage.ChunkSerializer.ChunkReadException::new);
            }
        }

        net.minecraft.world.level.levelgen.Heightmap hmap = null;

        if (includeMaxBlockY) {
            hmap = new net.minecraft.world.level.levelgen.Heightmap(chunk, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING);
            hmap.setRawData(chunk, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, chunk.heightmaps.get(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING).getRawData());
        }

        World world = getWorld();
        return new CraftChunkSnapshot(getX(), getZ(), chunk.getMinBuildHeight(), chunk.getMaxBuildHeight(), world.getName(), world.getFullTime(), sectionBlockIDs, sectionSkyLights, sectionEmitLights, sectionEmpty, hmap, iregistry, biome);
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return ((EthyleneChunkAccess) getHandle(net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_STARTS)).getPersistentDataContainer();
    }

    @Override
    public LoadLevel getLoadLevel() {
        net.minecraft.world.level.chunk.LevelChunk chunk = ((EthyleneServerLevel) worldServer).getChunkIfLoaded(getX(), getZ());
        if (chunk == null) {
            return LoadLevel.UNLOADED;
        }
        return LoadLevel.values()[chunk.getFullStatus().ordinal()];
    }

    @Override
    public Collection<GeneratedStructure> getStructures() {
        return getCraftWorld().getStructures(getX(), getZ());
    }

    @Override
    public Collection<GeneratedStructure> getStructures(Structure structure) {
        return getCraftWorld().getStructures(getX(), getZ(), structure);
    }

    @Override
    public Collection<Player> getPlayersSeeingChunk() {
        return getWorld().getPlayersSeeingChunk(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CraftChunk that = (CraftChunk) o;

        if (x != that.x) return false;
        if (z != that.z) return false;
        return worldServer.equals(that.worldServer);
    }

    @Override
    public int hashCode() {
        int result = worldServer.hashCode();
        result = 31 * result + x;
        result = 31 * result + z;
        return result;
    }

    public static ChunkSnapshot getEmptyChunkSnapshot(int x, int z, CraftWorld world, boolean includeBiome, boolean includeBiomeTempRain) {
        net.minecraft.world.level.chunk.ChunkAccess actual = world.getHandle().getChunk(x, z, (includeBiome || includeBiomeTempRain) ? net.minecraft.world.level.chunk.status.ChunkStatus.BIOMES : net.minecraft.world.level.chunk.status.ChunkStatus.EMPTY);

        /* Fill with empty data */
        int hSection = actual.getSectionsCount();
        net.minecraft.world.level.chunk.PalettedContainer[] blockIDs = new net.minecraft.world.level.chunk.PalettedContainer[hSection];
        byte[][] skyLight = new byte[hSection][];
        byte[][] emitLight = new byte[hSection][];
        boolean[] empty = new boolean[hSection];
        net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> iregistry = world.getHandle().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        net.minecraft.world.level.chunk.PalettedContainer<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>>[] biome = (includeBiome || includeBiomeTempRain) ? new net.minecraft.world.level.chunk.PalettedContainer[hSection] : null;
        Codec<net.minecraft.world.level.chunk.PalettedContainerRO<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>>> biomeCodec = net.minecraft.world.level.chunk.PalettedContainer.codecRO(iregistry.asHolderIdMap(), iregistry.holderByNameCodec(), net.minecraft.world.level.chunk.PalettedContainer.Strategy.SECTION_BIOMES, iregistry.getHolderOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS));

        for (int i = 0; i < hSection; i++) {
            blockIDs[i] = emptyBlockIDs;
            skyLight[i] = world.getHandle().dimensionType().hasSkyLight() ? FULL_LIGHT : EMPTY_LIGHT;
            emitLight[i] = EMPTY_LIGHT;
            empty[i] = true;

            if (biome != null) {
                biome[i] = (net.minecraft.world.level.chunk.PalettedContainer<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>>) biomeCodec.parse(net.minecraft.nbt.NbtOps.INSTANCE, biomeCodec.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, actual.getSection(i).getBiomes()).getOrThrow()).getOrThrow(net.minecraft.world.level.chunk.storage.ChunkSerializer.ChunkReadException::new);
            }
        }

        return new CraftChunkSnapshot(x, z, world.getMinHeight(), world.getMaxHeight(), world.getName(), world.getFullTime(), blockIDs, skyLight, emitLight, empty, new net.minecraft.world.level.levelgen.Heightmap(actual, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING), iregistry, biome);
    }

    static void validateChunkCoordinates(int minY, int maxY, int x, int y, int z) {
        Preconditions.checkArgument(0 <= x && x <= 15, "x out of range (expected 0-15, got %s)", x);
        Preconditions.checkArgument(minY <= y && y <= maxY, "y out of range (expected %s-%s, got %s)", minY, maxY, y);
        Preconditions.checkArgument(0 <= z && z <= 15, "z out of range (expected 0-15, got %s)", z);
    }

    static {
        Arrays.fill(FULL_LIGHT, (byte) 0xFF);
    }
}
