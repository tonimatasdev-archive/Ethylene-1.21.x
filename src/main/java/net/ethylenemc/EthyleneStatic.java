package net.ethylenemc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;

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
}
