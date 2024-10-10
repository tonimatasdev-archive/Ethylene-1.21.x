package org.bukkit.craftbukkit.util;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.ethylenemc.interfaces.world.level.EthyleneWorldGenLevel;
import org.bukkit.event.entity.CreatureSpawnEvent;

public abstract class DelegatedGeneratorAccess implements net.minecraft.world.level.WorldGenLevel, EthyleneWorldGenLevel {

    private net.minecraft.world.level.WorldGenLevel handle;

    public void setHandle(net.minecraft.world.level.WorldGenLevel worldAccess) {
        this.handle = worldAccess;
    }

    public net.minecraft.world.level.WorldGenLevel getHandle() {
        return handle;
    }

    @Override
    public long getSeed() {
        return handle.getSeed();
    }

    @Override
    public void setCurrentlyGenerating(Supplier<String> arg0) {
        handle.setCurrentlyGenerating(arg0);
    }

    @Override
    public boolean ensureCanWrite(net.minecraft.core.BlockPos arg0) {
        return handle.ensureCanWrite(arg0);
    }

    @Override
    public net.minecraft.server.level.ServerLevel getLevel() {
        return handle.getLevel();
    }

    public void addFreshEntityWithPassengers(net.minecraft.world.entity.Entity arg0, CreatureSpawnEvent.SpawnReason arg1) {
        handle.addFreshEntityWithPassengers(arg0, arg1);
    }

    @Override
    public void addFreshEntityWithPassengers(net.minecraft.world.entity.Entity arg0) {
        handle.addFreshEntityWithPassengers(arg0);
    }

    @Override
    public net.minecraft.server.level.ServerLevel getMinecraftWorld() {
        return ((EthyleneWorldGenLevel) handle).getMinecraftWorld();
    }

    @Override
    public net.minecraft.world.DifficultyInstance getCurrentDifficultyAt(net.minecraft.core.BlockPos arg0) {
        return handle.getCurrentDifficultyAt(arg0);
    }

