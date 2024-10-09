package org.bukkit.craftbukkit.event;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import net.ethylenemc.interfaces.world.entity.EthyleneEntity;
import net.ethylenemc.interfaces.world.inventory.EthyleneAbstractContainerMenu;
import net.ethylenemc.interfaces.world.level.EthyleneLevel;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftExplosionResult;
import org.bukkit.craftbukkit.CraftLootTable;
import org.bukkit.craftbukkit.CraftRaid;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftStatistic;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.CraftBlockStates;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.*;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.util.CraftVector;
import org.bukkit.entity.*;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BellResonateEvent;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.block.VaultDisplayItemEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.ArrowBodyCountChangeEvent;
import org.bukkit.event.entity.BatToggleSleepEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.event.entity.EntityKnockbackEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntitySpellCastEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.StriderTemperatureChangeEvent;
import org.bukkit.event.entity.TrialSpawnerSpawnEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerBucketFishEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRecipeBookClickEvent;
import org.bukkit.event.player.PlayerRecipeBookSettingsChangeEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.inventory.view.MerchantView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

public class CraftEventFactory {

    // helper methods
    private static boolean canBuild(net.minecraft.server.level.ServerLevel world, Player player, int x, int z) {
        int spawnSize = Bukkit.getServer().getSpawnRadius();

        if (world.dimension() != net.minecraft.world.level.Level.OVERWORLD) return true;
        if (spawnSize <= 0) return true;
        if (((CraftServer) Bukkit.getServer()).getHandle().getOps().isEmpty()) return true;
        if (player.isOp()) return true;

        net.minecraft.core.BlockPos chunkcoordinates = world.getSharedSpawnPos();

        int distanceFromSpawn = Math.max(Math.abs(x - chunkcoordinates.getX()), Math.abs(z - chunkcoordinates.getZ()));
        return distanceFromSpawn > spawnSize;
    }

    public static <T extends Event> T callEvent(T event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * PlayerSignOpenEvent
     */
    public static boolean callPlayerSignOpenEvent(net.minecraft.world.entity.player.Player player, net.minecraft.world.level.block.entity.SignBlockEntity tileEntitySign, boolean front, PlayerSignOpenEvent.Cause cause) {
        final Block block = CraftBlock.at(tileEntitySign.getLevel(), tileEntitySign.getBlockPos());
        final Sign sign = (Sign) CraftBlockStates.getBlockState(block);
        final Side side = (front) ? Side.FRONT : Side.BACK;
        return callPlayerSignOpenEvent((Player) ((EthyleneEntity) player).getBukkitEntity(), sign, side, cause);
    }

    /**
     * PlayerSignOpenEvent
     */
    public static boolean callPlayerSignOpenEvent(Player player, Sign sign, Side side, PlayerSignOpenEvent.Cause cause) {
        final PlayerSignOpenEvent event = new PlayerSignOpenEvent(player, sign, side, cause);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    /**
     * PlayerBedEnterEvent
     */
    public static Either<net.minecraft.world.entity.player.Player.BedSleepingProblem, net.minecraft.util.Unit> callPlayerBedEnterEvent(net.minecraft.world.entity.player.Player player, net.minecraft.core.BlockPos bed, Either<net.minecraft.world.entity.player.Player.BedSleepingProblem, net.minecraft.util.Unit> nmsBedResult) {
        BedEnterResult bedEnterResult = nmsBedResult.mapBoth(new Function<net.minecraft.world.entity.player.Player.BedSleepingProblem, BedEnterResult>() {
            @Override
            public BedEnterResult apply(net.minecraft.world.entity.player.Player.BedSleepingProblem t) {
                switch (t) {
                    case NOT_POSSIBLE_HERE:
                        return BedEnterResult.NOT_POSSIBLE_HERE;
                    case NOT_POSSIBLE_NOW:
                        return BedEnterResult.NOT_POSSIBLE_NOW;
                    case TOO_FAR_AWAY:
                        return BedEnterResult.TOO_FAR_AWAY;
                    case NOT_SAFE:
                        return BedEnterResult.NOT_SAFE;
                    default:
                        return BedEnterResult.OTHER_PROBLEM;
                }
            }
        }, t -> BedEnterResult.OK).map(java.util.function.Function.identity(), java.util.function.Function.identity());

        PlayerBedEnterEvent event = new PlayerBedEnterEvent((Player) ((EthyleneEntity) player).getBukkitEntity(), CraftBlock.at(player.level(), bed), bedEnterResult);
        Bukkit.getServer().getPluginManager().callEvent(event);

        Result result = event.useBed();
        if (result == Result.ALLOW) {
            return Either.right(net.minecraft.util.Unit.INSTANCE);
        } else if (result == Result.DENY) {
            return Either.left(net.minecraft.world.entity.player.Player.BedSleepingProblem.OTHER_PROBLEM);
        }

        return nmsBedResult;
    }

    /**
     * net.minecraft.world.entity.Entity Enter Love Mode Event
     */
    public static EntityEnterLoveModeEvent callEntityEnterLoveModeEvent(net.minecraft.world.entity.player.Player entityHuman, net.minecraft.world.entity.animal.Animal entityAnimal, int loveTicks) {
        EntityEnterLoveModeEvent entityEnterLoveModeEvent = new EntityEnterLoveModeEvent((Animals) ((EthyleneEntity) entityAnimal).getBukkitEntity(), entityHuman != null ? (HumanEntity) ((EthyleneEntity) entityHuman).getBukkitEntity() : null, loveTicks);
        Bukkit.getPluginManager().callEvent(entityEnterLoveModeEvent);
        return entityEnterLoveModeEvent;
    }

    /**
     * Player Harvest Block Event
     */
    public static PlayerHarvestBlockEvent callPlayerHarvestBlockEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos blockposition, net.minecraft.world.entity.player.Player who, net.minecraft.world.InteractionHand enumhand, List<net.minecraft.world.item.ItemStack> itemsToHarvest) {
        List<org.bukkit.inventory.ItemStack> bukkitItemsToHarvest = new ArrayList<>(itemsToHarvest.stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList()));
        Player player = (Player) ((EthyleneEntity) who).getBukkitEntity();
        PlayerHarvestBlockEvent playerHarvestBlockEvent = new PlayerHarvestBlockEvent(player, CraftBlock.at(world, blockposition), CraftEquipmentSlot.getHand(enumhand), bukkitItemsToHarvest);
        Bukkit.getPluginManager().callEvent(playerHarvestBlockEvent);
        return playerHarvestBlockEvent;
    }

    /**
     * Player Fish Bucket Event
     */
    public static PlayerBucketEntityEvent callPlayerFishBucketEvent(net.minecraft.world.entity.LivingEntity fish, net.minecraft.world.entity.player.Player entityHuman, net.minecraft.world.item.ItemStack originalBucket, net.minecraft.world.item.ItemStack entityBucket, net.minecraft.world.InteractionHand enumhand) {
        Player player = (Player) ((EthyleneEntity) entityHuman).getBukkitEntity();
        EquipmentSlot hand = CraftEquipmentSlot.getHand(enumhand);

        PlayerBucketEntityEvent event;
        if (fish instanceof net.minecraft.world.entity.animal.AbstractFish) {
            event = new PlayerBucketFishEvent(player, (Fish) ((EthyleneEntity) fish).getBukkitEntity(), CraftItemStack.asBukkitCopy(originalBucket), CraftItemStack.asBukkitCopy(entityBucket), hand);
        } else {
            event = new PlayerBucketEntityEvent(player, ((EthyleneEntity) fish).getBukkitEntity(), CraftItemStack.asBukkitCopy(originalBucket), CraftItemStack.asBukkitCopy(entityBucket), hand);
        }
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Trade Index Change Event
     */
    public static TradeSelectEvent callTradeSelectEvent(net.minecraft.server.level.ServerPlayer player, int newIndex, net.minecraft.world.inventory.MerchantMenu merchant) {
        TradeSelectEvent tradeSelectEvent = new TradeSelectEvent((MerchantView) ((EthyleneAbstractContainerMenu) merchant).getBukkitView(), newIndex);
        Bukkit.getPluginManager().callEvent(tradeSelectEvent);
        return tradeSelectEvent;
    }

    public static boolean handleBellRingEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos position, net.minecraft.core.Direction direction, net.minecraft.world.entity.Entity entity) {
        Block block = CraftBlock.at(world, position);
        BlockFace bukkitDirection = CraftBlock.notchToBlockFace(direction);
        BellRingEvent event = new BellRingEvent(block, bukkitDirection, (entity != null) ? ((EthyleneEntity) entity).getBukkitEntity() : null);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static Stream<net.minecraft.world.entity.LivingEntity> handleBellResonateEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos position, List<LivingEntity> bukkitEntities) {
        Block block = CraftBlock.at(world, position);
        BellResonateEvent event = new BellResonateEvent(block, bukkitEntities);
        Bukkit.getPluginManager().callEvent(event);
        return event.getResonatedEntities().stream().map((bukkitEntity) -> ((CraftLivingEntity) bukkitEntity).getHandle());
    }

    /**
     * Block place methods
     */
    public static BlockMultiPlaceEvent callBlockMultiPlaceEvent(net.minecraft.server.level.ServerLevel world, net.minecraft.world.entity.player.Player who, net.minecraft.world.InteractionHand hand, List<BlockState> blockStates, int clickedX, int clickedY, int clickedZ) {
        CraftWorld craftWorld = ((EthyleneLevel) world).getWorld();
        CraftServer craftServer = ((EthyleneLevel) world).getCraftServer();
        Player player = (Player) ((EthyleneEntity) who).getBukkitEntity();

        Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);

        boolean canBuild = true;
        for (int i = 0; i < blockStates.size(); i++) {
            if (!canBuild(world, player, blockStates.get(i).getX(), blockStates.get(i).getZ())) {
                canBuild = false;
                break;
            }
        }

        org.bukkit.inventory.ItemStack item;
        if (hand == net.minecraft.world.InteractionHand.MAIN_HAND) {
            item = player.getInventory().getItemInMainHand();
        } else {
            item = player.getInventory().getItemInOffHand();
        }

        BlockMultiPlaceEvent event = new BlockMultiPlaceEvent(blockStates, blockClicked, item, player, canBuild);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static BlockPlaceEvent callBlockPlaceEvent(net.minecraft.server.level.ServerLevel world, net.minecraft.world.entity.player.Player who, net.minecraft.world.InteractionHand hand, BlockState replacedBlockState, int clickedX, int clickedY, int clickedZ) {
        CraftWorld craftWorld = ((EthyleneLevel) world).getWorld();
        CraftServer craftServer = ((EthyleneLevel) world).getCraftServer();

        Player player = (Player) ((EthyleneEntity) who).getBukkitEntity();

        Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
        Block placedBlock = replacedBlockState.getBlock();

        boolean canBuild = canBuild(world, player, placedBlock.getX(), placedBlock.getZ());

        org.bukkit.inventory.ItemStack item;
        EquipmentSlot equipmentSlot;
        if (hand == net.minecraft.world.InteractionHand.MAIN_HAND) {
            item = player.getInventory().getItemInMainHand();
            equipmentSlot = EquipmentSlot.HAND;
        } else {
            item = player.getInventory().getItemInOffHand();
            equipmentSlot = EquipmentSlot.OFF_HAND;
        }

        BlockPlaceEvent event = new BlockPlaceEvent(placedBlock, replacedBlockState, blockClicked, item, player, canBuild, equipmentSlot);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static void handleBlockDropItemEvent(Block block, BlockState state, net.minecraft.server.level.ServerPlayer player, List<net.minecraft.world.entity.item.ItemEntity> items) {
        BlockDropItemEvent event = new BlockDropItemEvent(block, state, ((EthyleneEntity) player).getBukkitEntity(), Lists.transform(items, (item) -> (org.bukkit.entity.Item) ((EthyleneEntity) item).getBukkitEntity()));
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            for (net.minecraft.world.entity.item.ItemEntity item : items) {
                item.level().addFreshEntity(item);
            }
        }
    }

