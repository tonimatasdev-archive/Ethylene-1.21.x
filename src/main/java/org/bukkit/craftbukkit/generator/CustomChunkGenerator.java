package org.bukkit.craftbukkit.generator;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import net.ethylenemc.interfaces.world.level.EthyleneLevel;
import net.minecraft.world.level.NoiseColumn;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.CraftHeightMap;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.util.RandomSourceWrapper;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;

public class CustomChunkGenerator extends InternalChunkGenerator {

    private final net.minecraft.world.level.chunk.ChunkGenerator delegate;
    private final ChunkGenerator generator;
    private final net.minecraft.server.level.ServerLevel world;
    private final Random random = new Random();
    private boolean newApi;
    private boolean implementBaseHeight = true;

    @Deprecated
    private class CustomBiomeGrid implements BiomeGrid {

        private final net.minecraft.world.level.chunk.ChunkAccess biome;

        public CustomBiomeGrid(net.minecraft.world.level.chunk.ChunkAccess biome) {
            this.biome = biome;
        }

        @Override
        public Biome getBiome(int x, int z) {
            return getBiome(x, 0, z);
        }

        @Override
        public void setBiome(int x, int z, Biome bio) {
            for (int y = ((EthyleneLevel) world).getWorld().getMinHeight(); y < ((EthyleneLevel) world).getWorld().getMaxHeight(); y += 4) {
                setBiome(x, y, z, bio);
            }
        }

        @Override
        public Biome getBiome(int x, int y, int z) {
            return CraftBiome.minecraftHolderToBukkit(biome.getNoiseBiome(x >> 2, y >> 2, z >> 2));
        }

        @Override
        public void setBiome(int x, int y, int z, Biome bio) {
            Preconditions.checkArgument(bio != Biome.CUSTOM, "Cannot set the biome to %s", bio);
            biome.setBiome(x >> 2, y >> 2, z >> 2, CraftBiome.bukkitToMinecraftHolder(bio));
        }
    }

    public CustomChunkGenerator(net.minecraft.server.level.ServerLevel world, net.minecraft.world.level.chunk.ChunkGenerator delegate, ChunkGenerator generator) {
        super(delegate.getBiomeSource(), delegate.generationSettingsGetter);

        this.world = world;
        this.delegate = delegate;
        this.generator = generator;
    }

    public net.minecraft.world.level.chunk.ChunkGenerator getDelegate() {
        return delegate;
    }

    private static net.minecraft.world.level.levelgen.WorldgenRandom getSeededRandom() {
        return new net.minecraft.world.level.levelgen.WorldgenRandom(new net.minecraft.world.level.levelgen.LegacyRandomSource(0));
    }

    @Override
    public net.minecraft.world.level.biome.BiomeSource getBiomeSource() {
        return delegate.getBiomeSource();
    }

    @Override
    public int getMinY() {
        return delegate.getMinY();
    }

    @Override
    public int getSeaLevel() {
        return delegate.getSeaLevel();
    }

    @Override
    public void createStructures(net.minecraft.core.RegistryAccess iregistrycustom, net.minecraft.world.level.chunk.ChunkGeneratorStructureState chunkgeneratorstructurestate, net.minecraft.world.level.StructureManager structuremanager, net.minecraft.world.level.chunk.ChunkAccess ichunkaccess, net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager structuretemplatemanager) {
        net.minecraft.world.level.levelgen.WorldgenRandom random = getSeededRandom();
        int x = ichunkaccess.getPos().x;
        int z = ichunkaccess.getPos().z;

        random.setSeed(net.minecraft.util.Mth.getSeed(x, "should-structures".hashCode(), z) ^ world.getSeed());
        if (generator.shouldGenerateStructures(((EthyleneLevel) this.world).getWorld(), new RandomSourceWrapper.RandomWrapper(random), x, z)) {
            super.createStructures(iregistrycustom, chunkgeneratorstructurestate, structuremanager, ichunkaccess, structuretemplatemanager);
        }
    }

