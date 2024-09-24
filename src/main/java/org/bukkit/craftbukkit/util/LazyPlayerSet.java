package org.bukkit.craftbukkit.util;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.List;
import org.bukkit.entity.Player;

public class LazyPlayerSet extends LazyHashSet<Player> {

    private final net.minecraft.server.MinecraftServer server;

    public LazyPlayerSet(net.minecraft.server.MinecraftServer server) {
        this.server = server;
    }

    @Override
    HashSet<Player> makeReference() {
        Preconditions.checkState(reference == null, "Reference already created!");
        List<net.minecraft.server.level.ServerPlayer> players = server.getPlayerList().players;
        HashSet<Player> reference = new HashSet<Player>(players.size());
        for (net.minecraft.server.level.ServerPlayer player : players) {
            reference.add(player.getBukkitEntity());
        }
        return reference;
    }
}
