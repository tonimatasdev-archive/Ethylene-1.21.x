package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import javax.annotation.Nullable;

import net.ethylenemc.EthyleneStatic;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftWorld;

public final class CraftBlockStates {

    private abstract static class BlockStateFactory<B extends CraftBlockState> {

        public final Class<B> blockStateType;

        public BlockStateFactory(Class<B> blockStateType) {
            this.blockStateType = blockStateType;
        }

        // The given world can be null for unplaced BlockStates.
        // If the world is not null and the given block data is a tile entity, the given tile entity is expected to not be null.
        // Otherwise, the given tile entity may or may not be null.
        // If the given tile entity is not null, its position and block data are expected to match the given block position and block data.
        // In some situations, such as during chunk generation, the tile entity's world may be null, even if the given world is not null.
        // If the tile entity's world is not null, it is expected to match the given world.
        public abstract B createBlockState(World world, net.minecraft.core.BlockPos blockPosition, net.minecraft.world.level.block.state.BlockState blockData, net.minecraft.world.level.block.entity.BlockEntity tileEntity);
    }

    private static class BlockEntityStateFactory<T extends net.minecraft.world.level.block.entity.BlockEntity, B extends CraftBlockEntityState<T>> extends BlockStateFactory<B> {

        private final BiFunction<World, T, B> blockStateConstructor;
        private final BiFunction<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, T> tileEntityConstructor;

        protected BlockEntityStateFactory(Class<B> blockStateType, BiFunction<World, T, B> blockStateConstructor, BiFunction<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, T> tileEntityConstructor) {
            super(blockStateType);
            this.blockStateConstructor = blockStateConstructor;
            this.tileEntityConstructor = tileEntityConstructor;
        }

        @Override
        public final B createBlockState(World world, net.minecraft.core.BlockPos blockPosition, net.minecraft.world.level.block.state.BlockState blockData, net.minecraft.world.level.block.entity.BlockEntity tileEntity) {
            if (world != null) {
                Preconditions.checkState(tileEntity != null, "Tile is null, asynchronous access? %s", CraftBlock.at(((CraftWorld) world).getHandle(), blockPosition));
            } else if (tileEntity == null) {
                tileEntity = this.createTileEntity(blockPosition, blockData);
            }
            return this.createBlockState(world, (T) tileEntity);
        }

        private T createTileEntity(net.minecraft.core.BlockPos blockPosition, net.minecraft.world.level.block.state.BlockState blockData) {
            return tileEntityConstructor.apply(blockPosition, blockData);
        }

        private B createBlockState(World world, T tileEntity) {
            return blockStateConstructor.apply(world, tileEntity);
        }
    }

    private static final Map<Material, BlockStateFactory<?>> FACTORIES = new HashMap<>();
    private static final BlockStateFactory<?> DEFAULT_FACTORY = new BlockStateFactory<CraftBlockState>(CraftBlockState.class) {
        @Override
        public CraftBlockState createBlockState(World world, net.minecraft.core.BlockPos blockPosition, net.minecraft.world.level.block.state.BlockState blockData, net.minecraft.world.level.block.entity.BlockEntity tileEntity) {
            // SPIGOT-6754, SPIGOT-6817: Restore previous behaviour for tile entities with removed blocks (loot generation post-destroy)
            if (tileEntity != null) {
                // block with unhandled net.minecraft.world.level.block.entity.BlockEntity:
                return new CraftBlockEntityState<>(world, tileEntity);
            }
            Preconditions.checkState(tileEntity == null, "Unexpected BlockState for %s", CraftBlockType.minecraftToBukkit(blockData.getBlock()));
            return new CraftBlockState(world, blockPosition, blockData);
        }
    };