    @Override
    public void neighborShapeChanged(net.minecraft.core.Direction arg0, net.minecraft.world.level.block.state.BlockState arg1, net.minecraft.core.BlockPos arg2, net.minecraft.core.BlockPos arg3, int arg4, int arg5) {
        handle.neighborShapeChanged(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public long dayTime() {
        return handle.dayTime();
    }

    @Override
    public net.minecraft.world.level.storage.LevelData getLevelData() {
        return handle.getLevelData();
    }

    @Override
    public boolean hasChunk(int arg0, int arg1) {
        return handle.hasChunk(arg0, arg1);
    }

    @Override
    public net.minecraft.world.level.chunk.ChunkSource getChunkSource() {
        return handle.getChunkSource();
    }

    @Override
    public void scheduleTick(net.minecraft.core.BlockPos arg0, net.minecraft.world.level.block.Block arg1, int arg2, net.minecraft.world.ticks.TickPriority arg3) {
        handle.scheduleTick(arg0, arg1, arg2, arg3);
    }

    @Override
    public void scheduleTick(net.minecraft.core.BlockPos arg0, net.minecraft.world.level.block.Block arg1, int arg2) {
        handle.scheduleTick(arg0, arg1, arg2);
    }

    @Override
    public void scheduleTick(net.minecraft.core.BlockPos arg0, net.minecraft.world.level.material.Fluid arg1, int arg2, net.minecraft.world.ticks.TickPriority arg3) {
        handle.scheduleTick(arg0, arg1, arg2, arg3);
    }

    @Override
    public void scheduleTick(net.minecraft.core.BlockPos arg0, net.minecraft.world.level.material.Fluid arg1, int arg2) {
        handle.scheduleTick(arg0, arg1, arg2);
    }

    @Override
    public net.minecraft.world.Difficulty getDifficulty() {
        return handle.getDifficulty();
    }

    @Override
    public void blockUpdated(net.minecraft.core.BlockPos arg0, net.minecraft.world.level.block.Block arg1) {
        handle.blockUpdated(arg0, arg1);
    }

    @Override
    public net.minecraft.server.MinecraftServer getServer() {
        return handle.getServer();
    }

    @Override
    public net.minecraft.util.RandomSource getRandom() {
        return handle.getRandom();
    }

    @Override
    public net.minecraft.world.ticks.LevelTickAccess<net.minecraft.world.level.block.Block> getBlockTicks() {
        return handle.getBlockTicks();
    }

    @Override
    public long nextSubTickCount() {
        return handle.nextSubTickCount();
    }

    @Override
    public net.minecraft.world.ticks.LevelTickAccess<net.minecraft.world.level.material.Fluid> getFluidTicks() {
        return handle.getFluidTicks();
    }

    @Override
    public void playSound(net.minecraft.world.entity.player.Player arg0, net.minecraft.core.BlockPos arg1, net.minecraft.sounds.SoundEvent arg2, net.minecraft.sounds.SoundSource arg3) {
        handle.playSound(arg0, arg1, arg2, arg3);
    }

    @Override
    public void playSound(net.minecraft.world.entity.player.Player arg0, net.minecraft.core.BlockPos arg1, net.minecraft.sounds.SoundEvent arg2, net.minecraft.sounds.SoundSource arg3, float arg4, float arg5) {
        handle.playSound(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public void levelEvent(int arg0, net.minecraft.core.BlockPos arg1, int arg2) {
        handle.levelEvent(arg0, arg1, arg2);
    }

    @Override
    public void levelEvent(net.minecraft.world.entity.player.Player arg0, int arg1, net.minecraft.core.BlockPos arg2, int arg3) {
        handle.levelEvent(arg0, arg1, arg2, arg3);
    }

    @Override
    public void addParticle(net.minecraft.core.particles.ParticleOptions arg0, double arg1, double arg2, double arg3, double arg4, double arg5, double arg6) {
        handle.addParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    @Override
    public void gameEvent(net.minecraft.core.Holder<net.minecraft.world.level.gameevent.GameEvent> arg0, net.minecraft.world.phys.Vec3 arg1, net.minecraft.world.level.gameevent.GameEvent.Context arg2) {
        handle.gameEvent(arg0, arg1, arg2);
    }

    @Override
    public void gameEvent(net.minecraft.core.Holder<net.minecraft.world.level.gameevent.GameEvent> arg0, net.minecraft.core.BlockPos arg1, net.minecraft.world.level.gameevent.GameEvent.Context arg2) {
        handle.gameEvent(arg0, arg1, arg2);
    }

    @Override
    public void gameEvent(net.minecraft.world.entity.Entity arg0, net.minecraft.core.Holder<net.minecraft.world.level.gameevent.GameEvent> arg1, net.minecraft.core.BlockPos arg2) {
        handle.gameEvent(arg0, arg1, arg2);
    }

    @Override
    public void gameEvent(net.minecraft.world.entity.Entity arg0, net.minecraft.core.Holder<net.minecraft.world.level.gameevent.GameEvent> arg1, net.minecraft.world.phys.Vec3 arg2) {
        handle.gameEvent(arg0, arg1, arg2);
    }

    @Override
    public List<net.minecraft.world.phys.shapes.VoxelShape> getEntityCollisions(net.minecraft.world.entity.Entity arg0, net.minecraft.world.phys.AABB arg1) {
        return handle.getEntityCollisions(arg0, arg1);
    }

    @Override
    public <T extends net.minecraft.world.level.block.entity.BlockEntity> Optional<T> getBlockEntity(net.minecraft.core.BlockPos arg0, net.minecraft.world.level.block.entity.BlockEntityType<T> arg1) {
        return handle.getBlockEntity(arg0, arg1);
    }

    @Override
    public net.minecraft.core.BlockPos getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types arg0, net.minecraft.core.BlockPos arg1) {
        return handle.getHeightmapPos(arg0, arg1);
    }

    @Override
    public boolean isUnobstructed(net.minecraft.world.entity.Entity arg0, net.minecraft.world.phys.shapes.VoxelShape arg1) {
        return handle.isUnobstructed(arg0, arg1);
    }

    @Override
    public boolean hasNearbyAlivePlayer(double arg0, double arg1, double arg2, double arg3) {
        return handle.hasNearbyAlivePlayer(arg0, arg1, arg2, arg3);
    }

    @Override
    public List<? extends net.minecraft.world.entity.player.Player> players() {
        return handle.players();
    }

    @Override
    public List<net.minecraft.world.entity.Entity> getEntities(net.minecraft.world.entity.Entity arg0, net.minecraft.world.phys.AABB arg1, Predicate<? super net.minecraft.world.entity.Entity> arg2) {
        return handle.getEntities(arg0, arg1, arg2);
    }

    @Override
    public <T extends net.minecraft.world.entity.Entity> List<T> getEntities(net.minecraft.world.level.entity.EntityTypeTest<net.minecraft.world.entity.Entity, T> arg0, net.minecraft.world.phys.AABB arg1, Predicate<? super T> arg2) {
        return handle.getEntities(arg0, arg1, arg2);
    }

    @Override
    public List<net.minecraft.world.entity.Entity> getEntities(net.minecraft.world.entity.Entity arg0, net.minecraft.world.phys.AABB arg1) {
        return handle.getEntities(arg0, arg1);
    }

    @Override
    public <T extends net.minecraft.world.entity.Entity> List<T> getEntitiesOfClass(Class<T> arg0, net.minecraft.world.phys.AABB arg1) {
        return handle.getEntitiesOfClass(arg0, arg1);
    }

    @Override
    public <T extends net.minecraft.world.entity.Entity> List<T> getEntitiesOfClass(Class<T> arg0, net.minecraft.world.phys.AABB arg1, Predicate<? super T> arg2) {
        return handle.getEntitiesOfClass(arg0, arg1, arg2);
    }

    @Override
    public net.minecraft.world.entity.player.Player getNearestPlayer(net.minecraft.world.entity.ai.targeting.TargetingConditions arg0, net.minecraft.world.entity.LivingEntity arg1, double arg2, double arg3, double arg4) {
        return handle.getNearestPlayer(arg0, arg1, arg2, arg3, arg4);
    }

    @Override
    public net.minecraft.world.entity.player.Player getNearestPlayer(net.minecraft.world.entity.ai.targeting.TargetingConditions arg0, double arg1, double arg2, double arg3) {
        return handle.getNearestPlayer(arg0, arg1, arg2, arg3);
    }

    @Override
    public net.minecraft.world.entity.player.Player getNearestPlayer(net.minecraft.world.entity.Entity arg0, double arg1) {
        return handle.getNearestPlayer(arg0, arg1);
    }

    @Override
    public net.minecraft.world.entity.player.Player getNearestPlayer(double arg0, double arg1, double arg2, double arg3, Predicate<net.minecraft.world.entity.Entity> arg4) {
        return handle.getNearestPlayer(arg0, arg1, arg2, arg3, arg4);
    }

    @Override
    public net.minecraft.world.entity.player.Player getNearestPlayer(double arg0, double arg1, double arg2, double arg3, boolean arg4) {
        return handle.getNearestPlayer(arg0, arg1, arg2, arg3, arg4);
    }

    @Override
    public net.minecraft.world.entity.player.Player getNearestPlayer(net.minecraft.world.entity.ai.targeting.TargetingConditions arg0, net.minecraft.world.entity.LivingEntity arg1) {
        return handle.getNearestPlayer(arg0, arg1);
    }

    @Override
    public <T extends net.minecraft.world.entity.LivingEntity> T getNearestEntity(Class<? extends T> arg0, net.minecraft.world.entity.ai.targeting.TargetingConditions arg1, net.minecraft.world.entity.LivingEntity arg2, double arg3, double arg4, double arg5, net.minecraft.world.phys.AABB arg6) {
        return handle.getNearestEntity(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    @Override
    public <T extends net.minecraft.world.entity.LivingEntity> T getNearestEntity(List<? extends T> arg0, net.minecraft.world.entity.ai.targeting.TargetingConditions arg1, net.minecraft.world.entity.LivingEntity arg2, double arg3, double arg4, double arg5) {
        return handle.getNearestEntity(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public net.minecraft.world.entity.player.Player getPlayerByUUID(UUID arg0) {
        return handle.getPlayerByUUID(arg0);
    }

    @Override
    public List<net.minecraft.world.entity.player.Player> getNearbyPlayers(net.minecraft.world.entity.ai.targeting.TargetingConditions arg0, net.minecraft.world.entity.LivingEntity arg1, net.minecraft.world.phys.AABB arg2) {
        return handle.getNearbyPlayers(arg0, arg1, arg2);
    }

    @Override
    public <T extends net.minecraft.world.entity.LivingEntity> List<T> getNearbyEntities(Class<T> arg0, net.minecraft.world.entity.ai.targeting.TargetingConditions arg1, net.minecraft.world.entity.LivingEntity arg2, net.minecraft.world.phys.AABB arg3) {
        return handle.getNearbyEntities(arg0, arg1, arg2, arg3);
    }

    @Override
    @Deprecated
    public float getLightLevelDependentMagicValue(net.minecraft.core.BlockPos arg0) {
        return handle.getLightLevelDependentMagicValue(arg0);
    }

    @Override
    public net.minecraft.world.level.BlockGetter getChunkForCollisions(int arg0, int arg1) {
        return handle.getChunkForCollisions(arg0, arg1);
    }

    @Override
    public int getMaxLocalRawBrightness(net.minecraft.core.BlockPos arg0) {
        return handle.getMaxLocalRawBrightness(arg0);
    }

    @Override
    public int getMaxLocalRawBrightness(net.minecraft.core.BlockPos arg0, int arg1) {
        return handle.getMaxLocalRawBrightness(arg0, arg1);
    }

    @Override
    public boolean canSeeSkyFromBelowWater(net.minecraft.core.BlockPos arg0) {
        return handle.canSeeSkyFromBelowWater(arg0);
    }

    @Override
    public float getPathfindingCostFromLightLevels(net.minecraft.core.BlockPos arg0) {
        return handle.getPathfindingCostFromLightLevels(arg0);
    }

    @Override
    public Stream<net.minecraft.world.level.block.state.BlockState> getBlockStatesIfLoaded(net.minecraft.world.phys.AABB arg0) {
        return handle.getBlockStatesIfLoaded(arg0);
    }

    @Override
    public net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> getUncachedNoiseBiome(int arg0, int arg1, int arg2) {
        return handle.getUncachedNoiseBiome(arg0, arg1, arg2);
    }

    @Override
    @Deprecated
    public int getSeaLevel() {
        return handle.getSeaLevel();
    }

    @Override
    public boolean containsAnyLiquid(net.minecraft.world.phys.AABB arg0) {
        return handle.containsAnyLiquid(arg0);
    }

    @Override
    public int getMinBuildHeight() {
        return handle.getMinBuildHeight();
    }

    @Override
    public boolean isWaterAt(net.minecraft.core.BlockPos arg0) {
        return handle.isWaterAt(arg0);
    }

    @Override
    public boolean isEmptyBlock(net.minecraft.core.BlockPos arg0) {
        return handle.isEmptyBlock(arg0);
    }

    @Override
    public boolean isClientSide() {
        return handle.isClientSide();
    }

    @Override
    public net.minecraft.world.level.dimension.DimensionType dimensionType() {
        return handle.dimensionType();
    }

    @Override
    public net.minecraft.world.flag.FeatureFlagSet enabledFeatures() {
        return handle.enabledFeatures();
    }

    @Override
    @Deprecated
    public boolean hasChunkAt(int arg0, int arg1) {
        return handle.hasChunkAt(arg0, arg1);
    }

    @Override
    @Deprecated
    public boolean hasChunkAt(net.minecraft.core.BlockPos arg0) {
        return handle.hasChunkAt(arg0);
    }

    @Override
    public <T> net.minecraft.core.HolderLookup<T> holderLookup(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<? extends T>> arg0) {
        return handle.holderLookup(arg0);
    }

    @Override
    public net.minecraft.core.RegistryAccess registryAccess() {
        return handle.registryAccess();
    }

    @Override
    public net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> getNoiseBiome(int arg0, int arg1, int arg2) {
        return handle.getNoiseBiome(arg0, arg1, arg2);
    }

    @Override
    public int getBlockTint(net.minecraft.core.BlockPos arg0, net.minecraft.world.level.ColorResolver arg1) {
        return handle.getBlockTint(arg0, arg1);
    }

    @Override
    @Deprecated
    public boolean hasChunksAt(net.minecraft.core.BlockPos arg0, net.minecraft.core.BlockPos arg1) {
        return handle.hasChunksAt(arg0, arg1);
    }

    @Override
    @Deprecated
    public boolean hasChunksAt(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        return handle.hasChunksAt(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    @Deprecated
    public boolean hasChunksAt(int arg0, int arg1, int arg2, int arg3) {
        return handle.hasChunksAt(arg0, arg1, arg2, arg3);
    }

    @Override
    public net.minecraft.world.level.chunk.ChunkAccess getChunk(int arg0, int arg1, net.minecraft.world.level.chunk.status.ChunkStatus arg2, boolean arg3) {
        return handle.getChunk(arg0, arg1, arg2, arg3);
    }

    @Override
    public net.minecraft.world.level.chunk.ChunkAccess getChunk(int arg0, int arg1, net.minecraft.world.level.chunk.status.ChunkStatus arg2) {
        return handle.getChunk(arg0, arg1, arg2);
    }

    @Override
    public net.minecraft.world.level.chunk.ChunkAccess getChunk(net.minecraft.core.BlockPos arg0) {
        return handle.getChunk(arg0);
    }

    @Override
    public net.minecraft.world.level.chunk.ChunkAccess getChunk(int arg0, int arg1) {
        return handle.getChunk(arg0, arg1);
    }

    @Override
    public int getHeight(net.minecraft.world.level.levelgen.Heightmap.Types arg0, int arg1, int arg2) {
        return handle.getHeight(arg0, arg1, arg2);
    }

    @Override
    public int getHeight() {
        return handle.getHeight();
    }

    @Override
    public net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> getBiome(net.minecraft.core.BlockPos arg0) {
        return handle.getBiome(arg0);
    }

    @Override
    public int getSkyDarken() {
        return handle.getSkyDarken();
    }

    @Override
    public net.minecraft.world.level.biome.BiomeManager getBiomeManager() {
        return handle.getBiomeManager();
    }

    @Override
    public boolean canSeeSky(net.minecraft.core.BlockPos arg0) {
        return handle.canSeeSky(arg0);
    }

    @Override
    public int getRawBrightness(net.minecraft.core.BlockPos arg0, int arg1) {
        return handle.getRawBrightness(arg0, arg1);
    }

    @Override
    public net.minecraft.world.level.lighting.LevelLightEngine getLightEngine() {
        return handle.getLightEngine();
    }

    @Override
    public int getBrightness(net.minecraft.world.level.LightLayer arg0, net.minecraft.core.BlockPos arg1) {
        return handle.getBrightness(arg0, arg1);
    }

    @Override
    public float getShade(net.minecraft.core.Direction arg0, boolean arg1) {
        return handle.getShade(arg0, arg1);
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity getBlockEntity(net.minecraft.core.BlockPos arg0) {
        return handle.getBlockEntity(arg0);
    }

    @Override
    public double getBlockFloorHeight(net.minecraft.world.phys.shapes.VoxelShape arg0, Supplier<net.minecraft.world.phys.shapes.VoxelShape> arg1) {
        return handle.getBlockFloorHeight(arg0, arg1);
    }

    @Override
    public double getBlockFloorHeight(net.minecraft.core.BlockPos arg0) {
        return handle.getBlockFloorHeight(arg0);
    }

    @Override
    public net.minecraft.world.phys.BlockHitResult clipWithInteractionOverride(net.minecraft.world.phys.Vec3 arg0, net.minecraft.world.phys.Vec3 arg1, net.minecraft.core.BlockPos arg2, net.minecraft.world.phys.shapes.VoxelShape arg3, net.minecraft.world.level.block.state.BlockState arg4) {
        return handle.clipWithInteractionOverride(arg0, arg1, arg2, arg3, arg4);
    }

    @Override
    public net.minecraft.world.level.block.state.BlockState getBlockState(net.minecraft.core.BlockPos arg0) {
        return handle.getBlockState(arg0);
    }

    @Override
    public net.minecraft.world.level.material.FluidState getFluidState(net.minecraft.core.BlockPos arg0) {
        return handle.getFluidState(arg0);
    }

    @Override
    public int getLightEmission(net.minecraft.core.BlockPos arg0) {
        return handle.getLightEmission(arg0);
    }

    @Override
    public net.minecraft.world.phys.BlockHitResult clip(net.minecraft.world.level.ClipContext arg0) {
        return handle.clip(arg0);
    }

    public net.minecraft.world.phys.BlockHitResult clip(net.minecraft.world.level.ClipContext arg0, net.minecraft.core.BlockPos arg1) {
        return handle.clip(arg0, arg1);
    }

    @Override
    public int getMaxLightLevel() {
        return handle.getMaxLightLevel();
    }

    @Override
    public net.minecraft.world.phys.BlockHitResult isBlockInLine(net.minecraft.world.level.ClipBlockStateContext arg0) {
        return handle.isBlockInLine(arg0);
    }

    @Override
    public Stream<net.minecraft.world.level.block.state.BlockState> getBlockStates(net.minecraft.world.phys.AABB arg0) {
        return handle.getBlockStates(arg0);
    }

    @Override
    public boolean isOutsideBuildHeight(int arg0) {
        return handle.isOutsideBuildHeight(arg0);
    }

    @Override
    public boolean isOutsideBuildHeight(net.minecraft.core.BlockPos arg0) {
        return handle.isOutsideBuildHeight(arg0);
    }

    @Override
    public int getSectionIndexFromSectionY(int arg0) {
        return handle.getSectionIndexFromSectionY(arg0);
    }

    @Override
    public int getSectionYFromSectionIndex(int arg0) {
        return handle.getSectionYFromSectionIndex(arg0);
    }

    @Override
    public int getMaxSection() {
        return handle.getMaxSection();
    }

    @Override
    public int getMinSection() {
        return handle.getMinSection();
    }

    @Override
    public int getSectionIndex(int arg0) {
        return handle.getSectionIndex(arg0);
    }

    @Override
    public int getSectionsCount() {
        return handle.getSectionsCount();
    }

    @Override
    public int getMaxBuildHeight() {
        return handle.getMaxBuildHeight();
    }

    @Override
    public boolean isUnobstructed(net.minecraft.world.level.block.state.BlockState arg0, net.minecraft.core.BlockPos arg1, net.minecraft.world.phys.shapes.CollisionContext arg2) {
        return handle.isUnobstructed(arg0, arg1, arg2);
    }

    @Override
    public boolean isUnobstructed(net.minecraft.world.entity.Entity arg0) {
        return handle.isUnobstructed(arg0);
    }

    @Override
    public net.minecraft.world.level.border.WorldBorder getWorldBorder() {
        return handle.getWorldBorder();
    }

    @Override
    public Optional<net.minecraft.world.phys.Vec3> findFreePosition(net.minecraft.world.entity.Entity arg0, net.minecraft.world.phys.shapes.VoxelShape arg1, net.minecraft.world.phys.Vec3 arg2, double arg3, double arg4, double arg5) {
        return handle.findFreePosition(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public Iterable<net.minecraft.world.phys.shapes.VoxelShape> getCollisions(net.minecraft.world.entity.Entity arg0, net.minecraft.world.phys.AABB arg1) {
        return handle.getCollisions(arg0, arg1);
    }

    @Override
    public Iterable<net.minecraft.world.phys.shapes.VoxelShape> getBlockCollisions(net.minecraft.world.entity.Entity arg0, net.minecraft.world.phys.AABB arg1) {
        return handle.getBlockCollisions(arg0, arg1);
    }

    @Override
    public boolean noCollision(net.minecraft.world.phys.AABB arg0) {
        return handle.noCollision(arg0);
    }

    @Override
    public boolean noCollision(net.minecraft.world.entity.Entity arg0) {
        return handle.noCollision(arg0);
    }

    @Override
    public boolean noCollision(net.minecraft.world.entity.Entity arg0, net.minecraft.world.phys.AABB arg1) {
        return handle.noCollision(arg0, arg1);
    }

    @Override
    public boolean collidesWithSuffocatingBlock(net.minecraft.world.entity.Entity arg0, net.minecraft.world.phys.AABB arg1) {
        return handle.collidesWithSuffocatingBlock(arg0, arg1);
    }

    @Override
    public Optional<net.minecraft.core.BlockPos> findSupportingBlock(net.minecraft.world.entity.Entity arg0, net.minecraft.world.phys.AABB arg1) {
        return handle.findSupportingBlock(arg0, arg1);
    }

    @Override
    public int getBestNeighborSignal(net.minecraft.core.BlockPos arg0) {
        return handle.getBestNeighborSignal(arg0);
    }

    @Override
    public int getControlInputSignal(net.minecraft.core.BlockPos arg0, net.minecraft.core.Direction arg1, boolean arg2) {
        return handle.getControlInputSignal(arg0, arg1, arg2);
    }

    @Override
    public int getDirectSignal(net.minecraft.core.BlockPos arg0, net.minecraft.core.Direction arg1) {
        return handle.getDirectSignal(arg0, arg1);
    }

    @Override
    public int getDirectSignalTo(net.minecraft.core.BlockPos arg0) {
        return handle.getDirectSignalTo(arg0);
    }

    @Override
    public boolean hasNeighborSignal(net.minecraft.core.BlockPos arg0) {
        return handle.hasNeighborSignal(arg0);
    }

    @Override
    public boolean hasSignal(net.minecraft.core.BlockPos arg0, net.minecraft.core.Direction arg1) {
        return handle.hasSignal(arg0, arg1);
    }

    @Override
    public int getSignal(net.minecraft.core.BlockPos arg0, net.minecraft.core.Direction arg1) {
        return handle.getSignal(arg0, arg1);
    }

    @Override
    public boolean isStateAtPosition(net.minecraft.core.BlockPos arg0, Predicate<net.minecraft.world.level.block.state.BlockState> arg1) {
        return handle.isStateAtPosition(arg0, arg1);
    }

    @Override
    public boolean isFluidAtPosition(net.minecraft.core.BlockPos arg0, Predicate<net.minecraft.world.level.material.FluidState> arg1) {
        return handle.isFluidAtPosition(arg0, arg1);
    }

    public boolean addFreshEntity(net.minecraft.world.entity.Entity arg0, CreatureSpawnEvent.SpawnReason arg1) {
        return handle.addFreshEntity(arg0, arg1);
    }

    @Override
    public boolean addFreshEntity(net.minecraft.world.entity.Entity arg0) {
        return handle.addFreshEntity(arg0);
    }

    @Override
    public boolean removeBlock(net.minecraft.core.BlockPos arg0, boolean arg1) {
        return handle.removeBlock(arg0, arg1);
    }

    @Override
    public boolean destroyBlock(net.minecraft.core.BlockPos arg0, boolean arg1, net.minecraft.world.entity.Entity arg2, int arg3) {
        return handle.destroyBlock(arg0, arg1, arg2, arg3);
    }

    @Override
    public boolean destroyBlock(net.minecraft.core.BlockPos arg0, boolean arg1, net.minecraft.world.entity.Entity arg2) {
        return handle.destroyBlock(arg0, arg1, arg2);
    }

    @Override
    public boolean destroyBlock(net.minecraft.core.BlockPos arg0, boolean arg1) {
        return handle.destroyBlock(arg0, arg1);
    }

    @Override
    public boolean setBlock(net.minecraft.core.BlockPos arg0, net.minecraft.world.level.block.state.BlockState arg1, int arg2) {
        return handle.setBlock(arg0, arg1, arg2);
    }

    @Override
    public boolean setBlock(net.minecraft.core.BlockPos arg0, net.minecraft.world.level.block.state.BlockState arg1, int arg2, int arg3) {
        return handle.setBlock(arg0, arg1, arg2, arg3);
    }

    @Override
    public float getTimeOfDay(float arg0) {
        return handle.getTimeOfDay(arg0);
    }

    @Override
    public float getMoonBrightness() {
        return handle.getMoonBrightness();
    }

    @Override
    public int getMoonPhase() {
        return handle.getMoonPhase();
    }
}