    public static EntityPlaceEvent callEntityPlaceEvent(net.minecraft.world.item.context.UseOnContext itemactioncontext, net.minecraft.world.entity.Entity entity) {
        return callEntityPlaceEvent(itemactioncontext.getLevel(), itemactioncontext.getClickedPos(), itemactioncontext.getClickedFace(), itemactioncontext.getPlayer(), entity, itemactioncontext.getHand());
    }

    public static EntityPlaceEvent callEntityPlaceEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos clickPosition, net.minecraft.core.Direction clickedFace, net.minecraft.world.entity.player.Player human, net.minecraft.world.entity.Entity entity, net.minecraft.world.InteractionHand enumhand) {
        Player who = (human == null) ? null : (Player) ((EthyleneEntity) human).getBukkitEntity();
        org.bukkit.block.Block blockClicked = CraftBlock.at(world, clickPosition);
        org.bukkit.block.BlockFace blockFace = org.bukkit.craftbukkit.block.CraftBlock.notchToBlockFace(clickedFace);

        EntityPlaceEvent event = new EntityPlaceEvent(((EthyleneEntity) entity).getBukkitEntity(), who, blockClicked, blockFace, CraftEquipmentSlot.getHand(enumhand));
        ((EthyleneLevel) entity.level()).getCraftServer().getPluginManager().callEvent(event);

        return event;
    }

    /**
     * Bucket methods
     */
    public static PlayerBucketEmptyEvent callPlayerBucketEmptyEvent(net.minecraft.server.level.ServerLevel world, net.minecraft.world.entity.player.Player who, net.minecraft.core.BlockPos changed, net.minecraft.core.BlockPos clicked, net.minecraft.core.Direction clickedFace, net.minecraft.world.item.ItemStack itemInHand, net.minecraft.world.InteractionHand enumhand) {
        return (PlayerBucketEmptyEvent) getPlayerBucketEvent(false, world, who, changed, clicked, clickedFace, itemInHand, net.minecraft.world.item.Items.BUCKET, enumhand);
    }

    public static PlayerBucketFillEvent callPlayerBucketFillEvent(net.minecraft.server.level.ServerLevel world, net.minecraft.world.entity.player.Player who, net.minecraft.core.BlockPos changed, net.minecraft.core.BlockPos clicked, net.minecraft.core.Direction clickedFace, net.minecraft.world.item.ItemStack itemInHand, net.minecraft.world.item.Item bucket, net.minecraft.world.InteractionHand enumhand) {
        return (PlayerBucketFillEvent) getPlayerBucketEvent(true, world, who, clicked, changed, clickedFace, itemInHand, bucket, enumhand);
    }

    private static PlayerEvent getPlayerBucketEvent(boolean isFilling, net.minecraft.server.level.ServerLevel world, net.minecraft.world.entity.player.Player who, net.minecraft.core.BlockPos changed, net.minecraft.core.BlockPos clicked, net.minecraft.core.Direction clickedFace, net.minecraft.world.item.ItemStack itemstack, net.minecraft.world.item.Item item, net.minecraft.world.InteractionHand enumhand) {
        Player player = (Player) ((EthyleneEntity) who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asNewCraftStack(item);
        Material bucket = CraftItemType.minecraftToBukkit(itemstack.getItem());

        CraftServer craftServer = (CraftServer) player.getServer();

        Block block = CraftBlock.at(world, changed);
        Block blockClicked = CraftBlock.at(world, clicked);
        BlockFace blockFace = CraftBlock.notchToBlockFace(clickedFace);
        EquipmentSlot hand = CraftEquipmentSlot.getHand(enumhand);

        PlayerEvent event;
        if (isFilling) {
            event = new PlayerBucketFillEvent(player, block, blockClicked, blockFace, bucket, itemInHand, hand);
            ((PlayerBucketFillEvent) event).setCancelled(!canBuild(world, player, changed.getX(), changed.getZ()));
        } else {
            event = new PlayerBucketEmptyEvent(player, block, blockClicked, blockFace, bucket, itemInHand, hand);
            ((PlayerBucketEmptyEvent) event).setCancelled(!canBuild(world, player, changed.getX(), changed.getZ()));
        }

        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * Player Interact event
     */
    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.world.entity.player.Player who, Action action, net.minecraft.world.item.ItemStack itemstack, net.minecraft.world.InteractionHand hand) {
        if (action != Action.LEFT_CLICK_AIR && action != Action.RIGHT_CLICK_AIR) {
            throw new AssertionError(String.format("%s performing %s with %s", who, action, itemstack));
        }
        return callPlayerInteractEvent(who, action, null, net.minecraft.core.Direction.SOUTH, itemstack, hand);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.world.entity.player.Player who, Action action, net.minecraft.core.BlockPos position, net.minecraft.core.Direction direction, net.minecraft.world.item.ItemStack itemstack, net.minecraft.world.InteractionHand hand) {
        return callPlayerInteractEvent(who, action, position, direction, itemstack, false, hand, null);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.world.entity.player.Player who, Action action, net.minecraft.core.BlockPos position, net.minecraft.core.Direction direction, net.minecraft.world.item.ItemStack itemstack, boolean cancelledBlock, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.Vec3 targetPos) {
        Player player = (who == null) ? null : (Player) ((EthyleneEntity) who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

        Vector clickedPos = null;
        if (position != null && targetPos != null) {
            clickedPos = CraftVector.toBukkit(targetPos.subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(position)));
        }

        CraftWorld craftWorld = (CraftWorld) player.getWorld();
        CraftServer craftServer = (CraftServer) player.getServer();

        Block blockClicked = null;
        if (position != null) {
            blockClicked = craftWorld.getBlockAt(position.getX(), position.getY(), position.getZ());
        } else {
            switch (action) {
                case LEFT_CLICK_BLOCK:
                    action = Action.LEFT_CLICK_AIR;
                    break;
                case RIGHT_CLICK_BLOCK:
                    action = Action.RIGHT_CLICK_AIR;
                    break;
            }
        }
        BlockFace blockFace = CraftBlock.notchToBlockFace(direction);

        if (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0) {
            itemInHand = null;
        }

        PlayerInteractEvent event = new PlayerInteractEvent(player, action, itemInHand, blockClicked, blockFace, (hand == null) ? null : ((hand == net.minecraft.world.InteractionHand.OFF_HAND) ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND), clickedPos);
        if (cancelledBlock) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * EntityTransformEvent
     */
    public static EntityTransformEvent callEntityTransformEvent(net.minecraft.world.entity.LivingEntity original, net.minecraft.world.entity.LivingEntity coverted, EntityTransformEvent.TransformReason transformReason) {
        return callEntityTransformEvent(original, Collections.singletonList(coverted), transformReason);
    }

    /**
     * EntityTransformEvent
     */
    public static EntityTransformEvent callEntityTransformEvent(net.minecraft.world.entity.LivingEntity original, List<net.minecraft.world.entity.LivingEntity> convertedList, EntityTransformEvent.TransformReason convertType) {
        List<org.bukkit.entity.Entity> list = new ArrayList<>();
        for (net.minecraft.world.entity.LivingEntity entityLiving : convertedList) {
            list.add(((EthyleneEntity) entityLiving).getBukkitEntity());
        }

        EntityTransformEvent event = new EntityTransformEvent(((EthyleneEntity) original).getBukkitEntity(), list, convertType);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * EntityShootBowEvent
     */
    public static EntityShootBowEvent callEntityShootBowEvent(net.minecraft.world.entity.LivingEntity who, net.minecraft.world.item.ItemStack bow, net.minecraft.world.item.ItemStack consumableItem, net.minecraft.world.entity.Entity entityArrow, net.minecraft.world.InteractionHand hand, float force, boolean consumeItem) {
        LivingEntity shooter = (LivingEntity) ((EthyleneEntity) who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(bow);
        CraftItemStack itemConsumable = CraftItemStack.asCraftMirror(consumableItem);
        org.bukkit.entity.Entity arrow = ((EthyleneEntity) entityArrow).getBukkitEntity();
        EquipmentSlot handSlot = (hand == net.minecraft.world.InteractionHand.MAIN_HAND) ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;

        if (itemInHand != null && (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0)) {
            itemInHand = null;
        }

        EntityShootBowEvent event = new EntityShootBowEvent(shooter, itemInHand, itemConsumable, arrow, handSlot, force, consumeItem);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * VillagerCareerChangeEvent
     */
    public static VillagerCareerChangeEvent callVillagerCareerChangeEvent(net.minecraft.world.entity.npc.Villager vilager, Profession future, VillagerCareerChangeEvent.ChangeReason reason) {
        VillagerCareerChangeEvent event = new VillagerCareerChangeEvent((Villager) ((EthyleneEntity) vilager).getBukkitEntity(), future, reason);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * BlockDamageEvent
     */
    public static BlockDamageEvent callBlockDamageEvent(net.minecraft.server.level.ServerPlayer who, net.minecraft.core.BlockPos pos, net.minecraft.world.item.ItemStack itemstack, boolean instaBreak) {
        Player player = (Player) ((EthyleneEntity) who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

        Block blockClicked = CraftBlock.at(who.level(), pos);

        BlockDamageEvent event = new BlockDamageEvent(player, blockClicked, itemInHand, instaBreak);
        player.getServer().getPluginManager().callEvent(event);

        return event;
    }

    public static BlockDamageAbortEvent callBlockDamageAbortEvent(net.minecraft.server.level.ServerPlayer who, net.minecraft.core.BlockPos pos, net.minecraft.world.item.ItemStack itemstack) {
        Player player = (Player) ((EthyleneEntity) who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

        Block blockClicked = CraftBlock.at(who.level(), pos);

        BlockDamageAbortEvent event = new BlockDamageAbortEvent(player, blockClicked, itemInHand);
        player.getServer().getPluginManager().callEvent(event);

        return event;
    }

    public static boolean doEntityAddEventCalling(net.minecraft.world.level.Level world, net.minecraft.world.entity.Entity entity, SpawnReason spawnReason) {
        if (entity == null) return false;

        org.bukkit.event.Cancellable event = null;
        if (entity instanceof net.minecraft.world.entity.LivingEntity && !(entity instanceof net.minecraft.server.level.ServerPlayer)) {
            boolean isAnimal = entity instanceof net.minecraft.world.entity.animal.Animal || entity instanceof net.minecraft.world.entity.animal.WaterAnimal || entity instanceof net.minecraft.world.entity.animal.AbstractGolem;
            boolean isMonster = entity instanceof net.minecraft.world.entity.monster.Monster || entity instanceof net.minecraft.world.entity.monster.Ghast || entity instanceof net.minecraft.world.entity.monster.Slime;
            boolean isNpc = entity instanceof net.minecraft.world.entity.npc.Npc;

            if (spawnReason != SpawnReason.CUSTOM) {
                if (isAnimal && !((EthyleneLevel) world).getWorld().getAllowAnimals() || isMonster && !((EthyleneLevel) world).getWorld().getAllowMonsters() || isNpc && !((EthyleneLevel) world).getCraftServer().getServer().areNpcsEnabled()) {
                    ((EthyleneEntity) entity).discard(null); // Add Bukkit remove cause
                    return false;
                }
            }

            event = CraftEventFactory.callCreatureSpawnEvent((net.minecraft.world.entity.LivingEntity) entity, spawnReason);
        } else if (entity instanceof net.minecraft.world.entity.item.ItemEntity) {
            event = CraftEventFactory.callItemSpawnEvent((net.minecraft.world.entity.item.ItemEntity) entity);
        } else if (((EthyleneEntity) entity).getBukkitEntity() instanceof org.bukkit.entity.Projectile) {
            // Not all projectiles extend EntityProjectile, so check for Bukkit interface instead
            event = CraftEventFactory.callProjectileLaunchEvent(entity);
        } else if (((EthyleneEntity) entity).getBukkitEntity() instanceof org.bukkit.entity.Vehicle) {
            event = CraftEventFactory.callVehicleCreateEvent(entity);
        } else if (((EthyleneEntity) entity).getBukkitEntity() instanceof org.bukkit.entity.LightningStrike) {
            LightningStrikeEvent.Cause cause = LightningStrikeEvent.Cause.UNKNOWN;
            switch (spawnReason) {
                case COMMAND:
                    cause = LightningStrikeEvent.Cause.COMMAND;
                    break;
                case CUSTOM:
                    cause = LightningStrikeEvent.Cause.CUSTOM;
                    break;
                case SPAWNER:
                    cause = LightningStrikeEvent.Cause.SPAWNER;
                    break;
            }
            // This event is called in nms-patches for common causes like Weather, Trap or Trident (SpawnReason.DEFAULT) then can ignore this cases for avoid two calls to this event
            if (cause == LightningStrikeEvent.Cause.UNKNOWN && spawnReason == SpawnReason.DEFAULT) {
                return true;
            }
            event = CraftEventFactory.callLightningStrikeEvent((LightningStrike) ((EthyleneEntity) entity).getBukkitEntity(), cause);
        } else if (!(entity instanceof net.minecraft.server.level.ServerPlayer)) {
            event = CraftEventFactory.callEntitySpawnEvent(entity);
        }

        if (event != null && (event.isCancelled() || entity.isRemoved())) {
            net.minecraft.world.entity.Entity vehicle = entity.getVehicle();
            if (vehicle != null) {
                ((EthyleneEntity) vehicle).discard(null); // Add Bukkit remove cause
            }
            for (net.minecraft.world.entity.Entity passenger : entity.getIndirectPassengers()) {
                ((EthyleneEntity) passenger).discard(null); // Add Bukkit remove cause
            }
            ((EthyleneEntity) entity).discard(null); // Add Bukkit remove cause
            return false;
        }

        return true;
    }

    /**
     * EntitySpawnEvent
     */
    public static EntitySpawnEvent callEntitySpawnEvent(net.minecraft.world.entity.Entity entity) {
        org.bukkit.entity.Entity bukkitEntity = ((EthyleneEntity) entity).getBukkitEntity();

        EntitySpawnEvent event = new EntitySpawnEvent(bukkitEntity);
        bukkitEntity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * CreatureSpawnEvent
     */
    public static CreatureSpawnEvent callCreatureSpawnEvent(net.minecraft.world.entity.LivingEntity entityliving, SpawnReason spawnReason) {
        LivingEntity entity = (LivingEntity) ((EthyleneEntity) entityliving).getBukkitEntity();
        CraftServer craftServer = (CraftServer) entity.getServer();

        CreatureSpawnEvent event = new CreatureSpawnEvent(entity, spawnReason);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * EntityTameEvent
     */
    public static EntityTameEvent callEntityTameEvent(net.minecraft.world.entity.Mob entity, net.minecraft.world.entity.player.Player tamer) {
        org.bukkit.entity.Entity bukkitEntity = ((EthyleneEntity) entity).getBukkitEntity();
        org.bukkit.entity.AnimalTamer bukkitTamer = (tamer != null ? (AnimalTamer) ((EthyleneEntity) tamer).getBukkitEntity() : null);
        CraftServer craftServer = (CraftServer) bukkitEntity.getServer();

        EntityTameEvent event = new EntityTameEvent((LivingEntity) bukkitEntity, bukkitTamer);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * ItemSpawnEvent
     */
    public static ItemSpawnEvent callItemSpawnEvent(net.minecraft.world.entity.item.ItemEntity entityitem) {
        org.bukkit.entity.Item entity = (org.bukkit.entity.Item) ((EthyleneEntity) entityitem).getBukkitEntity();
        CraftServer craftServer = (CraftServer) entity.getServer();

        ItemSpawnEvent event = new ItemSpawnEvent(entity);

        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * ItemDespawnEvent
     */
    public static ItemDespawnEvent callItemDespawnEvent(net.minecraft.world.entity.item.ItemEntity entityitem) {
        org.bukkit.entity.Item entity = (org.bukkit.entity.Item) ((EthyleneEntity) entityitem).getBukkitEntity();

        ItemDespawnEvent event = new ItemDespawnEvent(entity, entity.getLocation());

        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * ItemMergeEvent
     */
    public static boolean callItemMergeEvent(net.minecraft.world.entity.item.ItemEntity merging, net.minecraft.world.entity.item.ItemEntity mergingWith) {
        org.bukkit.entity.Item entityMerging = (org.bukkit.entity.Item) ((EthyleneEntity) merging).getBukkitEntity();
        org.bukkit.entity.Item entityMergingWith = (org.bukkit.entity.Item) ((EthyleneEntity) mergingWith).getBukkitEntity();

        ItemMergeEvent event = new ItemMergeEvent(entityMerging, entityMergingWith);

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    /**
     * PotionSplashEvent
     */
    public static PotionSplashEvent callPotionSplashEvent(net.minecraft.world.entity.projectile.ThrownPotion potion, net.minecraft.world.phys.HitResult position, Map<LivingEntity, Double> affectedEntities) {
        ThrownPotion thrownPotion = (ThrownPotion) ((EthyleneEntity) potion).getBukkitEntity();

        Block hitBlock = null;
        BlockFace hitFace = null;
        if (position.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.world.phys.BlockHitResult positionBlock = (net.minecraft.world.phys.BlockHitResult) position;
            hitBlock = CraftBlock.at(potion.level(), positionBlock.getBlockPos());
            hitFace = CraftBlock.notchToBlockFace(positionBlock.getDirection());
        }

        org.bukkit.entity.Entity hitEntity = null;
        if (position.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
            hitEntity = ((EthyleneEntity) ((net.minecraft.world.phys.EntityHitResult) position).getEntity()).getBukkitEntity();
        }

        PotionSplashEvent event = new PotionSplashEvent(thrownPotion, hitEntity, hitBlock, hitFace, affectedEntities);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static LingeringPotionSplashEvent callLingeringPotionSplashEvent(net.minecraft.world.entity.projectile.ThrownPotion potion, net.minecraft.world.phys.HitResult position, net.minecraft.world.entity.AreaEffectCloud cloud) {
        ThrownPotion thrownPotion = (ThrownPotion) ((EthyleneEntity) potion).getBukkitEntity();
        AreaEffectCloud effectCloud = (AreaEffectCloud) ((EthyleneEntity) cloud).getBukkitEntity();

        Block hitBlock = null;
        BlockFace hitFace = null;
        if (position.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.world.phys.BlockHitResult positionBlock = (net.minecraft.world.phys.BlockHitResult) position;
            hitBlock = CraftBlock.at(potion.level(), positionBlock.getBlockPos());
            hitFace = CraftBlock.notchToBlockFace(positionBlock.getDirection());
        }

        org.bukkit.entity.Entity hitEntity = null;
        if (position.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
            hitEntity = ((EthyleneEntity) ((net.minecraft.world.phys.EntityHitResult) position).getEntity()).getBukkitEntity();
        }

        LingeringPotionSplashEvent event = new LingeringPotionSplashEvent(thrownPotion, hitEntity, hitBlock, hitFace, effectCloud);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * BlockFadeEvent
     */
    public static BlockFadeEvent callBlockFadeEvent(net.minecraft.world.level.LevelAccessor world, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState newBlock) {
        CraftBlockState state = CraftBlockStates.getBlockState(world, pos);
        state.setData(newBlock);

        BlockFadeEvent event = new BlockFadeEvent(state.getBlock(), state);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handleMoistureChangeEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState newBlock, int flag) {
        CraftBlockState state = CraftBlockStates.getBlockState(world, pos, flag);
        state.setData(newBlock);

        MoistureChangeEvent event = new MoistureChangeEvent(state.getBlock(), state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }
        return !event.isCancelled();
    }

    public static boolean handleBlockSpreadEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos source, net.minecraft.core.BlockPos target, net.minecraft.world.level.block.state.BlockState block) {
        return handleBlockSpreadEvent(world, source, target, block, 2);
    }

    public static net.minecraft.core.BlockPos sourceBlockOverride = null; // SPIGOT-7068: Add source block override, not the most elegant way but better than passing down a net.minecraft.core.BlockPos up to five methods deep.

    public static boolean handleBlockSpreadEvent(net.minecraft.world.level.LevelAccessor world, net.minecraft.core.BlockPos source, net.minecraft.core.BlockPos target, net.minecraft.world.level.block.state.BlockState block, int flag) {
        // Suppress during worldgen
        if (!(world instanceof net.minecraft.world.level.Level)) {
            world.setBlock(target, block, flag);
            return true;
        }

        CraftBlockState state = CraftBlockStates.getBlockState(world, target, flag);
        state.setData(block);

        BlockSpreadEvent event = new BlockSpreadEvent(state.getBlock(), CraftBlock.at(world, sourceBlockOverride != null ? sourceBlockOverride : source), state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }
        return !event.isCancelled();
    }

    public static EntityDeathEvent callEntityDeathEvent(net.minecraft.world.entity.LivingEntity victim, net.minecraft.world.damagesource.DamageSource damageSource) {
        return callEntityDeathEvent(victim, damageSource, new ArrayList<org.bukkit.inventory.ItemStack>(0));
    }

    public static EntityDeathEvent callEntityDeathEvent(net.minecraft.world.entity.LivingEntity victim, net.minecraft.world.damagesource.DamageSource damageSource, List<org.bukkit.inventory.ItemStack> drops) {
        CraftLivingEntity entity = (CraftLivingEntity) ((EthyleneEntity) victim).getBukkitEntity();
        CraftDamageSource bukkitDamageSource = new CraftDamageSource(damageSource);
        EntityDeathEvent event = new EntityDeathEvent(entity, bukkitDamageSource, drops, victim.getExpReward(damageSource.getEntity()));
        CraftWorld world = (CraftWorld) entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);

        victim.expToDrop = event.getDroppedExp();

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0) continue;

            world.dropItem(entity.getLocation(), stack);
        }

        return event;
    }

    public static PlayerDeathEvent callPlayerDeathEvent(net.minecraft.server.level.ServerPlayer victim, net.minecraft.world.damagesource.DamageSource damageSource, List<org.bukkit.inventory.ItemStack> drops, String deathMessage, boolean keepInventory) {
        CraftPlayer entity = (CraftPlayer) ((EthyleneEntity) victim).getBukkitEntity();
        CraftDamageSource bukkitDamageSource = new CraftDamageSource(damageSource);
        PlayerDeathEvent event = new PlayerDeathEvent(entity, bukkitDamageSource, drops, victim.getExpReward(damageSource.getEntity()), 0, deathMessage);
        event.setKeepInventory(keepInventory);
        event.setKeepLevel(victim.keepLevel); // SPIGOT-2222: pre-set keepLevel
        Bukkit.getServer().getPluginManager().callEvent(event);

        victim.keepLevel = event.getKeepLevel();
        victim.newLevel = event.getNewLevel();
        victim.newTotalExp = event.getNewTotalExp();
        victim.expToDrop = event.getDroppedExp();
        victim.newExp = event.getNewExp();

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR) continue;

            if (stack instanceof CraftItemStack craftItemStack && craftItemStack.isForInventoryDrop()) {
                victim.drop(CraftItemStack.asNMSCopy(stack), true, false, false); // SPIGOT-7800, SPIGOT-7801: Vanilla Behaviour for Player Inventory dropped items
            } else {
                victim.forceDrops = true;
                victim.spawnAtLocation(CraftItemStack.asNMSCopy(stack)); // SPIGOT-7806: Vanilla Behaviour for items not related to Player Inventory dropped items
                victim.forceDrops = false;
            }
        }

        return event;
    }

    /**
     * Server methods
     */
    public static ServerListPingEvent callServerListPingEvent(SocketAddress address, String motd, int numPlayers, int maxPlayers) {
        ServerListPingEvent event = new ServerListPingEvent("", ((InetSocketAddress) address).getAddress(), motd, numPlayers, maxPlayers);
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event;
    }

    private static EntityDamageEvent handleEntityDamageEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.damagesource.DamageSource source, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions) {
        return handleEntityDamageEvent(entity, source, modifiers, modifierFunctions, false);
    }

    private static EntityDamageEvent handleEntityDamageEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.damagesource.DamageSource source, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled) {
        CraftDamageSource bukkitDamageSource = new CraftDamageSource(source);
        net.minecraft.world.entity.Entity damager = (source.getDamager() != null) ? source.getDamager() : source.getEntity();
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            if (damager == null) {
                return callEntityDamageEvent(source.getDirectBlock(), source.getDirectBlockState(), entity, DamageCause.BLOCK_EXPLOSION, bukkitDamageSource, modifiers, modifierFunctions, cancelled);
            }
            DamageCause damageCause = (((EthyleneEntity) damager).getBukkitEntity() instanceof org.bukkit.entity.TNTPrimed) ? DamageCause.BLOCK_EXPLOSION : DamageCause.ENTITY_EXPLOSION;
            return callEntityDamageEvent(damager, entity, damageCause, bukkitDamageSource, modifiers, modifierFunctions, cancelled);
        } else if (damager != null || source.getDirectEntity() != null) {
            DamageCause cause = (source.isSweep()) ? DamageCause.ENTITY_SWEEP_ATTACK : DamageCause.ENTITY_ATTACK;

            if (damager instanceof net.minecraft.world.entity.projectile.Projectile) {
                if (((EthyleneEntity) damager).getBukkitEntity() instanceof ThrownPotion) {
                    cause = DamageCause.MAGIC;
                } else if (((EthyleneEntity) damager).getBukkitEntity() instanceof Projectile) {
                    cause = DamageCause.PROJECTILE;
                }
            } else if (source.is(net.minecraft.world.damagesource.DamageTypes.THORNS)) {
                cause = DamageCause.THORNS;
            } else if (source.is(net.minecraft.world.damagesource.DamageTypes.SONIC_BOOM)) {
                cause = DamageCause.SONIC_BOOM;
            } else if (source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_STALACTITE) || source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_BLOCK) || source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_ANVIL)) {
                cause = DamageCause.FALLING_BLOCK;
            } else if (source.is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT)) {
                cause = DamageCause.LIGHTNING;
            } else if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
                cause = DamageCause.FALL;
            } else if (source.is(net.minecraft.world.damagesource.DamageTypes.DRAGON_BREATH)) {
                cause = DamageCause.DRAGON_BREATH;
            } else if (source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC)) {
                cause = DamageCause.MAGIC;
            }

            return callEntityDamageEvent(damager, entity, cause, bukkitDamageSource, modifiers, modifierFunctions, cancelled);
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)) {
            return callEntityDamageEvent(source.getDirectBlock(), source.getDirectBlockState(), entity, DamageCause.VOID, bukkitDamageSource, modifiers, modifierFunctions, cancelled);
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)) {
            return callEntityDamageEvent(source.getDirectBlock(), source.getDirectBlockState(), entity, DamageCause.LAVA, bukkitDamageSource, modifiers, modifierFunctions, cancelled);
        } else if (source.getDirectBlock() != null) {
            DamageCause cause;
            if (source.is(net.minecraft.world.damagesource.DamageTypes.CACTUS) || source.is(net.minecraft.world.damagesource.DamageTypes.SWEET_BERRY_BUSH) || source.is(net.minecraft.world.damagesource.DamageTypes.STALAGMITE) || source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_STALACTITE) || source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_ANVIL)) {
                cause = DamageCause.CONTACT;
            } else if (source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)) {
                cause = DamageCause.HOT_FLOOR;
            } else if (source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC)) {
                cause = DamageCause.MAGIC;
            } else if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE)) {
                cause = DamageCause.FIRE;
            } else if (source.is(net.minecraft.world.damagesource.DamageTypes.CAMPFIRE)) {
                cause = DamageCause.CAMPFIRE;
            } else {
                throw new IllegalStateException(String.format("Unhandled damage of %s by %s from %s [%s]", entity, source.getDirectBlock(), source.getMsgId(), source.typeHolder().getRegisteredName()));
            }
            return callEntityDamageEvent(source.getDirectBlock(), source.getDirectBlockState(), entity, cause, bukkitDamageSource, modifiers, modifierFunctions, cancelled);
        }

        DamageCause cause;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE)) {
            cause = DamageCause.FIRE;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.STARVE)) {
            cause = DamageCause.STARVATION;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
            cause = DamageCause.WITHER;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) {
            cause = DamageCause.SUFFOCATION;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.DROWN)) {
            cause = DamageCause.DROWNING;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)) {
            cause = DamageCause.FIRE_TICK;
        } else if (source.isMelting()) {
            cause = DamageCause.MELTING;
        } else if (source.isPoison()) {
            cause = DamageCause.POISON;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC)) {
            cause = DamageCause.MAGIC;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
            cause = DamageCause.FALL;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.FLY_INTO_WALL)) {
            cause = DamageCause.FLY_INTO_WALL;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.CRAMMING)) {
            cause = DamageCause.CRAMMING;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.DRY_OUT)) {
            cause = DamageCause.DRYOUT;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.FREEZE)) {
            cause = DamageCause.FREEZE;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)) {
            cause = DamageCause.KILL;
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.OUTSIDE_BORDER)) {
            cause = DamageCause.WORLD_BORDER;
        } else {
            cause = DamageCause.CUSTOM;
        }

        return callEntityDamageEvent((net.minecraft.world.entity.Entity) null, entity, cause, bukkitDamageSource, modifiers, modifierFunctions, cancelled);
    }

    private static EntityDamageEvent callEntityDamageEvent(net.minecraft.world.entity.Entity damager, net.minecraft.world.entity.Entity damagee, DamageCause cause, org.bukkit.damage.DamageSource bukkitDamageSource, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled) {
        EntityDamageEvent event;
        if (damager != null) {
            event = new EntityDamageByEntityEvent(((EthyleneEntity) damager).getBukkitEntity(), ((EthyleneEntity) damagee).getBukkitEntity(), cause, bukkitDamageSource, modifiers, modifierFunctions);
        } else {
            event = new EntityDamageEvent(((EthyleneEntity) damagee).getBukkitEntity(), cause, bukkitDamageSource, modifiers, modifierFunctions);
        }
        return callEntityDamageEvent(event, damagee, cancelled);
    }

    private static EntityDamageEvent callEntityDamageEvent(Block damager, BlockState damagerState, net.minecraft.world.entity.Entity damagee, DamageCause cause, org.bukkit.damage.DamageSource bukkitDamageSource, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled) {
        EntityDamageByBlockEvent event = new EntityDamageByBlockEvent(damager, damagerState, ((EthyleneEntity) damagee).getBukkitEntity(), cause, bukkitDamageSource, modifiers, modifierFunctions);
        return callEntityDamageEvent(event, damagee, cancelled);
    }

    private static EntityDamageEvent callEntityDamageEvent(EntityDamageEvent event, net.minecraft.world.entity.Entity damagee, boolean cancelled) {
        event.setCancelled(cancelled);
        callEvent(event);

        if (!event.isCancelled()) {
            event.getEntity().setLastDamageCause(event);
        } else {
            damagee.lastDamageCancelled = true; // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
        }

        return event;
    }

    private static final Function<? super Double, Double> ZERO = Functions.constant(-0.0);

    public static EntityDamageEvent handleLivingEntityDamageEvent(net.minecraft.world.entity.Entity damagee, net.minecraft.world.damagesource.DamageSource source, double rawDamage, double freezingModifier, double hardHatModifier, double blockingModifier, double armorModifier, double resistanceModifier, double magicModifier, double absorptionModifier, Function<Double, Double> freezing, Function<Double, Double> hardHat, Function<Double, Double> blocking, Function<Double, Double> armor, Function<Double, Double> resistance, Function<Double, Double> magic, Function<Double, Double> absorption) {
        Map<DamageModifier, Double> modifiers = new EnumMap<>(DamageModifier.class);
        Map<DamageModifier, Function<? super Double, Double>> modifierFunctions = new EnumMap<>(DamageModifier.class);
        modifiers.put(DamageModifier.BASE, rawDamage);
        modifierFunctions.put(DamageModifier.BASE, ZERO);
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FREEZE)) {
            modifiers.put(DamageModifier.FREEZING, freezingModifier);
            modifierFunctions.put(DamageModifier.FREEZING, freezing);
        }
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_BLOCK) || source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_ANVIL)) {
            modifiers.put(DamageModifier.HARD_HAT, hardHatModifier);
            modifierFunctions.put(DamageModifier.HARD_HAT, hardHat);
        }
        if (damagee instanceof net.minecraft.world.entity.player.Player) {
            modifiers.put(DamageModifier.BLOCKING, blockingModifier);
            modifierFunctions.put(DamageModifier.BLOCKING, blocking);
        }
        modifiers.put(DamageModifier.ARMOR, armorModifier);
        modifierFunctions.put(DamageModifier.ARMOR, armor);
        modifiers.put(DamageModifier.RESISTANCE, resistanceModifier);
        modifierFunctions.put(DamageModifier.RESISTANCE, resistance);
        modifiers.put(DamageModifier.MAGIC, magicModifier);
        modifierFunctions.put(DamageModifier.MAGIC, magic);
        modifiers.put(DamageModifier.ABSORPTION, absorptionModifier);
        modifierFunctions.put(DamageModifier.ABSORPTION, absorption);
        return handleEntityDamageEvent(damagee, source, modifiers, modifierFunctions);
    }

    // Non-Living Entities such as EntityEnderCrystal and EntityFireball need to call this
    public static boolean handleNonLivingEntityDamageEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.damagesource.DamageSource source, double damage) {
        return handleNonLivingEntityDamageEvent(entity, source, damage, true);
    }

    public static boolean handleNonLivingEntityDamageEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.damagesource.DamageSource source, double damage, boolean cancelOnZeroDamage) {
        return handleNonLivingEntityDamageEvent(entity, source, damage, cancelOnZeroDamage, false);
    }

    public static EntityDamageEvent callNonLivingEntityDamageEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.damagesource.DamageSource source, double damage, boolean cancelled) {
        final EnumMap<DamageModifier, Double> modifiers = new EnumMap<DamageModifier, Double>(DamageModifier.class);
        final EnumMap<DamageModifier, Function<? super Double, Double>> functions = new EnumMap(DamageModifier.class);

        modifiers.put(DamageModifier.BASE, damage);
        functions.put(DamageModifier.BASE, ZERO);

        return handleEntityDamageEvent(entity, source, modifiers, functions, cancelled);
    }

    public static boolean handleNonLivingEntityDamageEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.damagesource.DamageSource source, double damage, boolean cancelOnZeroDamage, boolean cancelled) {
        final EntityDamageEvent event = callNonLivingEntityDamageEvent(entity, source, damage, cancelled);

        if (event == null) {
            return false;
        }
        return event.isCancelled() || (cancelOnZeroDamage && event.getDamage() == 0);
    }

    public static PlayerLevelChangeEvent callPlayerLevelChangeEvent(Player player, int oldLevel, int newLevel) {
        PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(player, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerExpChangeEvent callPlayerExpChangeEvent(net.minecraft.world.entity.player.Player entity, int expAmount) {
        Player player = (Player) ((EthyleneEntity) entity).getBukkitEntity();
        PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, expAmount);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerExpCooldownChangeEvent callPlayerXpCooldownEvent(net.minecraft.world.entity.player.Player entity, int newCooldown, PlayerExpCooldownChangeEvent.ChangeReason changeReason) {
        Player player = (Player) ((EthyleneEntity) entity).getBukkitEntity();
        PlayerExpCooldownChangeEvent event = new PlayerExpCooldownChangeEvent(player, newCooldown, changeReason);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerItemMendEvent callPlayerItemMendEvent(net.minecraft.world.entity.player.Player entity, net.minecraft.world.entity.ExperienceOrb orb, net.minecraft.world.item.ItemStack nmsMendedItem, net.minecraft.world.entity.EquipmentSlot slot, int repairAmount) {
        Player player = (Player) ((EthyleneEntity) entity).getBukkitEntity();
        org.bukkit.inventory.ItemStack bukkitStack = CraftItemStack.asCraftMirror(nmsMendedItem);
        PlayerItemMendEvent event = new PlayerItemMendEvent(player, bukkitStack, CraftEquipmentSlot.getSlot(slot), (ExperienceOrb) ((EthyleneEntity) orb).getBukkitEntity(), repairAmount);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handleBlockGrowEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState block) {
        return handleBlockGrowEvent(world, pos, block, 3);
    }

    public static boolean handleBlockGrowEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState newData, int flag) {
        Block block = ((EthyleneLevel) world).getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        CraftBlockState state = (CraftBlockState) block.getState();
        state.setData(newData);

        BlockGrowEvent event = new BlockGrowEvent(block, state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }

        return !event.isCancelled();
    }

    public static FluidLevelChangeEvent callFluidLevelChangeEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos block, net.minecraft.world.level.block.state.BlockState newData) {
        FluidLevelChangeEvent event = new FluidLevelChangeEvent(CraftBlock.at(world, block), CraftBlockData.fromData(newData));
        ((EthyleneLevel) world).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static FoodLevelChangeEvent callFoodLevelChangeEvent(net.minecraft.world.entity.player.Player entity, int level) {
        return callFoodLevelChangeEvent(entity, level, null);
    }

    public static FoodLevelChangeEvent callFoodLevelChangeEvent(net.minecraft.world.entity.player.Player entity, int level, net.minecraft.world.item.ItemStack item) {
        FoodLevelChangeEvent event = new FoodLevelChangeEvent((HumanEntity) ((EthyleneEntity) entity).getBukkitEntity(), level, (item == null) ? null : CraftItemStack.asBukkitCopy(item));
        ((EthyleneEntity) entity).getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PigZapEvent callPigZapEvent(net.minecraft.world.entity.Entity pig, net.minecraft.world.entity.Entity lightning, net.minecraft.world.entity.Entity pigzombie) {
        PigZapEvent event = new PigZapEvent((Pig) ((EthyleneEntity) pig).getBukkitEntity(), (LightningStrike) ((EthyleneEntity) lightning).getBukkitEntity(), (PigZombie) ((EthyleneEntity) pigzombie).getBukkitEntity());
        ((EthyleneEntity) pig).getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callHorseJumpEvent(net.minecraft.world.entity.Entity horse, float power) {
        HorseJumpEvent event = new HorseJumpEvent((AbstractHorse) ((EthyleneEntity) horse).getBukkitEntity(), power);
        ((EthyleneEntity) horse).getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static boolean callEntityChangeBlockEvent(net.minecraft.world.entity.Entity entity, net.minecraft.core.BlockPos position, net.minecraft.world.level.block.state.BlockState newBlock) {
        return callEntityChangeBlockEvent(entity, position, newBlock, false);
    }

    public static boolean callEntityChangeBlockEvent(net.minecraft.world.entity.Entity entity, net.minecraft.core.BlockPos position, net.minecraft.world.level.block.state.BlockState newBlock, boolean cancelled) {
        Block block = ((EthyleneLevel) entity.level()).getWorld().getBlockAt(position.getX(), position.getY(), position.getZ());

        EntityChangeBlockEvent event = new EntityChangeBlockEvent(((EthyleneEntity) entity).getBukkitEntity(), block, CraftBlockData.fromData(newBlock));
        event.setCancelled(cancelled);
        event.getEntity().getServer().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static CreeperPowerEvent callCreeperPowerEvent(net.minecraft.world.entity.Entity creeper, net.minecraft.world.entity.Entity lightning, CreeperPowerEvent.PowerCause cause) {
        CreeperPowerEvent event = new CreeperPowerEvent((Creeper) ((EthyleneEntity) creeper).getBukkitEntity(), (LightningStrike) ((EthyleneEntity) lightning).getBukkitEntity(), cause);
        ((EthyleneEntity) creeper).getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTargetEvent callEntityTargetEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.Entity target, EntityTargetEvent.TargetReason reason) {
        EntityTargetEvent event = new EntityTargetEvent(((EthyleneEntity) entity).getBukkitEntity(), (target == null) ? null : ((EthyleneEntity) target).getBukkitEntity(), reason);
        ((EthyleneEntity) entity).getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTargetLivingEntityEvent callEntityTargetLivingEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.LivingEntity target, EntityTargetEvent.TargetReason reason) {
        EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(((EthyleneEntity) entity).getBukkitEntity(), (target == null) ? null : (LivingEntity) ((EthyleneEntity) target).getBukkitEntity(), reason);
        ((EthyleneEntity) entity).getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityBreakDoorEvent callEntityBreakDoorEvent(net.minecraft.world.entity.Entity entity, net.minecraft.core.BlockPos pos) {
        org.bukkit.entity.Entity entity1 = ((EthyleneEntity) entity).getBukkitEntity();
        Block block = CraftBlock.at(entity.level(), pos);

        EntityBreakDoorEvent event = new EntityBreakDoorEvent((LivingEntity) entity1, block);
        entity1.getServer().getPluginManager().callEvent(event);

        return event;
    }

    public static net.minecraft.world.inventory.AbstractContainerMenu callInventoryOpenEvent(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.inventory.AbstractContainerMenu container) {
        return callInventoryOpenEvent(player, container, false);
    }

    public static net.minecraft.world.inventory.AbstractContainerMenu callInventoryOpenEvent(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.inventory.AbstractContainerMenu container, boolean cancelled) {
        if (player.containerMenu != player.inventoryMenu) { // fire INVENTORY_CLOSE if one already open
            player.connection.handleContainerClose(new net.minecraft.network.protocol.game.ServerboundContainerClosePacket(player.containerMenu.containerId));
        }

        CraftServer server = ((EthyleneLevel) player.level()).getCraftServer();
        CraftPlayer craftPlayer = (CraftPlayer) ((EthyleneEntity) player).getBukkitEntity();
        ((EthyleneAbstractContainerMenu) player.containerMenu).transferTo(container, craftPlayer);

        InventoryOpenEvent event = new InventoryOpenEvent(((EthyleneAbstractContainerMenu) container).getBukkitView());
        event.setCancelled(cancelled);
        server.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ((EthyleneAbstractContainerMenu) container).transferTo(player.containerMenu, craftPlayer);
            return null;
        }

        return container;
    }

    public static net.minecraft.world.item.ItemStack callPreCraftEvent(net.minecraft.world.inventory.CraftingContainer matrix, Container resultInventory, net.minecraft.world.item.ItemStack result, InventoryView lastCraftView, boolean isRepair) {
        CraftInventoryCrafting inventory = new CraftInventoryCrafting(matrix, resultInventory);
        inventory.setResult(CraftItemStack.asCraftMirror(result));

        PrepareItemCraftEvent event = new PrepareItemCraftEvent(inventory, lastCraftView, isRepair);
        Bukkit.getPluginManager().callEvent(event);

        org.bukkit.inventory.ItemStack bitem = event.getInventory().getResult();

        return CraftItemStack.asNMSCopy(bitem);
    }

    public static CrafterCraftEvent callCrafterCraftEvent(net.minecraft.core.BlockPos pos, net.minecraft.world.level.Level world, net.minecraft.world.inventory.CraftingContainer inventoryCrafting, net.minecraft.world.item.ItemStack result, net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe> holder) {
        CraftBlock block = CraftBlock.at(world, pos);
        CraftItemStack itemStack = CraftItemStack.asCraftMirror(result);
        CraftingRecipe craftingRecipe = (CraftingRecipe) holder.toBukkitRecipe();

        CrafterCraftEvent crafterCraftEvent = new CrafterCraftEvent(block, craftingRecipe, itemStack);
        Bukkit.getPluginManager().callEvent(crafterCraftEvent);
        return crafterCraftEvent;
    }

    public static ProjectileLaunchEvent callProjectileLaunchEvent(net.minecraft.world.entity.Entity entity) {
        Projectile bukkitEntity = (Projectile) ((EthyleneEntity) entity).getBukkitEntity();
        ProjectileLaunchEvent event = new ProjectileLaunchEvent(bukkitEntity);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static ProjectileHitEvent callProjectileHitEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.phys.HitResult position) {
        if (position.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
            return null;
        }

        Block hitBlock = null;
        BlockFace hitFace = null;
        if (position.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.world.phys.BlockHitResult positionBlock = (net.minecraft.world.phys.BlockHitResult) position;
            hitBlock = CraftBlock.at(entity.level(), positionBlock.getBlockPos());
            hitFace = CraftBlock.notchToBlockFace(positionBlock.getDirection());
        }

        org.bukkit.entity.Entity hitEntity = null;
        if (position.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
            hitEntity = ((EthyleneEntity) ((net.minecraft.world.phys.EntityHitResult) position).getEntity()).getBukkitEntity();
        }

        ProjectileHitEvent event = new ProjectileHitEvent((Projectile) ((EthyleneEntity) entity).getBukkitEntity(), hitEntity, hitBlock, hitFace);
        ((EthyleneLevel) entity.level()).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static ExpBottleEvent callExpBottleEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.phys.HitResult position, int exp) {
        ThrownExpBottle bottle = (ThrownExpBottle) ((EthyleneEntity) entity).getBukkitEntity();

        Block hitBlock = null;
        BlockFace hitFace = null;
        if (position.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.world.phys.BlockHitResult positionBlock = (net.minecraft.world.phys.BlockHitResult) position;
            hitBlock = CraftBlock.at(entity.level(), positionBlock.getBlockPos());
            hitFace = CraftBlock.notchToBlockFace(positionBlock.getDirection());
        }

        org.bukkit.entity.Entity hitEntity = null;
        if (position.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
            hitEntity = ((EthyleneEntity) ((net.minecraft.world.phys.EntityHitResult) position).getEntity()).getBukkitEntity();
        }

        ExpBottleEvent event = new ExpBottleEvent(bottle, hitEntity, hitBlock, hitFace, exp);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockRedstoneEvent callRedstoneChange(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, int oldCurrent, int newCurrent) {
        BlockRedstoneEvent event = new BlockRedstoneEvent(((EthyleneLevel) world).getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), oldCurrent, newCurrent);
        ((EthyleneLevel) world).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static NotePlayEvent callNotePlayEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.properties.NoteBlockInstrument instrument, int note) {
        NotePlayEvent event = new NotePlayEvent(((EthyleneLevel) world).getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), org.bukkit.Instrument.getByType((byte) instrument.ordinal()), new org.bukkit.Note(note));
        ((EthyleneLevel) world).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void callPlayerItemBreakEvent(net.minecraft.server.level.ServerPlayer human, net.minecraft.world.item.ItemStack brokenItem) {
        CraftItemStack item = CraftItemStack.asCraftMirror(brokenItem);
        PlayerItemBreakEvent event = new PlayerItemBreakEvent((Player) ((EthyleneEntity) human).getBukkitEntity(), item);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static BlockIgniteEvent callBlockIgniteEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos block, net.minecraft.core.BlockPos source) {
        org.bukkit.World bukkitWorld = ((EthyleneLevel) world).getWorld();
        Block igniter = bukkitWorld.getBlockAt(source.getX(), source.getY(), source.getZ());
        IgniteCause cause;
        switch (igniter.getType()) {
            case LAVA:
                cause = IgniteCause.LAVA;
                break;
            case DISPENSER:
                cause = IgniteCause.FLINT_AND_STEEL;
                break;
            case FIRE: // Fire or any other unknown block counts as SPREAD.
            default:
                cause = IgniteCause.SPREAD;
        }

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(block.getX(), block.getY(), block.getZ()), cause, igniter);
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.Entity igniter) {
        org.bukkit.World bukkitWorld = ((EthyleneLevel) world).getWorld();
        org.bukkit.entity.Entity bukkitIgniter = ((EthyleneEntity) igniter).getBukkitEntity();
        IgniteCause cause;
        switch (bukkitIgniter.getType()) {
            case END_CRYSTAL:
                cause = IgniteCause.ENDER_CRYSTAL;
                break;
            case LIGHTNING_BOLT:
                cause = IgniteCause.LIGHTNING;
                break;
            case SMALL_FIREBALL:
            case FIREBALL:
                cause = IgniteCause.FIREBALL;
                break;
            case ARROW:
                cause = IgniteCause.ARROW;
                break;
            default:
                cause = IgniteCause.FLINT_AND_STEEL;
        }

        if (igniter instanceof net.minecraft.world.entity.projectile.Projectile) {
            net.minecraft.world.entity.Entity shooter = ((net.minecraft.world.entity.projectile.Projectile) igniter).getOwner();
            if (shooter != null) {
                bukkitIgniter = ((EthyleneEntity) shooter).getBukkitEntity();
            }
        }

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), cause, bukkitIgniter);
        ((EthyleneLevel) world).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos blockposition, net.minecraft.world.level.Explosion explosion) {
        org.bukkit.entity.Entity igniter = explosion.source == null ? null : ((EthyleneEntity) explosion.source).getBukkitEntity();

        BlockIgniteEvent event = new BlockIgniteEvent(CraftBlock.at(world, blockposition), IgniteCause.EXPLOSION, igniter);
        ((EthyleneLevel) world).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, IgniteCause cause, net.minecraft.world.entity.Entity igniter) {
        BlockIgniteEvent event = new BlockIgniteEvent(((EthyleneLevel) world).getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), cause, ((EthyleneEntity) igniter).getBukkitEntity());
        ((EthyleneLevel) world).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void handleInventoryCloseEvent(net.minecraft.world.entity.player.Player human) {
        InventoryCloseEvent event = new InventoryCloseEvent(((EthyleneAbstractContainerMenu) human.containerMenu).getBukkitView());
        ((EthyleneLevel) human.level()).getCraftServer().getPluginManager().callEvent(event);
        ((EthyleneAbstractContainerMenu) human.containerMenu).transferTo(human.inventoryMenu, (CraftHumanEntity) ((EthyleneEntity) human).getBukkitEntity());
    }

    public static net.minecraft.world.item.ItemStack handleEditBookEvent(net.minecraft.server.level.ServerPlayer player, int itemInHandIndex, net.minecraft.world.item.ItemStack itemInHand, net.minecraft.world.item.ItemStack newBookItem) {
        PlayerEditBookEvent editBookEvent = new PlayerEditBookEvent((Player) ((EthyleneEntity) player).getBukkitEntity(), (itemInHandIndex >= 0 && itemInHandIndex <= 8) ? itemInHandIndex : -1, (BookMeta) CraftItemStack.getItemMeta(itemInHand), (BookMeta) CraftItemStack.getItemMeta(newBookItem), newBookItem.getItem() == net.minecraft.world.item.Items.WRITTEN_BOOK);
        ((EthyleneLevel) player.level()).getCraftServer().getPluginManager().callEvent(editBookEvent);

        // If they've got the same item in their hand, it'll need to be updated.
        if (itemInHand != null && itemInHand.getItem() == net.minecraft.world.item.Items.WRITABLE_BOOK) {
            if (!editBookEvent.isCancelled()) {
                if (editBookEvent.isSigning()) {
                    itemInHand.setItem(net.minecraft.world.item.Items.WRITTEN_BOOK);
                }
                BookMeta meta = editBookEvent.getNewBookMeta();
                CraftItemStack.setItemMeta(itemInHand, meta);
            } else {
                ((Player) ((EthyleneEntity) player).getBukkitEntity()).updateInventory(); // SPIGOT-7484
            }
        }

        return itemInHand;
    }

    public static void callRecipeBookSettingsEvent(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.inventory.RecipeBookType type, boolean open, boolean filter) {
        PlayerRecipeBookSettingsChangeEvent.RecipeBookType bukkitType = PlayerRecipeBookSettingsChangeEvent.RecipeBookType.values()[type.ordinal()];
        Bukkit.getPluginManager().callEvent(new PlayerRecipeBookSettingsChangeEvent((Player) ((EthyleneEntity) player).getBukkitEntity(), bukkitType, open, filter));
    }

    public static PlayerUnleashEntityEvent callPlayerUnleashEntityEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand enumhand) {
        PlayerUnleashEntityEvent event = new PlayerUnleashEntityEvent(((EthyleneEntity) entity).getBukkitEntity(), (Player) ((EthyleneEntity) player).getBukkitEntity(), CraftEquipmentSlot.getHand(enumhand));
        ((EthyleneLevel) entity.level()).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerLeashEntityEvent callPlayerLeashEntityEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.Entity leashHolder, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand enumhand) {
        PlayerLeashEntityEvent event = new PlayerLeashEntityEvent(((EthyleneEntity) entity).getBukkitEntity(), ((EthyleneEntity) leashHolder).getBukkitEntity(), (Player) ((EthyleneEntity) player).getBukkitEntity(), CraftEquipmentSlot.getHand(enumhand));
        ((EthyleneLevel) entity.level()).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void callPlayerRiptideEvent(net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack tridentItemStack, float velocityX, float velocityY, float velocityZ) {
        PlayerRiptideEvent event = new PlayerRiptideEvent((Player) ((EthyleneEntity) player).getBukkitEntity(), CraftItemStack.asCraftMirror(tridentItemStack), new Vector(velocityX, velocityY, velocityZ));
        ((EthyleneLevel) player.level()).getCraftServer().getPluginManager().callEvent(event);
    }

    public static BlockShearEntityEvent callBlockShearEntityEvent(net.minecraft.world.entity.Entity animal, org.bukkit.block.Block dispenser, CraftItemStack is) {
        BlockShearEntityEvent bse = new BlockShearEntityEvent(dispenser, ((EthyleneEntity) animal).getBukkitEntity(), is);
        Bukkit.getPluginManager().callEvent(bse);
        return bse;
    }

    public static boolean handlePlayerShearEntityEvent(net.minecraft.world.entity.player.Player player, net.minecraft.world.entity.Entity sheared, net.minecraft.world.item.ItemStack shears, net.minecraft.world.InteractionHand hand) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer)) {
            return true;
        }

        PlayerShearEntityEvent event = new PlayerShearEntityEvent((Player) ((EthyleneEntity) player).getBukkitEntity(), ((EthyleneEntity) sheared).getBukkitEntity(), CraftItemStack.asCraftMirror(shears), (hand == net.minecraft.world.InteractionHand.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND));
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static Cancellable handleStatisticsIncrease(net.minecraft.world.entity.player.Player entityHuman, net.minecraft.stats.Stat<?> statistic, int current, int newValue) {
        Player player = (Player) (((EthyleneEntity) entityHuman)).getBukkitEntity();
        Event event;
        if (true) {
            org.bukkit.Statistic stat = CraftStatistic.getBukkitStatistic(statistic);
            if (stat == null) {
                System.err.println("Unhandled statistic: " + statistic);
                return null;
            }
            switch (stat) {
                case FALL_ONE_CM:
                case BOAT_ONE_CM:
                case CLIMB_ONE_CM:
                case WALK_ON_WATER_ONE_CM:
                case WALK_UNDER_WATER_ONE_CM:
                case FLY_ONE_CM:
                case HORSE_ONE_CM:
                case MINECART_ONE_CM:
                case PIG_ONE_CM:
                case PLAY_ONE_MINUTE:
                case SWIM_ONE_CM:
                case WALK_ONE_CM:
                case SPRINT_ONE_CM:
                case CROUCH_ONE_CM:
                case TIME_SINCE_DEATH:
                case SNEAK_TIME:
                case TOTAL_WORLD_TIME:
                case TIME_SINCE_REST:
                case AVIATE_ONE_CM:
                case STRIDER_ONE_CM:
                    // Do not process event for these - too spammy
                    return null;
                default:
            }
            if (stat.getType() == Type.UNTYPED) {
                event = new PlayerStatisticIncrementEvent(player, stat, current, newValue);
            } else if (stat.getType() == Type.ENTITY) {
                EntityType entityType = CraftStatistic.getEntityTypeFromStatistic((net.minecraft.stats.Stat<net.minecraft.world.entity.EntityType<?>>) statistic);
                event = new PlayerStatisticIncrementEvent(player, stat, current, newValue, entityType);
            } else {
                Material material = CraftStatistic.getMaterialFromStatistic(statistic);
                event = new PlayerStatisticIncrementEvent(player, stat, current, newValue, material);
            }
        }
        ((EthyleneLevel) entityHuman.level()).getCraftServer().getPluginManager().callEvent(event);
        return (Cancellable) event;
    }

    public static FireworkExplodeEvent callFireworkExplodeEvent(net.minecraft.world.entity.projectile.FireworkRocketEntity firework) {
        FireworkExplodeEvent event = new FireworkExplodeEvent((Firework) ((EthyleneEntity) firework).getBukkitEntity());
        ((EthyleneLevel) firework.level()).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PrepareAnvilEvent callPrepareAnvilEvent(AnvilView view, net.minecraft.world.item.ItemStack item) {
        PrepareAnvilEvent event = new PrepareAnvilEvent(view, CraftItemStack.asCraftMirror(item).clone());
        event.getView().getPlayer().getServer().getPluginManager().callEvent(event);
        event.getInventory().setItem(2, event.getResult());
        return event;
    }

    public static PrepareGrindstoneEvent callPrepareGrindstoneEvent(InventoryView view, net.minecraft.world.item.ItemStack item) {
        PrepareGrindstoneEvent event = new PrepareGrindstoneEvent(view, CraftItemStack.asCraftMirror(item).clone());
        event.getView().getPlayer().getServer().getPluginManager().callEvent(event);
        event.getInventory().setItem(2, event.getResult());
        return event;
    }

    public static PrepareSmithingEvent callPrepareSmithingEvent(InventoryView view, net.minecraft.world.item.ItemStack item) {
        PrepareSmithingEvent event = new PrepareSmithingEvent(view, CraftItemStack.asCraftMirror(item).clone());
        event.getView().getPlayer().getServer().getPluginManager().callEvent(event);
        event.getInventory().setResult(event.getResult());
        return event;
    }

    /**
     * Mob spawner event.
     */
    public static SpawnerSpawnEvent callSpawnerSpawnEvent(net.minecraft.world.entity.Entity spawnee, net.minecraft.core.BlockPos pos) {
        org.bukkit.craftbukkit.entity.CraftEntity entity = ((EthyleneEntity) spawnee).getBukkitEntity();
        BlockState state = CraftBlock.at(spawnee.level(), pos).getState();
        if (!(state instanceof org.bukkit.block.CreatureSpawner)) {
            state = null;
        }

        SpawnerSpawnEvent event = new SpawnerSpawnEvent(entity, (org.bukkit.block.CreatureSpawner) state);
        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Trial Mob spawner event.
     */
    public static TrialSpawnerSpawnEvent callTrialSpawnerSpawnEvent(net.minecraft.world.entity.Entity spawnee, net.minecraft.core.BlockPos pos) {
        org.bukkit.craftbukkit.entity.CraftEntity entity = ((EthyleneEntity) spawnee).getBukkitEntity();
        BlockState state = CraftBlock.at(spawnee.level(), pos).getState();
        if (!(state instanceof org.bukkit.block.TrialSpawner)) {
            state = null;
        }

        TrialSpawnerSpawnEvent event = new TrialSpawnerSpawnEvent(entity, (org.bukkit.block.TrialSpawner) state);
        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockDispenseLootEvent callBlockDispenseLootEvent(net.minecraft.server.level.ServerLevel worldServer, net.minecraft.core.BlockPos blockPosition, net.minecraft.world.entity.player.Player player, List<net.minecraft.world.item.ItemStack> rewardLoot) {
        List<org.bukkit.inventory.ItemStack> craftItemStacks = rewardLoot.stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());

        BlockDispenseLootEvent event = new BlockDispenseLootEvent((player == null) ? null : (Player) ((EthyleneEntity) player).getBukkitEntity(), CraftBlock.at(worldServer, blockPosition), craftItemStacks);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static VaultDisplayItemEvent callVaultDisplayItemEvent(net.minecraft.server.level.ServerLevel worldServer, net.minecraft.core.BlockPos blockPosition, net.minecraft.world.item.ItemStack displayitemStack) {
        VaultDisplayItemEvent event = new VaultDisplayItemEvent(CraftBlock.at(worldServer, blockPosition), CraftItemStack.asBukkitCopy(displayitemStack));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityToggleGlideEvent callToggleGlideEvent(net.minecraft.world.entity.LivingEntity entity, boolean gliding) {
        EntityToggleGlideEvent event = new EntityToggleGlideEvent((LivingEntity) ((EthyleneEntity) entity).getBukkitEntity(), gliding);
        ((EthyleneLevel) entity.level()).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityToggleSwimEvent callToggleSwimEvent(net.minecraft.world.entity.LivingEntity entity, boolean swimming) {
        EntityToggleSwimEvent event = new EntityToggleSwimEvent((LivingEntity) ((EthyleneEntity) entity).getBukkitEntity(), swimming);
        ((EthyleneLevel) entity.level()).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static AreaEffectCloudApplyEvent callAreaEffectCloudApplyEvent(net.minecraft.world.entity.AreaEffectCloud cloud, List<LivingEntity> entities) {
        AreaEffectCloudApplyEvent event = new AreaEffectCloudApplyEvent((AreaEffectCloud) ((EthyleneEntity) cloud).getBukkitEntity(), entities);
        ((EthyleneLevel) cloud.level()).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static VehicleCreateEvent callVehicleCreateEvent(net.minecraft.world.entity.Entity entity) {
        Vehicle bukkitEntity = (Vehicle) ((EthyleneEntity) entity).getBukkitEntity();
        VehicleCreateEvent event = new VehicleCreateEvent(bukkitEntity);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityBreedEvent callEntityBreedEvent(net.minecraft.world.entity.LivingEntity child, net.minecraft.world.entity.LivingEntity mother, net.minecraft.world.entity.LivingEntity father, net.minecraft.world.entity.LivingEntity breeder, net.minecraft.world.item.ItemStack bredWith, int experience) {
        org.bukkit.entity.LivingEntity breederEntity = (LivingEntity) (breeder == null ? null : ((EthyleneEntity) breeder).getBukkitEntity());
        CraftItemStack bredWithStack = bredWith == null ? null : CraftItemStack.asCraftMirror(bredWith).clone();

        EntityBreedEvent event = new EntityBreedEvent((LivingEntity) ((EthyleneEntity) child).getBukkitEntity(), (LivingEntity) ((EthyleneEntity) mother).getBukkitEntity(), (LivingEntity) ((EthyleneEntity) father).getBukkitEntity(), breederEntity, bredWithStack, experience);
        ((EthyleneLevel) child.level()).getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockPhysicsEvent callBlockPhysicsEvent(net.minecraft.world.level.LevelAccessor world, net.minecraft.core.BlockPos blockposition) {
        org.bukkit.block.Block block = CraftBlock.at(world, blockposition);
        BlockPhysicsEvent event = new BlockPhysicsEvent(block, block.getBlockData());
        // Suppress during worldgen
        if (world instanceof net.minecraft.world.level.Level) {
            ((net.minecraft.world.level.Level) world).getServer().server.getPluginManager().callEvent(event);
        }
        return event;
    }

    public static boolean handleBlockFormEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState block) {
        return handleBlockFormEvent(world, pos, block, 3);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(net.minecraft.world.entity.LivingEntity entity, @Nullable net.minecraft.world.effect.MobEffectInstance oldEffect, @Nullable net.minecraft.world.effect.MobEffectInstance newEffect, EntityPotionEffectEvent.Cause cause) {
        return callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, true);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(net.minecraft.world.entity.LivingEntity entity, @Nullable net.minecraft.world.effect.MobEffectInstance oldEffect, @Nullable net.minecraft.world.effect.MobEffectInstance newEffect, EntityPotionEffectEvent.Cause cause, EntityPotionEffectEvent.Action action) {
        return callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, action, true);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(net.minecraft.world.entity.LivingEntity entity, @Nullable net.minecraft.world.effect.MobEffectInstance oldEffect, @Nullable net.minecraft.world.effect.MobEffectInstance newEffect, EntityPotionEffectEvent.Cause cause, boolean willOverride) {
        EntityPotionEffectEvent.Action action = EntityPotionEffectEvent.Action.CHANGED;
        if (oldEffect == null) {
            action = EntityPotionEffectEvent.Action.ADDED;
        } else if (newEffect == null) {
            action = EntityPotionEffectEvent.Action.REMOVED;
        }

        return callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, action, willOverride);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(net.minecraft.world.entity.LivingEntity entity, @Nullable net.minecraft.world.effect.MobEffectInstance oldEffect, @Nullable net.minecraft.world.effect.MobEffectInstance newEffect, EntityPotionEffectEvent.Cause cause, EntityPotionEffectEvent.Action action, boolean willOverride) {
        PotionEffect bukkitOldEffect = (oldEffect == null) ? null : CraftPotionUtil.toBukkit(oldEffect);
        PotionEffect bukkitNewEffect = (newEffect == null) ? null : CraftPotionUtil.toBukkit(newEffect);

        Preconditions.checkState(bukkitOldEffect != null || bukkitNewEffect != null, "Old and new potion effect are both null");

        EntityPotionEffectEvent event = new EntityPotionEffectEvent((LivingEntity) ((EthyleneEntity) entity).getBukkitEntity(), bukkitOldEffect, bukkitNewEffect, cause, action, willOverride);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    public static boolean handleBlockFormEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState block, @Nullable net.minecraft.world.entity.Entity entity) {
        return handleBlockFormEvent(world, pos, block, 3, entity);
    }

    public static boolean handleBlockFormEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState block, int flag) {
        return handleBlockFormEvent(world, pos, block, flag, null);
    }

    public static boolean handleBlockFormEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState block, int flag, @Nullable net.minecraft.world.entity.Entity entity) {
        CraftBlockState blockState = CraftBlockStates.getBlockState(world, pos, flag);
        blockState.setData(block);

        BlockFormEvent event = (entity == null) ? new BlockFormEvent(blockState.getBlock(), blockState) : new EntityBlockFormEvent(((EthyleneEntity) entity).getBukkitEntity(), blockState.getBlock(), blockState);
        ((EthyleneLevel) world).getCraftServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            blockState.update(true);
        }

        return !event.isCancelled();
    }

    public static boolean handleBatToggleSleepEvent(net.minecraft.world.entity.Entity bat, boolean awake) {
        BatToggleSleepEvent event = new BatToggleSleepEvent((Bat) ((EthyleneEntity) bat).getBukkitEntity(), awake);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static boolean handlePlayerRecipeListUpdateEvent(net.minecraft.world.entity.player.Player who, net.minecraft.resources.ResourceLocation recipe) {
        PlayerRecipeDiscoverEvent event = new PlayerRecipeDiscoverEvent((Player) ((EthyleneEntity) who).getBukkitEntity(), CraftNamespacedKey.fromMinecraft(recipe));
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static EntityPickupItemEvent callEntityPickupItemEvent(net.minecraft.world.entity.Entity who, net.minecraft.world.entity.item.ItemEntity item, int remaining, boolean cancelled) {
        EntityPickupItemEvent event = new EntityPickupItemEvent((LivingEntity) ((EthyleneEntity) who).getBukkitEntity(), (Item) ((EthyleneEntity) item).getBukkitEntity(), remaining);
        event.setCancelled(cancelled);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static LightningStrikeEvent callLightningStrikeEvent(LightningStrike entity, LightningStrikeEvent.Cause cause) {
        LightningStrikeEvent event = new LightningStrikeEvent(entity.getWorld(), entity, cause);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * net.minecraft.world.entity.raid.Raid events
     */
    public static boolean callRaidTriggerEvent(net.minecraft.world.entity.raid.Raid raid, net.minecraft.server.level.ServerPlayer player) {
        RaidTriggerEvent event = new RaidTriggerEvent(new CraftRaid(raid), ((EthyleneLevel) raid.getLevel()).getWorld(), (Player) ((EthyleneEntity) player).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static void callRaidFinishEvent(net.minecraft.world.entity.raid.Raid raid, List<Player> players) {
        RaidFinishEvent event = new RaidFinishEvent(new CraftRaid(raid), ((EthyleneLevel) raid.getLevel()).getWorld(), players);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callRaidStopEvent(net.minecraft.world.entity.raid.Raid raid, RaidStopEvent.Reason reason) {
        RaidStopEvent event = new RaidStopEvent(new CraftRaid(raid), ((EthyleneLevel) raid.getLevel()).getWorld(), reason);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callRaidSpawnWaveEvent(net.minecraft.world.entity.raid.Raid raid, net.minecraft.world.entity.raid.Raider leader, List<net.minecraft.world.entity.raid.Raider> raiders) {
        Raider craftLeader = (CraftRaider) ((EthyleneEntity) leader).getBukkitEntity();
        List<Raider> craftRaiders = new ArrayList<>();
        for (net.minecraft.world.entity.raid.Raider entityRaider : raiders) {
            craftRaiders.add((Raider) ((EthyleneEntity) entityRaider).getBukkitEntity());
        }
        RaidSpawnWaveEvent event = new RaidSpawnWaveEvent(new CraftRaid(raid), ((EthyleneLevel) raid.getLevel()).getWorld(), craftLeader, craftRaiders);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static LootGenerateEvent callLootGenerateEvent(net.minecraft.world.inventory.AbstractContainerMenu inventory, net.minecraft.world.level.storage.loot.LootTable lootTable, net.minecraft.world.level.storage.loot.LootContext lootInfo, List<net.minecraft.world.item.ItemStack> loot, boolean plugin) {
        CraftWorld world = ((EthyleneLevel) lootInfo.getLevel()).getWorld();
        net.minecraft.world.entity.Entity entity = lootInfo.getParamOrNull(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY);
        List<org.bukkit.inventory.ItemStack> bukkitLoot = loot.stream().map(CraftItemStack::asCraftMirror).collect(Collectors.toCollection(ArrayList::new));

        LootGenerateEvent event = new LootGenerateEvent(world, (entity != null ? ((EthyleneEntity) entity).getBukkitEntity() : null), inventory.getOwner(), lootTable.craftLootTable, CraftLootTable.convertContext(lootInfo), bukkitLoot, plugin);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callStriderTemperatureChangeEvent(net.minecraft.world.entity.monster.Strider strider, boolean shivering) {
        StriderTemperatureChangeEvent event = new StriderTemperatureChangeEvent((Strider) ((EthyleneEntity) strider).getBukkitEntity(), shivering);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static boolean handleEntitySpellCastEvent(net.minecraft.world.entity.monster.SpellcasterIllager caster, net.minecraft.world.entity.monster.SpellcasterIllager.IllagerSpell spell) {
        EntitySpellCastEvent event = new EntitySpellCastEvent((Spellcaster) ((EthyleneEntity) caster).getBukkitEntity(), CraftSpellcaster.toBukkitSpell(spell));
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    /**
     * ArrowBodyCountChangeEvent
     */
    public static ArrowBodyCountChangeEvent callArrowBodyCountChangeEvent(net.minecraft.world.entity.LivingEntity entity, int oldAmount, int newAmount, boolean isReset) {
        org.bukkit.entity.LivingEntity bukkitEntity = (LivingEntity) ((EthyleneEntity) entity).getBukkitEntity();

        ArrowBodyCountChangeEvent event = new ArrowBodyCountChangeEvent(bukkitEntity, oldAmount, newAmount, isReset);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    public static EntityExhaustionEvent callPlayerExhaustionEvent(net.minecraft.world.entity.player.Player humanEntity, EntityExhaustionEvent.ExhaustionReason exhaustionReason, float exhaustion) {
        EntityExhaustionEvent event = new EntityExhaustionEvent((HumanEntity) ((EthyleneEntity) humanEntity).getBukkitEntity(), exhaustionReason, exhaustion);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    public static PiglinBarterEvent callPiglinBarterEvent(net.minecraft.world.entity.monster.piglin.Piglin piglin, List<net.minecraft.world.item.ItemStack> outcome, net.minecraft.world.item.ItemStack input) {
        PiglinBarterEvent event = new PiglinBarterEvent((Piglin) ((EthyleneEntity) piglin).getBukkitEntity(), CraftItemStack.asBukkitCopy(input), outcome.stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList()));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void callEntitiesLoadEvent(net.minecraft.world.level.Level world, net.minecraft.world.level.ChunkPos coords, List<net.minecraft.world.entity.Entity> entities) {
        List<org.bukkit.entity.Entity> bukkitEntities = Collections.unmodifiableList(entities.stream().map(entity -> ((EthyleneEntity) entity).getBukkitEntity()).collect(Collectors.toList()));
        EntitiesLoadEvent event = new EntitiesLoadEvent(new CraftChunk((net.minecraft.server.level.ServerLevel) world, coords.x, coords.z), bukkitEntities);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callEntitiesUnloadEvent(net.minecraft.world.level.Level world, net.minecraft.world.level.ChunkPos coords, List<net.minecraft.world.entity.Entity> entities) {
        List<org.bukkit.entity.Entity> bukkitEntities = Collections.unmodifiableList(entities.stream().map(entity -> ((EthyleneEntity) entity).getBukkitEntity()).collect(Collectors.toList()));
        EntitiesUnloadEvent event = new EntitiesUnloadEvent(new CraftChunk((net.minecraft.server.level.ServerLevel) world, coords.x, coords.z), bukkitEntities);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static boolean callTNTPrimeEvent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, TNTPrimeEvent.PrimeCause cause, net.minecraft.world.entity.Entity causingEntity, net.minecraft.core.BlockPos causePosition) {
        org.bukkit.entity.Entity bukkitEntity = (causingEntity == null) ? null : ((EthyleneEntity) causingEntity).getBukkitEntity();
        org.bukkit.block.Block bukkitBlock = (causePosition == null) ? null : CraftBlock.at(world, causePosition);

        TNTPrimeEvent event = new TNTPrimeEvent(CraftBlock.at(world, pos), cause, bukkitEntity, bukkitBlock);
        Bukkit.getPluginManager().callEvent(event);

        return !event.isCancelled();
    }

    public static PlayerRecipeBookClickEvent callRecipeBookClickEvent(net.minecraft.server.level.ServerPlayer player, Recipe recipe, boolean shiftClick) {
        PlayerRecipeBookClickEvent event = new PlayerRecipeBookClickEvent((Player) ((EthyleneEntity) player).getBukkitEntity(), recipe, shiftClick);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTeleportEvent callEntityTeleportEvent(net.minecraft.world.entity.Entity nmsEntity, double x, double y, double z) {
        CraftEntity entity = ((EthyleneEntity) nmsEntity).getBukkitEntity();
        Location to = new Location(entity.getWorld(), x, y, z, nmsEntity.getYRot(), nmsEntity.getXRot());
        return callEntityTeleportEvent(nmsEntity, to);
    }

    public static EntityTeleportEvent callEntityTeleportEvent(net.minecraft.world.entity.Entity nmsEntity, Location to) {
        CraftEntity entity = ((EthyleneEntity) nmsEntity).getBukkitEntity();
        EntityTeleportEvent event = new org.bukkit.event.entity.EntityTeleportEvent(entity, entity.getLocation(), to);

        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    public static boolean callEntityInteractEvent(net.minecraft.world.entity.Entity nmsEntity, Block block) {
        EntityInteractEvent event = new EntityInteractEvent(((EthyleneEntity) nmsEntity).getBukkitEntity(), block);
        Bukkit.getPluginManager().callEvent(event);

        return !event.isCancelled();
    }

    public static EntityExplodeEvent callEntityExplodeEvent(net.minecraft.world.entity.Entity entity, List<Block> blocks, float yield, net.minecraft.world.level.Explosion.BlockInteraction effect) {
        EntityExplodeEvent event = new EntityExplodeEvent(((EthyleneEntity) entity).getBukkitEntity(), ((EthyleneEntity) entity).getBukkitEntity().getLocation(), blocks, yield, CraftExplosionResult.toBukkit(effect));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockExplodeEvent callBlockExplodeEvent(Block block, BlockState state, List<Block> blocks, float yield, net.minecraft.world.level.Explosion.BlockInteraction effect) {
        BlockExplodeEvent event = new BlockExplodeEvent(block, state, blocks, yield, CraftExplosionResult.toBukkit(effect));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static ExplosionPrimeEvent callExplosionPrimeEvent(Explosive explosive) {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(explosive);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static ExplosionPrimeEvent callExplosionPrimeEvent(net.minecraft.world.entity.Entity nmsEntity, float size, boolean fire) {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(((EthyleneEntity) nmsEntity).getBukkitEntity(), size, fire);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityKnockbackEvent callEntityKnockbackEvent(CraftLivingEntity entity, net.minecraft.world.entity.Entity attacker, EntityKnockbackEvent.KnockbackCause cause, double force, net.minecraft.world.phys.Vec3 raw, double x, double y, double z) {
        Vector bukkitRaw = new Vector(-raw.x, raw.y, -raw.z); // Due to how the knockback calculation works, we need to invert x and z.

        EntityKnockbackEvent event;
        if (attacker != null) {
            event = new EntityKnockbackByEntityEvent(entity, ((EthyleneEntity) attacker).getBukkitEntity(), cause, force, new Vector(-raw.x, raw.y, -raw.z), new Vector(x, y, z));
        } else {
            event = new EntityKnockbackEvent(entity, cause, force, new Vector(-raw.x, raw.y, -raw.z), new Vector(x, y, z));
        }

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void callEntityRemoveEvent(net.minecraft.world.entity.Entity entity, EntityRemoveEvent.Cause cause) {
        if (entity instanceof net.minecraft.server.level.ServerPlayer) {
            return; // Don't call for player
        }

        if (cause == null) {
            // Don't call if cause is null
            // This can happen when an entity changes dimension,
            // the entity gets removed during world gen or
            // the entity is removed before it is even spawned (when the spawn event is cancelled for example)
            return;
        }

        Bukkit.getPluginManager().callEvent(new EntityRemoveEvent(((EthyleneEntity) entity).getBukkitEntity(), cause));
    }
}
