package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.ethylenemc.EthyleneCaptures;
import net.ethylenemc.interfaces.world.level.EthyleneLevel;
import net.ethylenemc.interfaces.world.level.EthyleneWorldGenLevel;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftFluidCollisionMode;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftRayTraceResult;
import org.bukkit.craftbukkit.util.CraftVoxelShape;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class CraftBlock implements Block {
    private final net.minecraft.world.level.LevelAccessor world;
    private final net.minecraft.core.BlockPos position;

    public CraftBlock(net.minecraft.world.level.LevelAccessor world, net.minecraft.core.BlockPos position) {
        this.world = world;
        this.position = position.immutable();
    }

    public static CraftBlock at(net.minecraft.world.level.LevelAccessor world, net.minecraft.core.BlockPos position) {
        return new CraftBlock(world, position);
    }

    public net.minecraft.world.level.block.state.BlockState getNMS() {
        return world.getBlockState(position);
    }

    public net.minecraft.core.BlockPos getPosition() {
        return position;
    }

    public net.minecraft.world.level.LevelAccessor getHandle() {
        return world;
    }

    @Override
    public World getWorld() {
        return ((EthyleneLevel) ((EthyleneWorldGenLevel) world).getMinecraftWorld()).getWorld();
    }

    public CraftWorld getCraftWorld() {
        return (CraftWorld) getWorld();
    }

    @Override
    public Location getLocation() {
        return CraftLocation.toBukkit(position, getWorld());
    }

    @Override
    public Location getLocation(Location loc) {
        if (loc != null) {
            loc.setWorld(getWorld());
            loc.setX(position.getX());
            loc.setY(position.getY());
            loc.setZ(position.getZ());
            loc.setYaw(0);
            loc.setPitch(0);
        }

        return loc;
    }

    public BlockVector getVector() {
        return new BlockVector(getX(), getY(), getZ());
    }

    @Override
    public int getX() {
        return position.getX();
    }

    @Override
    public int getY() {
        return position.getY();
    }

    @Override
    public int getZ() {
        return position.getZ();
    }

    @Override
    public Chunk getChunk() {
        return getWorld().getChunkAt(this);
    }

    public void setData(final byte data) {
        setData(data, 3);
    }

    public void setData(final byte data, boolean applyPhysics) {
        if (applyPhysics) {
            setData(data, 3);
        } else {
            setData(data, 2);
        }
    }

    private void setData(final byte data, int flag) {
        world.setBlock(position, CraftMagicNumbers.getBlock(getType(), data), flag);
    }

    @Override
    public byte getData() {
        net.minecraft.world.level.block.state.BlockState blockData = world.getBlockState(position);
        return CraftMagicNumbers.toLegacyData(blockData);
    }

    @Override
    public BlockData getBlockData() {
        return CraftBlockData.fromData(getNMS());
    }

    @Override
    public void setType(final Material type) {
        setType(type, true);
    }

    @Override
    public void setType(Material type, boolean applyPhysics) {
        Preconditions.checkArgument(type != null, "Material cannot be null");
        setBlockData(type.createBlockData(), applyPhysics);
    }

    @Override
    public void setBlockData(BlockData data) {
        setBlockData(data, true);
    }

    @Override
    public void setBlockData(BlockData data, boolean applyPhysics) {
        Preconditions.checkArgument(data != null, "BlockData cannot be null");
        setTypeAndData(((CraftBlockData) data).getState(), applyPhysics);
    }

    boolean setTypeAndData(final net.minecraft.world.level.block.state.BlockState blockData, final boolean applyPhysics) {
        return setTypeAndData(world, position, getNMS(), blockData, applyPhysics);
    }

    public static boolean setTypeAndData(net.minecraft.world.level.LevelAccessor world, net.minecraft.core.BlockPos position, net.minecraft.world.level.block.state.BlockState old, net.minecraft.world.level.block.state.BlockState blockData, boolean applyPhysics) {
        // SPIGOT-611: need to do this to prevent glitchiness. Easier to handle this here (like /setblock) than to fix weirdness in tile entity cleanup
        if (old.hasBlockEntity() && blockData.getBlock() != old.getBlock()) { // SPIGOT-3725 remove old tile entity if block changes
            // SPIGOT-4612: faster - just clear tile
            if (world instanceof net.minecraft.world.level.Level) {
                ((net.minecraft.world.level.Level) world).removeBlockEntity(position);
            } else {
                world.setBlock(position, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 0);
            }
        }

        if (applyPhysics) {
            return world.setBlock(position, blockData, 3);
        } else {
            boolean success = world.setBlock(position, blockData, 2 | 16 | 1024); // NOTIFY | NO_OBSERVER | NO_PLACE (custom)
            if (success && world instanceof net.minecraft.world.level.Level) {
                ((EthyleneWorldGenLevel) world).getMinecraftWorld().sendBlockUpdated(
                        position,
                        old,
                        blockData,
                        3
                );
            }
            return success;
        }
    }

    @Override
    public Material getType() {
        return CraftBlockType.minecraftToBukkit(world.getBlockState(position).getBlock());
    }

    @Override
    public byte getLightLevel() {
        return (byte) ((EthyleneWorldGenLevel) world).getMinecraftWorld().getMaxLocalRawBrightness(position);
    }

    @Override
    public byte getLightFromSky() {
        return (byte) world.getBrightness(net.minecraft.world.level.LightLayer.SKY, position);
    }

    @Override
    public byte getLightFromBlocks() {
        return (byte) world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, position);
    }

    public Block getFace(final BlockFace face) {
        return getRelative(face, 1);
    }

    public Block getFace(final BlockFace face, final int distance) {
        return getRelative(face, distance);
    }

    @Override
    public Block getRelative(final int modX, final int modY, final int modZ) {
        return getWorld().getBlockAt(getX() + modX, getY() + modY, getZ() + modZ);
    }

    @Override
    public Block getRelative(BlockFace face) {
        return getRelative(face, 1);
    }

    @Override
    public Block getRelative(BlockFace face, int distance) {
        return getRelative(face.getModX() * distance, face.getModY() * distance, face.getModZ() * distance);
    }

    @Override
    public BlockFace getFace(final Block block) {
        BlockFace[] values = BlockFace.values();

        for (BlockFace face : values) {
            if ((this.getX() + face.getModX() == block.getX()) && (this.getY() + face.getModY() == block.getY()) && (this.getZ() + face.getModZ() == block.getZ())) {
                return face;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "CraftBlock{pos=" + position + ",type=" + getType() + ",data=" + getNMS() + ",fluid=" + world.getFluidState(position) + '}';
    }

    public static BlockFace notchToBlockFace(net.minecraft.core.Direction notch) {
        if (notch == null) {
            return BlockFace.SELF;
        }
        switch (notch) {
            case DOWN:
                return BlockFace.DOWN;
            case UP:
                return BlockFace.UP;
            case NORTH:
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.SOUTH;
            case WEST:
                return BlockFace.WEST;
            case EAST:
                return BlockFace.EAST;
            default:
                return BlockFace.SELF;
        }
    }

    public static net.minecraft.core.Direction blockFaceToNotch(BlockFace face) {
        if (face == null) {
            return null;
        }
        switch (face) {
            case DOWN:
                return net.minecraft.core.Direction.DOWN;
            case UP:
                return net.minecraft.core.Direction.UP;
            case NORTH:
                return net.minecraft.core.Direction.NORTH;
            case SOUTH:
                return net.minecraft.core.Direction.SOUTH;
            case WEST:
                return net.minecraft.core.Direction.WEST;
            case EAST:
                return net.minecraft.core.Direction.EAST;
            default:
                return null;
        }
    }

    @Override
    public BlockState getState() {
        return CraftBlockStates.getBlockState(this);
    }

    @Override
    public Biome getBiome() {
        return getWorld().getBiome(getX(), getY(), getZ());
    }

    @Override
    public void setBiome(Biome bio) {
        getWorld().setBiome(getX(), getY(), getZ(), bio);
    }

    @Override
    public double getTemperature() {
        return world.getBiome(position).value().getTemperature(position);
    }

    @Override
    public double getHumidity() {
        return getWorld().getHumidity(getX(), getY(), getZ());
    }

    @Override
    public boolean isBlockPowered() {
        return ((EthyleneWorldGenLevel) world).getMinecraftWorld().getDirectSignalTo(position) > 0;
    }

    @Override
    public boolean isBlockIndirectlyPowered() {
        return ((EthyleneWorldGenLevel) world).getMinecraftWorld().hasNeighborSignal(position);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CraftBlock other)) {
            return false;
        }

        return this.position.equals(other.position) && this.getWorld().equals(other.getWorld());
    }

    @Override
    public int hashCode() {
        return this.position.hashCode() ^ this.getWorld().hashCode();
    }

    @Override
    public boolean isBlockFacePowered(BlockFace face) {
        return ((EthyleneWorldGenLevel) world).getMinecraftWorld().hasSignal(position, blockFaceToNotch(face));
    }

    @Override
    public boolean isBlockFaceIndirectlyPowered(BlockFace face) {
        int power = ((EthyleneWorldGenLevel) world).getMinecraftWorld().getSignal(position, blockFaceToNotch(face));

        Block relative = getRelative(face);
        if (relative.getType() == Material.REDSTONE_WIRE) {
            return Math.max(power, relative.getData()) > 0;
        }

        return power > 0;
    }

    @Override
    public int getBlockPower(BlockFace face) {
        int power = 0;
        net.minecraft.world.level.Level world = ((EthyleneWorldGenLevel) this.world).getMinecraftWorld();
        int x = getX();
        int y = getY();
        int z = getZ();
        if ((face == BlockFace.DOWN || face == BlockFace.SELF) && world.hasSignal(new net.minecraft.core.BlockPos(x, y - 1, z), net.minecraft.core.Direction.DOWN)) power = getPower(power, world.getBlockState(new net.minecraft.core.BlockPos(x, y - 1, z)));
        if ((face == BlockFace.UP || face == BlockFace.SELF) && world.hasSignal(new net.minecraft.core.BlockPos(x, y + 1, z), net.minecraft.core.Direction.UP)) power = getPower(power, world.getBlockState(new net.minecraft.core.BlockPos(x, y + 1, z)));
        if ((face == BlockFace.EAST || face == BlockFace.SELF) && world.hasSignal(new net.minecraft.core.BlockPos(x + 1, y, z), net.minecraft.core.Direction.EAST)) power = getPower(power, world.getBlockState(new net.minecraft.core.BlockPos(x + 1, y, z)));
        if ((face == BlockFace.WEST || face == BlockFace.SELF) && world.hasSignal(new net.minecraft.core.BlockPos(x - 1, y, z), net.minecraft.core.Direction.WEST)) power = getPower(power, world.getBlockState(new net.minecraft.core.BlockPos(x - 1, y, z)));
        if ((face == BlockFace.NORTH || face == BlockFace.SELF) && world.hasSignal(new net.minecraft.core.BlockPos(x, y, z - 1), net.minecraft.core.Direction.NORTH)) power = getPower(power, world.getBlockState(new net.minecraft.core.BlockPos(x, y, z - 1)));
        if ((face == BlockFace.SOUTH || face == BlockFace.SELF) && world.hasSignal(new net.minecraft.core.BlockPos(x, y, z + 1), net.minecraft.core.Direction.SOUTH)) power = getPower(power, world.getBlockState(new net.minecraft.core.BlockPos(x, y, z + 1)));
        return power > 0 ? power : (face == BlockFace.SELF ? isBlockIndirectlyPowered() : isBlockFaceIndirectlyPowered(face)) ? 15 : 0;
    }

    private static int getPower(int i, net.minecraft.world.level.block.state.BlockState iblockdata) {
        if (!iblockdata.is(net.minecraft.world.level.block.Blocks.REDSTONE_WIRE)) {
            return i;
        } else {
            int j = iblockdata.getValue(net.minecraft.world.level.block.RedStoneWireBlock.POWER);

            return j > i ? j : i;
        }
    }

    @Override
    public int getBlockPower() {
        return getBlockPower(BlockFace.SELF);
    }

    @Override
    public boolean isEmpty() {
        return getNMS().isAir();
    }

    @Override
    public boolean isLiquid() {
        return getNMS().liquid();
    }

    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        return PistonMoveReaction.getById(getNMS().getPistonPushReaction().ordinal());
    }

    @Override
    public boolean breakNaturally() {
        return breakNaturally(null);
    }

    @Override
    public boolean breakNaturally(ItemStack item) {
        // Order matters here, need to drop before setting to air so skulls can get their data
        net.minecraft.world.level.block.state.BlockState iblockdata = this.getNMS();
        net.minecraft.world.level.block.Block block = iblockdata.getBlock();
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        boolean result = false;

        // Modelled off EntityHuman#hasBlock
        if (block != net.minecraft.world.level.block.Blocks.AIR && (item == null || !iblockdata.requiresCorrectToolForDrops() || nmsItem.isCorrectToolForDrops(iblockdata))) {
            net.minecraft.world.level.block.Block.dropResources(iblockdata, ((EthyleneWorldGenLevel) world).getMinecraftWorld(), position, world.getBlockEntity(position), null, nmsItem);
            result = true;
        }

        // SPIGOT-6778: Directly call setBlock instead of setTypeAndData, so that the tile entiy is not removed and custom remove logic is run.
        return world.setBlock(position, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3) && result;
    }

    @Override
    public boolean applyBoneMeal(BlockFace face) {
        net.minecraft.core.Direction direction = blockFaceToNotch(face);
        BlockFertilizeEvent event = null;
        net.minecraft.server.level.ServerLevel world = getCraftWorld().getHandle();
        net.minecraft.world.item.context.UseOnContext context = new net.minecraft.world.item.context.UseOnContext(world, null, net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.Items.BONE_MEAL.getDefaultInstance(), new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.ZERO, direction, getPosition(), false));

        // SPIGOT-6895: Call StructureGrowEvent and BlockFertilizeEvent
        ((EthyleneLevel) world).captureTreeGeneration(true);
        net.minecraft.world.InteractionResult result = net.minecraft.world.item.BoneMealItem.applyBonemeal(context);
        ((EthyleneLevel) world).captureTreeGeneration(false);

        if (((EthyleneLevel) world).capturedBlockStates().size() > 0) {
            TreeType treeType = EthyleneCaptures.treeType;
            EthyleneCaptures.treeType = null;
            List<BlockState> blocks = new ArrayList<>(((EthyleneLevel) world).capturedBlockStates().values());
            ((EthyleneLevel) world).capturedBlockStates().clear();
            StructureGrowEvent structureEvent = null;

            if (treeType != null) {
                structureEvent = new StructureGrowEvent(getLocation(), treeType, true, null, blocks);
                Bukkit.getPluginManager().callEvent(structureEvent);
            }

            event = new BlockFertilizeEvent(CraftBlock.at(world, getPosition()), null, blocks);
            event.setCancelled(structureEvent != null && structureEvent.isCancelled());
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                for (BlockState blockstate : blocks) {
                    blockstate.update(true);
                }
            }
        }

        return result == net.minecraft.world.InteractionResult.SUCCESS && (event == null || !event.isCancelled());
    }

    @Override
    public Collection<ItemStack> getDrops() {
        return getDrops(null);
    }

    @Override
    public Collection<ItemStack> getDrops(ItemStack item) {
        return getDrops(item, null);
    }

    @Override
    public Collection<ItemStack> getDrops(ItemStack item, Entity entity) {
        net.minecraft.world.level.block.state.BlockState iblockdata = getNMS();
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);

        // Modelled off EntityHuman#hasBlock
        if (item == null || CraftBlockData.isPreferredTool(iblockdata, nms)) {
            return net.minecraft.world.level.block.Block.getDrops(iblockdata, (net.minecraft.server.level.ServerLevel) ((EthyleneWorldGenLevel) world).getMinecraftWorld(), position, world.getBlockEntity(position), entity == null ? null : ((CraftEntity) entity).getHandle(), nms)
                    .stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isPreferredTool(ItemStack item) {
        net.minecraft.world.level.block.state.BlockState iblockdata = getNMS();
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
        return CraftBlockData.isPreferredTool(iblockdata, nms);
    }

    @Override
    public float getBreakSpeed(Player player) {
        Preconditions.checkArgument(player != null, "player cannot be null");
        return getNMS().getDestroyProgress(((CraftPlayer) player).getHandle(), world, position);
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        getCraftWorld().getBlockMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return getCraftWorld().getBlockMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return getCraftWorld().getBlockMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        getCraftWorld().getBlockMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    public boolean isPassable() {
        return this.getNMS().getCollisionShape(world, position).isEmpty();
    }

    @Override
    public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
        Preconditions.checkArgument(start != null, "Location start cannot be null");
        Preconditions.checkArgument(this.getWorld().equals(start.getWorld()), "Location start cannot be a different world");
        start.checkFinite();

        Preconditions.checkArgument(direction != null, "Vector direction cannot be null");
        direction.checkFinite();
        Preconditions.checkArgument(direction.lengthSquared() > 0, "Direction's magnitude (%s) must be greater than 0", direction.lengthSquared());

        Preconditions.checkArgument(fluidCollisionMode != null, "FluidCollisionMode cannot be null");
        if (maxDistance < 0.0D) {
            return null;
        }

        Vector dir = direction.clone().normalize().multiply(maxDistance);
        net.minecraft.world.phys.Vec3 startPos = CraftLocation.toVec3D(start);
        net.minecraft.world.phys.Vec3 endPos = startPos.add(dir.getX(), dir.getY(), dir.getZ());

        net.minecraft.world.phys.HitResult nmsHitResult = world.clip(new net.minecraft.world.level.ClipContext(startPos, endPos, net.minecraft.world.level.ClipContext.Block.OUTLINE, CraftFluidCollisionMode.toNMS(fluidCollisionMode), net.minecraft.world.phys.shapes.CollisionContext.empty()), position);
        return CraftRayTraceResult.fromNMS(this.getWorld(), nmsHitResult);
    }

    @Override
    public BoundingBox getBoundingBox() {
        net.minecraft.world.phys.shapes.VoxelShape shape = getNMS().getShape(world, position);

        if (shape.isEmpty()) {
            return new BoundingBox(); // Return an empty bounding box if the block has no dimension
        }

        net.minecraft.world.phys.AABB aabb = shape.bounds();
        return new BoundingBox(getX() + aabb.minX, getY() + aabb.minY, getZ() + aabb.minZ, getX() + aabb.maxX, getY() + aabb.maxY, getZ() + aabb.maxZ);
    }

    @Override
    public org.bukkit.util.VoxelShape getCollisionShape() {
        net.minecraft.world.phys.shapes.VoxelShape shape = getNMS().getCollisionShape(world, position);
        return new CraftVoxelShape(shape);
    }

    @Override
    public boolean canPlace(BlockData data) {
        Preconditions.checkArgument(data != null, "BlockData cannot be null");
        net.minecraft.world.level.block.state.BlockState iblockdata = ((CraftBlockData) data).getState();
        net.minecraft.world.level.Level world = ((EthyleneWorldGenLevel) this.world).getMinecraftWorld();

        return iblockdata.canSurvive(world, this.position);
    }

    @Override
    public String getTranslationKey() {
        return getNMS().getBlock().getDescriptionId();
    }
}