    static {
        register(
                Arrays.asList(
                        Material.ACACIA_SIGN,
                        Material.ACACIA_WALL_SIGN,
                        Material.BAMBOO_SIGN,
                        Material.BAMBOO_WALL_SIGN,
                        Material.BIRCH_SIGN,
                        Material.BIRCH_WALL_SIGN,
                        Material.CHERRY_SIGN,
                        Material.CHERRY_WALL_SIGN,
                        Material.CRIMSON_SIGN,
                        Material.CRIMSON_WALL_SIGN,
                        Material.DARK_OAK_SIGN,
                        Material.DARK_OAK_WALL_SIGN,
                        Material.JUNGLE_SIGN,
                        Material.JUNGLE_WALL_SIGN,
                        Material.MANGROVE_SIGN,
                        Material.MANGROVE_WALL_SIGN,
                        Material.OAK_SIGN,
                        Material.OAK_WALL_SIGN,
                        Material.SPRUCE_SIGN,
                        Material.SPRUCE_WALL_SIGN,
                        Material.WARPED_SIGN,
                        Material.WARPED_WALL_SIGN
                ), CraftSign.class, CraftSign::new, net.minecraft.world.level.block.entity.SignBlockEntity::new
        );

        register(
                Arrays.asList(
                        Material.ACACIA_HANGING_SIGN,
                        Material.ACACIA_WALL_HANGING_SIGN,
                        Material.BAMBOO_HANGING_SIGN,
                        Material.BAMBOO_WALL_HANGING_SIGN,
                        Material.BIRCH_HANGING_SIGN,
                        Material.BIRCH_WALL_HANGING_SIGN,
                        Material.CHERRY_HANGING_SIGN,
                        Material.CHERRY_WALL_HANGING_SIGN,
                        Material.CRIMSON_HANGING_SIGN,
                        Material.CRIMSON_WALL_HANGING_SIGN,
                        Material.DARK_OAK_HANGING_SIGN,
                        Material.DARK_OAK_WALL_HANGING_SIGN,
                        Material.JUNGLE_HANGING_SIGN,
                        Material.JUNGLE_WALL_HANGING_SIGN,
                        Material.MANGROVE_HANGING_SIGN,
                        Material.MANGROVE_WALL_HANGING_SIGN,
                        Material.OAK_HANGING_SIGN,
                        Material.OAK_WALL_HANGING_SIGN,
                        Material.SPRUCE_HANGING_SIGN,
                        Material.SPRUCE_WALL_HANGING_SIGN,
                        Material.WARPED_HANGING_SIGN,
                        Material.WARPED_WALL_HANGING_SIGN
                ), CraftHangingSign.class, CraftHangingSign::new, net.minecraft.world.level.block.entity.HangingSignBlockEntity::new
        );

        register(
                Arrays.asList(
                        Material.CREEPER_HEAD,
                        Material.CREEPER_WALL_HEAD,
                        Material.DRAGON_HEAD,
                        Material.DRAGON_WALL_HEAD,
                        Material.PIGLIN_HEAD,
                        Material.PIGLIN_WALL_HEAD,
                        Material.PLAYER_HEAD,
                        Material.PLAYER_WALL_HEAD,
                        Material.SKELETON_SKULL,
                        Material.SKELETON_WALL_SKULL,
                        Material.WITHER_SKELETON_SKULL,
                        Material.WITHER_SKELETON_WALL_SKULL,
                        Material.ZOMBIE_HEAD,
                        Material.ZOMBIE_WALL_HEAD
                ), CraftSkull.class, CraftSkull::new, net.minecraft.world.level.block.entity.SkullBlockEntity::new
        );

        register(
                Arrays.asList(
                        Material.COMMAND_BLOCK,
                        Material.REPEATING_COMMAND_BLOCK,
                        Material.CHAIN_COMMAND_BLOCK
                ), CraftCommandBlock.class, CraftCommandBlock::new, net.minecraft.world.level.block.entity.CommandBlockEntity::new
        );

        register(
                Arrays.asList(
                        Material.BLACK_BANNER,
                        Material.BLACK_WALL_BANNER,
                        Material.BLUE_BANNER,
                        Material.BLUE_WALL_BANNER,
                        Material.BROWN_BANNER,
                        Material.BROWN_WALL_BANNER,
                        Material.CYAN_BANNER,
                        Material.CYAN_WALL_BANNER,
                        Material.GRAY_BANNER,
                        Material.GRAY_WALL_BANNER,
                        Material.GREEN_BANNER,
                        Material.GREEN_WALL_BANNER,
                        Material.LIGHT_BLUE_BANNER,
                        Material.LIGHT_BLUE_WALL_BANNER,
                        Material.LIGHT_GRAY_BANNER,
                        Material.LIGHT_GRAY_WALL_BANNER,
                        Material.LIME_BANNER,
                        Material.LIME_WALL_BANNER,
                        Material.MAGENTA_BANNER,
                        Material.MAGENTA_WALL_BANNER,
                        Material.ORANGE_BANNER,
                        Material.ORANGE_WALL_BANNER,
                        Material.PINK_BANNER,
                        Material.PINK_WALL_BANNER,
                        Material.PURPLE_BANNER,
                        Material.PURPLE_WALL_BANNER,
                        Material.RED_BANNER,
                        Material.RED_WALL_BANNER,
                        Material.WHITE_BANNER,
                        Material.WHITE_WALL_BANNER,
                        Material.YELLOW_BANNER,
                        Material.YELLOW_WALL_BANNER
                ), CraftBanner.class, CraftBanner::new, net.minecraft.world.level.block.entity.BannerBlockEntity::new
        );

        register(
                Arrays.asList(
                        Material.SHULKER_BOX,
                        Material.WHITE_SHULKER_BOX,
                        Material.ORANGE_SHULKER_BOX,
                        Material.MAGENTA_SHULKER_BOX,
                        Material.LIGHT_BLUE_SHULKER_BOX,
                        Material.YELLOW_SHULKER_BOX,
                        Material.LIME_SHULKER_BOX,
                        Material.PINK_SHULKER_BOX,
                        Material.GRAY_SHULKER_BOX,
                        Material.LIGHT_GRAY_SHULKER_BOX,
                        Material.CYAN_SHULKER_BOX,
                        Material.PURPLE_SHULKER_BOX,
                        Material.BLUE_SHULKER_BOX,
                        Material.BROWN_SHULKER_BOX,
                        Material.GREEN_SHULKER_BOX,
                        Material.RED_SHULKER_BOX,
                        Material.BLACK_SHULKER_BOX
                ), CraftShulkerBox.class, CraftShulkerBox::new, net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity::new
        );

        register(
                Arrays.asList(
                        Material.BLACK_BED,
                        Material.BLUE_BED,
                        Material.BROWN_BED,
                        Material.CYAN_BED,
                        Material.GRAY_BED,
                        Material.GREEN_BED,
                        Material.LIGHT_BLUE_BED,
                        Material.LIGHT_GRAY_BED,
                        Material.LIME_BED,
                        Material.MAGENTA_BED,
                        Material.ORANGE_BED,
                        Material.PINK_BED,
                        Material.PURPLE_BED,
                        Material.RED_BED,
                        Material.WHITE_BED,
                        Material.YELLOW_BED
                ), CraftBed.class, CraftBed::new, net.minecraft.world.level.block.entity.BedBlockEntity::new
        );

        register(
                Arrays.asList(
                        Material.BEEHIVE,
                        Material.BEE_NEST
                ), CraftBeehive.class, CraftBeehive::new, net.minecraft.world.level.block.entity.BeehiveBlockEntity::new
        );

        register(
                Arrays.asList(
                        Material.CAMPFIRE,
                        Material.SOUL_CAMPFIRE
                ), CraftCampfire.class, CraftCampfire::new, net.minecraft.world.level.block.entity.CampfireBlockEntity::new
        );

        register(Material.BARREL, CraftBarrel.class, CraftBarrel::new, net.minecraft.world.level.block.entity.BarrelBlockEntity::new);
        register(Material.BEACON, CraftBeacon.class, CraftBeacon::new, net.minecraft.world.level.block.entity.BeaconBlockEntity::new);
        register(Material.BELL, CraftBell.class, CraftBell::new, net.minecraft.world.level.block.entity.BellBlockEntity::new);
        register(Material.BLAST_FURNACE, CraftBlastFurnace.class, CraftBlastFurnace::new, net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity::new);
        register(Material.BREWING_STAND, CraftBrewingStand.class, CraftBrewingStand::new, net.minecraft.world.level.block.entity.BrewingStandBlockEntity::new);
        register(Material.CHEST, CraftChest.class, CraftChest::new, net.minecraft.world.level.block.entity.ChestBlockEntity::new);
        register(Material.CHISELED_BOOKSHELF, CraftChiseledBookshelf.class, CraftChiseledBookshelf::new, net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity::new);
        register(Material.COMPARATOR, CraftComparator.class, CraftComparator::new, net.minecraft.world.level.block.entity.ComparatorBlockEntity::new);
        register(Material.CONDUIT, CraftConduit.class, CraftConduit::new, net.minecraft.world.level.block.entity.ConduitBlockEntity::new);
        register(Material.DAYLIGHT_DETECTOR, CraftDaylightDetector.class, CraftDaylightDetector::new, net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity::new);
        register(Material.DECORATED_POT, CraftDecoratedPot.class, CraftDecoratedPot::new, net.minecraft.world.level.block.entity.DecoratedPotBlockEntity::new);
        register(Material.DISPENSER, CraftDispenser.class, CraftDispenser::new, net.minecraft.world.level.block.entity.DispenserBlockEntity::new);
        register(Material.DROPPER, CraftDropper.class, CraftDropper::new, net.minecraft.world.level.block.entity.DropperBlockEntity::new);
        register(Material.ENCHANTING_TABLE, CraftEnchantingTable.class, CraftEnchantingTable::new, net.minecraft.world.level.block.entity.EnchantingTableBlockEntity::new);
        register(Material.ENDER_CHEST, CraftEnderChest.class, CraftEnderChest::new, net.minecraft.world.level.block.entity.EnderChestBlockEntity::new);
        register(Material.END_GATEWAY, CraftEndGateway.class, CraftEndGateway::new, net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity::new);
        register(Material.END_PORTAL, CraftEndPortal.class, CraftEndPortal::new, net.minecraft.world.level.block.entity.TheEndPortalBlockEntity::new);
        register(Material.FURNACE, CraftFurnaceFurnace.class, CraftFurnaceFurnace::new, net.minecraft.world.level.block.entity.FurnaceBlockEntity::new);
        register(Material.HOPPER, CraftHopper.class, CraftHopper::new, net.minecraft.world.level.block.entity.HopperBlockEntity::new);
        register(Material.JIGSAW, CraftJigsaw.class, CraftJigsaw::new, net.minecraft.world.level.block.entity.JigsawBlockEntity::new);
        register(Material.JUKEBOX, CraftJukebox.class, CraftJukebox::new, net.minecraft.world.level.block.entity.JukeboxBlockEntity::new);
        register(Material.LECTERN, CraftLectern.class, CraftLectern::new, net.minecraft.world.level.block.entity.LecternBlockEntity::new);
        register(Material.MOVING_PISTON, CraftMovingPiston.class, CraftMovingPiston::new, net.minecraft.world.level.block.piston.PistonMovingBlockEntity::new);
        register(Material.SCULK_CATALYST, CraftSculkCatalyst.class, CraftSculkCatalyst::new, net.minecraft.world.level.block.entity.SculkCatalystBlockEntity::new);
        register(Material.CALIBRATED_SCULK_SENSOR, CraftCalibratedSculkSensor.class, CraftCalibratedSculkSensor::new, net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity::new);
        register(Material.SCULK_SENSOR, CraftSculkSensor.class, CraftSculkSensor::new, net.minecraft.world.level.block.entity.SculkSensorBlockEntity::new);
        register(Material.SCULK_SHRIEKER, CraftSculkShrieker.class, CraftSculkShrieker::new, net.minecraft.world.level.block.entity.SculkShriekerBlockEntity::new);
        register(Material.SMOKER, CraftSmoker.class, CraftSmoker::new, net.minecraft.world.level.block.entity.SmokerBlockEntity::new);
        register(Material.SPAWNER, CraftCreatureSpawner.class, CraftCreatureSpawner::new, net.minecraft.world.level.block.entity.SpawnerBlockEntity::new);
        register(Material.STRUCTURE_BLOCK, CraftStructureBlock.class, CraftStructureBlock::new, net.minecraft.world.level.block.entity.StructureBlockEntity::new);
        register(Material.SUSPICIOUS_SAND, CraftSuspiciousSand.class, CraftSuspiciousSand::new, net.minecraft.world.level.block.entity.BrushableBlockEntity::new);
        register(Material.SUSPICIOUS_GRAVEL, CraftBrushableBlock.class, CraftBrushableBlock::new, net.minecraft.world.level.block.entity.BrushableBlockEntity::new);
        register(Material.TRAPPED_CHEST, CraftChest.class, CraftChest::new, net.minecraft.world.level.block.entity.TrappedChestBlockEntity::new);
        register(Material.CRAFTER, CraftCrafter.class, CraftCrafter::new, net.minecraft.world.level.block.entity.CrafterBlockEntity::new);
        register(Material.TRIAL_SPAWNER, CraftTrialSpawner.class, CraftTrialSpawner::new, net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity::new);
        register(Material.VAULT, CraftVault.class, CraftVault::new, net.minecraft.world.level.block.entity.vault.VaultBlockEntity::new);
    }

