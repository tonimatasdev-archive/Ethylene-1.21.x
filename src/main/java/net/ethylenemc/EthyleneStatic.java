package net.ethylenemc;

import net.ethylenemc.interfaces.world.entity.EthyleneMob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;

import java.util.List;

public class EthyleneStatic {

    public static final TicketType<Unit> PLUGIN = TicketType.create("plugin", (a, b) -> 0); // CraftBukkit
    public static final TicketType<org.bukkit.plugin.Plugin> PLUGIN_TICKET = TicketType.create("plugin_ticket", (plugin1, plugin2) -> plugin1.getClass().getName().compareTo(plugin2.getClass().getName())); // CraftBukkit
    
    @Deprecated
    public static MinecraftServer getServer() {
        return (Bukkit.getServer() instanceof CraftServer) ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }

    @Deprecated
    public static RegistryAccess getDefaultRegistryAccess() {
        return CraftRegistry.getMinecraftRegistry();
    }

    public static AABB calculateItemFrameBoundingBox(BlockPos blockPos, Direction direction) {
        Vec3 vec3 = Vec3.atCenterOf(blockPos).relative(direction, -0.46875);
        Direction.Axis axis = direction.getAxis();
        double d = axis == Direction.Axis.X ? 0.0625 : 0.75;
        double e = axis == Direction.Axis.Y ? 0.0625 : 0.75;
        double g = axis == Direction.Axis.Z ? 0.0625 : 0.75;
        return AABB.ofSize(vec3, d, e, g);
    }

    public static AABB calculatePaintingBoundingBox(BlockPos blockPos, Direction direction, int width, int height) {
        Vec3 vec3 = Vec3.atCenterOf(blockPos).relative(direction, -0.46875);
        // CraftBukkit start
        double d0 = offsetForPaintingSize(width);
        double d1 = offsetForPaintingSize(height);
        // CraftBukkit end
        Direction direction2 = direction.getCounterClockWise();
        Vec3 vec32 = vec3.relative(direction2, d0).relative(Direction.UP, d1);
        Direction.Axis axis = direction.getAxis();
        // CraftBukkit start
        double d2 = axis == Direction.Axis.X ? 0.0625D : (double) width;
        double d3 = (double) height;
        double d4 = axis == Direction.Axis.Z ? 0.0625D : (double) width;
        // CraftBukkit end
        return AABB.ofSize(vec32, d2, d3, d4);
    }

    private static double offsetForPaintingSize(int i) { // CraftBukkit - static
        return i % 2 == 0 ? 0.5D : 0.0D;
    }

    public static List<Player> getHumansInRange(Level world, BlockPos blockposition, int i) {
        double d0 = (double) (i * 10 + 10);
        AABB axisalignedbb = (new AABB(blockposition)).inflate(d0).expandTowards(0.0D, (double) world.getHeight(), 0.0D);
        return world.getEntitiesOfClass(Player.class, axisalignedbb);
    }

    public static ZombieVillager zombifyVillager(ServerLevel serverLevel, Villager villager, net.minecraft.core.BlockPos blockPosition, boolean silent, CreatureSpawnEvent.SpawnReason spawnReason) {
        ZombieVillager zombieVillager = ((EthyleneMob) villager).convertTo(EntityType.ZOMBIE_VILLAGER, false, EntityTransformEvent.TransformReason.INFECTION, spawnReason);

        if (zombieVillager != null) {
            zombieVillager.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(zombieVillager.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true));
            zombieVillager.setVillagerData(villager.getVillagerData());
            zombieVillager.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
            zombieVillager.setTradeOffers(villager.getOffers().copy());
            zombieVillager.setVillagerXp(villager.getVillagerXp());

            if (!silent) {
                serverLevel.levelEvent(null, 1026, blockPosition, 0);
            }
        }

        return zombieVillager;
    }
    
    public static InteractionResult applyBonemeal(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockPos blockPos2 = blockPos.relative(useOnContext.getClickedFace());
        if (BoneMealItem.growCrop(useOnContext.getItemInHand(), level, blockPos)) {
            if (!level.isClientSide) {
                useOnContext.getPlayer().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                level.levelEvent(1505, blockPos, 15);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            BlockState blockState = level.getBlockState(blockPos);
            boolean bl = blockState.isFaceSturdy(level, blockPos, useOnContext.getClickedFace());
            if (bl && BoneMealItem.growWaterPlant(useOnContext.getItemInHand(), level, blockPos2, useOnContext.getClickedFace())) {
                if (!level.isClientSide) {
                    useOnContext.getPlayer().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                    level.levelEvent(1505, blockPos2, 15);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }
}