    @Override
    public void buildSurface(net.minecraft.server.level.WorldGenRegion regionlimitedworldaccess, net.minecraft.world.level.StructureManager structuremanager, net.minecraft.world.level.levelgen.RandomState randomstate, net.minecraft.world.level.chunk.ChunkAccess ichunkaccess) {
        net.minecraft.world.level.levelgen.WorldgenRandom random = getSeededRandom();
        int x = ichunkaccess.getPos().x;
        int z = ichunkaccess.getPos().z;

        random.setSeed(net.minecraft.util.Mth.getSeed(x, "should-surface".hashCode(), z) ^ regionlimitedworldaccess.getSeed());
        if (generator.shouldGenerateSurface(((EthyleneLevel) this.world).getWorld(), new RandomSourceWrapper.RandomWrapper(random), x, z)) {
            delegate.buildSurface(regionlimitedworldaccess, structuremanager, randomstate, ichunkaccess);
        }

        CraftChunkData chunkData = new CraftChunkData(((EthyleneLevel) this.world).getWorld(), ichunkaccess);

        random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        generator.generateSurface(((EthyleneLevel) this.world).getWorld(), new RandomSourceWrapper.RandomWrapper(random), x, z, chunkData);

        if (generator.shouldGenerateBedrock()) {
            random = getSeededRandom();
            random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
            // delegate.buildBedrock(ichunkaccess, random);
        }

        random = getSeededRandom();
        random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        generator.generateBedrock(((EthyleneLevel) this.world).getWorld(), new RandomSourceWrapper.RandomWrapper(random), x, z, chunkData);
        chunkData.breakLink();

        // return if new api is used
        if (newApi) {
            return;
        }

        // old ChunkGenerator logic, for backwards compatibility
        // Call the bukkit ChunkGenerator before structure generation so correct biome information is available.
        this.random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);

        // Get default biome data for chunk
        CustomBiomeGrid biomegrid = new CustomBiomeGrid(ichunkaccess);