    private static void register(Material blockType, BlockStateFactory<?> factory) {
        FACTORIES.put(blockType, factory);
    }

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity, B extends CraftBlockEntityState<T>> void register(
            Material blockType,
            Class<B> blockStateType,
            BiFunction<World, T, B> blockStateConstructor,
            BiFunction<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, T> tileEntityConstructor
    ) {
        register(Collections.singletonList(blockType), blockStateType, blockStateConstructor, tileEntityConstructor);
    }

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity, B extends CraftBlockEntityState<T>> void register(
            List<Material> blockTypes,
            Class<B> blockStateType,
            BiFunction<World, T, B> blockStateConstructor,
            BiFunction<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, T> tileEntityConstructor
    ) {
        BlockStateFactory<B> factory = new BlockEntityStateFactory<>(blockStateType, blockStateConstructor, tileEntityConstructor);
        for (Material blockType : blockTypes) {
            register(blockType, factory);
        }
    }

    private static BlockStateFactory<?> getFactory(Material material) {
        return FACTORIES.getOrDefault(material, DEFAULT_FACTORY);
    }

    public static Class<? extends CraftBlockState> getBlockStateType(Material material) {
        Preconditions.checkNotNull(material, "material is null");
        return getFactory(material).blockStateType;
    }

    public static net.minecraft.world.level.block.entity.BlockEntity createNewTileEntity(Material material) {
        BlockStateFactory<?> factory = getFactory(material);

        if (factory instanceof BlockEntityStateFactory) {
            return ((BlockEntityStateFactory<?, ?>) factory).createTileEntity(net.minecraft.core.BlockPos.ZERO, CraftBlockType.bukkitToMinecraft(material).defaultBlockState());
        }

        return null;
    }

