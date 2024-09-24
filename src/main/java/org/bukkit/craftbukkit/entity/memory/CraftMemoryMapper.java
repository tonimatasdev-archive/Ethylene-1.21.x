package org.bukkit.craftbukkit.entity.memory;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

public final class CraftMemoryMapper {

    private CraftMemoryMapper() {}

    public static Object fromNms(Object object) {
        if (object instanceof net.minecraft.core.GlobalPos) {
            return fromNms((net.minecraft.core.GlobalPos) object);
        } else if (object instanceof Long) {
            return (Long) object;
        } else if (object instanceof UUID) {
            return (UUID) object;
        } else if (object instanceof Boolean) {
            return (Boolean) object;
        } else if (object instanceof Integer) {
            return (Integer) object;
        }

        throw new UnsupportedOperationException("Do not know how to map " + object);
    }

    public static Object toNms(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Location) {
            return toNms((Location) object);
        } else if (object instanceof Long) {
            return (Long) object;
        } else if (object instanceof UUID) {
            return (UUID) object;
        } else if (object instanceof Boolean) {
            return (Boolean) object;
        } else if (object instanceof Integer) {
            return (Integer) object;
        }

        throw new UnsupportedOperationException("Do not know how to map " + object);
    }

    public static Location fromNms(net.minecraft.core.GlobalPos globalPos) {
        return new org.bukkit.Location(((CraftServer) Bukkit.getServer()).getServer().getLevel(globalPos.dimension()).getWorld(), globalPos.pos().getX(), globalPos.pos().getY(), globalPos.pos().getZ());
    }

    public static net.minecraft.core.GlobalPos toNms(Location location) {
        return net.minecraft.core.GlobalPos.of(((CraftWorld) location.getWorld()).getHandle().dimension(), net.minecraft.core.BlockPos.containing(location.getX(), location.getY(), location.getZ()));
    }
}