        ChunkData data;
        try {
            if (generator.isParallelCapable()) {
                data = generator.generateChunkData(((EthyleneLevel) (this.world)).getWorld(), this.random, x, z, biomegrid);
            } else {
                synchronized (this) {
                    data = generator.generateChunkData(((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) this.world)))))).getWorld(), this.random, x, z, biomegrid);
                }
            }
        } catch (UnsupportedOperationException exception) {
            newApi = true;
            return;
        }

        Preconditions.checkArgument(data instanceof OldCraftChunkData, "Plugins must use createChunkData(World) rather than implementing ChunkData: %s", data);
        OldCraftChunkData craftData = (OldCraftChunkData) data;
        net.minecraft.world.level.chunk.LevelChunkSection[] sections = craftData.getRawChunkData();

        net.minecraft.world.level.chunk.LevelChunkSection[] csect = ichunkaccess.getSections();
        int scnt = Math.min(csect.length, sections.length);

        // Loop through returned sections
        for (int sec = 0; sec < scnt; sec++) {
            if (sections[sec] == null) {
                continue;
            }
            net.minecraft.world.level.chunk.LevelChunkSection section = sections[sec];

            // SPIGOT-6843: Copy biomes over to new section.
            // Not the most performant way, but has a small footprint and developer should move to the new api anyway
            net.minecraft.world.level.chunk.LevelChunkSection oldSection = csect[sec];
            for (int biomeX = 0; biomeX < 4; biomeX++) {
                for (int biomeY = 0; biomeY < 4; biomeY++) {
                    for (int biomeZ = 0; biomeZ < 4; biomeZ++) {
                        section.setBiome(biomeX, biomeY, biomeZ, oldSection.getNoiseBiome(biomeX, biomeY, biomeZ));
                    }
                }
            }

            csect[sec] = section;
        }

        if (craftData.getTiles() != null) {
            for (net.minecraft.core.BlockPos pos : craftData.getTiles()) {
                int tx = pos.getX();
                int ty = pos.getY();
                int tz = pos.getZ();
                net.minecraft.world.level.block.state.BlockState block = craftData.getTypeId(tx, ty, tz);

                if (block.hasBlockEntity()) {
                    net.minecraft.world.level.block.entity.BlockEntity tile = ((net.minecraft.world.level.block.EntityBlock) block.getBlock()).newBlockEntity(new net.minecraft.core.BlockPos((x << 4) + tx, ty, (z << 4) + tz), block);
                    ichunkaccess.setBlockEntity(tile);
                }
            }
        }
    }

    @Override
    public void applyCarvers(net.minecraft.server.level.WorldGenRegion regionlimitedworldaccess, long seed, net.minecraft.world.level.levelgen.RandomState randomstate, net.minecraft.world.level.biome.BiomeManager biomemanager, net.minecraft.world.level.StructureManager structuremanager, net.minecraft.world.level.chunk.ChunkAccess ichunkaccess, net.minecraft.world.level.levelgen.GenerationStep.Carving worldgenstage_features) {
        net.minecraft.world.level.levelgen.WorldgenRandom random = getSeededRandom();
        int x = ichunkaccess.getPos().x;
        int z = ichunkaccess.getPos().z;

        random.setSeed(net.minecraft.util.Mth.getSeed(x, "should-caves".hashCode(), z) ^ regionlimitedworldaccess.getSeed());
        if (generator.shouldGenerateCaves(((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) this.world)))))).getWorld(), new RandomSourceWrapper.RandomWrapper(random), x, z)) {
            delegate.applyCarvers(regionlimitedworldaccess, seed, randomstate, biomemanager, structuremanager, ichunkaccess, worldgenstage_features);
        }

        // Minecraft removed the LIQUID_CARVERS stage from world generation, without removing the LIQUID Carving enum.
        // Meaning this method is only called once for each chunk, so no check is required.
        CraftChunkData chunkData = new CraftChunkData(((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) this.world)))))).getWorld(), ichunkaccess);
        random.setDecorationSeed(seed, 0, 0);

        generator.generateCaves(((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) this.world)))))).getWorld(), new RandomSourceWrapper.RandomWrapper(random), x, z, chunkData);
        chunkData.breakLink();
    }

    @Override
    public CompletableFuture<net.minecraft.world.level.chunk.ChunkAccess> fillFromNoise(net.minecraft.world.level.levelgen.blending.Blender blender, net.minecraft.world.level.levelgen.RandomState randomstate, net.minecraft.world.level.StructureManager structuremanager, net.minecraft.world.level.chunk.ChunkAccess ichunkaccess) {
        CompletableFuture<net.minecraft.world.level.chunk.ChunkAccess> future = null;
        net.minecraft.world.level.levelgen.WorldgenRandom random = getSeededRandom();
        int x = ichunkaccess.getPos().x;
        int z = ichunkaccess.getPos().z;

        random.setSeed(net.minecraft.util.Mth.getSeed(x, "should-noise".hashCode(), z) ^ ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) this.world)))))).getSeed());
        if (generator.shouldGenerateNoise(((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) this.world)))))).getWorld(), new RandomSourceWrapper.RandomWrapper(random), x, z)) {
            future = delegate.fillFromNoise(blender, randomstate, structuremanager, ichunkaccess);
        }

        java.util.function.Function<net.minecraft.world.level.chunk.ChunkAccess, net.minecraft.world.level.chunk.ChunkAccess> function = (ichunkaccess1) -> {
            CraftChunkData chunkData = new CraftChunkData(((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) this.world)))))).getWorld(), ichunkaccess1);
            random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);

            generator.generateNoise(((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) this.world)))))).getWorld(), new RandomSourceWrapper.RandomWrapper(random), x, z, chunkData);
            chunkData.breakLink();
            return ichunkaccess1;
        };

        return future == null ? CompletableFuture.supplyAsync(() -> function.apply(ichunkaccess), net.minecraft.Util.backgroundExecutor()) : future.thenApply(function);
    }

    @Override
    public int getBaseHeight(int i, int j, net.minecraft.world.level.levelgen.Heightmap.Types heightmap_type, net.minecraft.world.level.LevelHeightAccessor levelheightaccessor, net.minecraft.world.level.levelgen.RandomState randomstate) {
        if (implementBaseHeight) {
            try {
                net.minecraft.world.level.levelgen.WorldgenRandom random = getSeededRandom();
                int xChunk = i >> 4;
                int zChunk = j >> 4;
                random.setSeed((long) xChunk * 341873128712L + (long) zChunk * 132897987541L);

                return generator.getBaseHeight(((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) this.world)))))).getWorld(), new RandomSourceWrapper.RandomWrapper(random), i, j, CraftHeightMap.fromNMS(heightmap_type));
            } catch (UnsupportedOperationException exception) {
                implementBaseHeight = false;
            }
        }

        return delegate.getBaseHeight(i, j, heightmap_type, levelheightaccessor, randomstate);
    }

    @Override
    public net.minecraft.util.random.WeightedRandomList<net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData> getMobsAt(net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> holder, net.minecraft.world.level.StructureManager structuremanager, net.minecraft.world.entity.MobCategory enumcreaturetype, net.minecraft.core.BlockPos blockposition) {
        return delegate.getMobsAt(holder, structuremanager, enumcreaturetype, blockposition);
    }

    @Override
    public void applyBiomeDecoration(net.minecraft.world.level.WorldGenLevel generatoraccessseed, net.minecraft.world.level.chunk.ChunkAccess ichunkaccess, net.minecraft.world.level.StructureManager structuremanager) {
        net.minecraft.world.level.levelgen.WorldgenRandom random = getSeededRandom();
        int x = ichunkaccess.getPos().x;
        int z = ichunkaccess.getPos().z;

        random.setSeed(net.minecraft.util.Mth.getSeed(x, "should-decoration".hashCode(), z) ^ generatoraccessseed.getSeed());
        super.applyBiomeDecoration(generatoraccessseed, ichunkaccess, structuremanager, generator.shouldGenerateDecorations(((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) this.world)))))).getWorld(), new RandomSourceWrapper.RandomWrapper(random), x, z));
    }

    @Override
    public void addDebugScreenInfo(List<String> list, net.minecraft.world.level.levelgen.RandomState randomstate, net.minecraft.core.BlockPos blockposition) {
        delegate.addDebugScreenInfo(list, randomstate, blockposition);
    }

    @Override
    public void spawnOriginalMobs(net.minecraft.server.level.WorldGenRegion regionlimitedworldaccess) {
        net.minecraft.world.level.levelgen.WorldgenRandom random = getSeededRandom();
        int x = regionlimitedworldaccess.getCenter().x;
        int z = regionlimitedworldaccess.getCenter().z;

        random.setSeed(net.minecraft.util.Mth.getSeed(x, "should-mobs".hashCode(), z) ^ regionlimitedworldaccess.getSeed());
        if (generator.shouldGenerateMobs(((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) ((EthyleneLevel) this.world)))))).getWorld(), new RandomSourceWrapper.RandomWrapper(random), x, z)) {
            delegate.spawnOriginalMobs(regionlimitedworldaccess);
        }
    }

    @Override
    public int getSpawnHeight(net.minecraft.world.level.LevelHeightAccessor levelheightaccessor) {
        return delegate.getSpawnHeight(levelheightaccessor);
    }

    @Override
    public int getGenDepth() {
        return delegate.getGenDepth();
    }

    @Override
    public NoiseColumn getBaseColumn(int i, int j, net.minecraft.world.level.LevelHeightAccessor levelheightaccessor, net.minecraft.world.level.levelgen.RandomState randomstate) {
        return delegate.getBaseColumn(i, j, levelheightaccessor, randomstate);
    }

    @Override
    protected MapCodec<? extends net.minecraft.world.level.chunk.ChunkGenerator> codec() {
        return MapCodec.unit(null);
    }
}