    public static BlockState getBlockState(Block block) {
        Preconditions.checkNotNull(block, "block is null");
        CraftBlock craftBlock = (CraftBlock) block;
        CraftWorld world = (CraftWorld) block.getWorld();
        net.minecraft.core.BlockPos blockPosition = craftBlock.getPosition();
        net.minecraft.world.level.block.state.BlockState blockData = craftBlock.getNMS();
        net.minecraft.world.level.block.entity.BlockEntity tileEntity = craftBlock.getHandle().getBlockEntity(blockPosition);
        CraftBlockState blockState = getBlockState(world, blockPosition, blockData, tileEntity);
        blockState.setWorldHandle(craftBlock.getHandle()); // Inject the block's generator access
        return blockState;
    }

    @Deprecated
    public static BlockState getBlockState(net.minecraft.core.BlockPos blockPosition, Material material, @Nullable net.minecraft.nbt.CompoundTag blockEntityTag) {
        return getBlockState(EthyleneStatic.getDefaultRegistryAccess(), blockPosition, material, blockEntityTag);
    }

    public static BlockState getBlockState(net.minecraft.world.level.LevelReader world, net.minecraft.core.BlockPos blockPosition, Material material, @Nullable net.minecraft.nbt.CompoundTag blockEntityTag) {
        return getBlockState(world.registryAccess(), blockPosition, material, blockEntityTag);
    }

