package org.bukkit.craftbukkit.block;

import net.ethylenemc.interfaces.world.level.EthyleneLevel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.util.CraftLocation;

public final class CapturedBlockState extends CraftBlockState {

    private final boolean treeBlock;

    public CapturedBlockState(Block block, int flag, boolean treeBlock) {
        super(block, flag);

        this.treeBlock = treeBlock;
    }

    protected CapturedBlockState(CapturedBlockState state, Location location) {
        super(state, location);
        this.treeBlock = state.treeBlock;
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);

        // Probably no longer needed with the extra #updatedTree method,
        // but leave if here for now in case a plugin for whatever reason relies on this.
        addBees();

        return result;
    }

    private void updatedTree() {
        // SPIGOT-7248 - Manual update to avoid physics where appropriate
        // SPIGOT-7572 - Move SPIGOT-7248 fix from nms ItemStack to here, to allow bee generation in nests
        world.getHandle().setBlock(CraftLocation.toBlockPosition(getLocation()), getHandle(), getFlag());

        addBees();
    }

    private void addBees() {
        // SPIGOT-5537: Horrible hack to manually add bees given net.minecraft.world.level.Level.captureTreeGeneration does not support tiles
        if (this.treeBlock && getType() == Material.BEE_NEST) {
            net.minecraft.world.level.WorldGenLevel generatoraccessseed = this.world.getHandle();
            net.minecraft.core.BlockPos blockposition1 = this.getPosition();
            net.minecraft.util.RandomSource random = generatoraccessseed.getRandom();

            // Begin copied block from WorldGenFeatureTreeBeehive
            net.minecraft.world.level.block.entity.BlockEntity tileentity = generatoraccessseed.getBlockEntity(blockposition1);

            if (tileentity instanceof net.minecraft.world.level.block.entity.BeehiveBlockEntity) {
                net.minecraft.world.level.block.entity.BeehiveBlockEntity tileentitybeehive = (net.minecraft.world.level.block.entity.BeehiveBlockEntity) tileentity;
                int j = 2 + random.nextInt(2);

                for (int k = 0; k < j; ++k) {
                    tileentitybeehive.storeBee(net.minecraft.world.level.block.entity.BeehiveBlockEntity.Occupant.create(random.nextInt(599)));
                }
            }
            // End copied block
        }
    }

    @Override
    public CapturedBlockState copy() {
        return new CapturedBlockState(this, null);
    }

    @Override
    public CapturedBlockState copy(Location location) {
        return new CapturedBlockState(this, location);
    }

    public static CapturedBlockState getBlockState(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, int flag) {
        return new CapturedBlockState(((EthyleneLevel) world).getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), flag, false);
    }

    public static CapturedBlockState getTreeBlockState(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, int flag) {
        return new CapturedBlockState(((EthyleneLevel) world).getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), flag, true);
    }

    public static void setBlockState(BlockState blockState) {
        if (blockState instanceof CapturedBlockState capturedBlockState) {
            capturedBlockState.updatedTree();
        } else {
            blockState.update(true);
        }
    }
}