    public static BlockState getBlockState(net.minecraft.core.RegistryAccess registry, net.minecraft.core.BlockPos blockPosition, Material material, @Nullable net.minecraft.nbt.CompoundTag blockEntityTag) {
        Preconditions.checkNotNull(material, "material is null");
        net.minecraft.world.level.block.state.BlockState blockData = CraftBlockType.bukkitToMinecraft(material).defaultBlockState();
        return getBlockState(registry, blockPosition, blockData, blockEntityTag);
    }

    @Deprecated
    public static BlockState getBlockState(net.minecraft.world.level.block.state.BlockState blockData, @Nullable net.minecraft.nbt.CompoundTag blockEntityTag) {
        return getBlockState(EthyleneStatic.getDefaultRegistryAccess(), net.minecraft.core.BlockPos.ZERO, blockData, blockEntityTag);
    }

    public static BlockState getBlockState(net.minecraft.world.level.LevelReader world, net.minecraft.core.BlockPos blockPosition, net.minecraft.world.level.block.state.BlockState blockData, @Nullable net.minecraft.nbt.CompoundTag blockEntityTag) {
        return getBlockState(world.registryAccess(), blockPosition, blockData, blockEntityTag);
    }

    public static BlockState getBlockState(net.minecraft.core.RegistryAccess registry, net.minecraft.core.BlockPos blockPosition, net.minecraft.world.level.block.state.BlockState blockData, @Nullable net.minecraft.nbt.CompoundTag blockEntityTag) {
        Preconditions.checkNotNull(blockPosition, "blockPosition is null");
        Preconditions.checkNotNull(blockData, "blockData is null");
        net.minecraft.world.level.block.entity.BlockEntity tileEntity = (blockEntityTag == null) ? null : net.minecraft.world.level.block.entity.BlockEntity.loadStatic(blockPosition, blockData, blockEntityTag, registry);
        return getBlockState(null, blockPosition, blockData, tileEntity);
    }

    // See BlockStateFactory#createBlockState(World, net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.block.entity.BlockEntity)
    private static CraftBlockState getBlockState(World world, net.minecraft.core.BlockPos blockPosition, net.minecraft.world.level.block.state.BlockState blockData, net.minecraft.world.level.block.entity.BlockEntity tileEntity) {
        Material material = CraftBlockType.minecraftToBukkit(blockData.getBlock());
        BlockStateFactory<?> factory;
        // For some types of net.minecraft.world.level.block.entity.BlockEntity blocks (eg. moving pistons), Minecraft may in some situations (eg. when using Block#setType or the
        // setBlock command) not create a corresponding net.minecraft.world.level.block.entity.BlockEntity in the world. We return a normal BlockState in this case.
        if (world != null && tileEntity == null && isTileEntityOptional(material)) {
            factory = DEFAULT_FACTORY;
        } else {
            factory = getFactory(material);
        }
        return factory.createBlockState(world, blockPosition, blockData, tileEntity);
    }

    public static boolean isTileEntityOptional(Material material) {
        return material == Material.MOVING_PISTON;
    }

    // This ignores tile entity data.
    public static CraftBlockState getBlockState(net.minecraft.world.level.LevelAccessor world, net.minecraft.core.BlockPos pos) {
        return new CraftBlockState(CraftBlock.at(world, pos));
    }

    // This ignores tile entity data.
    public static CraftBlockState getBlockState(net.minecraft.world.level.LevelAccessor world, net.minecraft.core.BlockPos pos, int flag) {
        return new CraftBlockState(CraftBlock.at(world, pos), flag);
    }

    private CraftBlockStates() {
    }
}
