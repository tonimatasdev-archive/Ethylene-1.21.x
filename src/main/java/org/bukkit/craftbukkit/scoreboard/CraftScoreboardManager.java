package org.bukkit.craftbukkit.scoreboard;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.WeakCollection;
import org.bukkit.scoreboard.ScoreboardManager;

public final class CraftScoreboardManager implements ScoreboardManager {
    private final CraftScoreboard mainScoreboard;
    private final net.minecraft.server.MinecraftServer server;
    private final Collection<CraftScoreboard> scoreboards = new WeakCollection<>();
    private final Map<CraftPlayer, CraftScoreboard> playerBoards = new HashMap<>();

    public CraftScoreboardManager(net.minecraft.server.MinecraftServer minecraftserver, net.minecraft.world.scores.Scoreboard scoreboardServer) {
        mainScoreboard = new CraftScoreboard(scoreboardServer);
        server = minecraftserver;
        scoreboards.add(mainScoreboard);
    }

    @Override
    public CraftScoreboard getMainScoreboard() {
        return mainScoreboard;
    }

    @Override
    public CraftScoreboard getNewScoreboard() {
        CraftScoreboard scoreboard = new CraftScoreboard(new net.minecraft.server.ServerScoreboard(server));
        scoreboards.add(scoreboard);
        return scoreboard;
    }

    // CraftBukkit method
    public CraftScoreboard getPlayerBoard(CraftPlayer player) {
        CraftScoreboard board = playerBoards.get(player);
        return board == null ? getMainScoreboard() : board;
    }

    // CraftBukkit method
    public void setPlayerBoard(CraftPlayer player, org.bukkit.scoreboard.Scoreboard bukkitScoreboard) {
        Preconditions.checkArgument(bukkitScoreboard instanceof CraftScoreboard, "Cannot set player scoreboard to an unregistered net.minecraft.world.scores.Scoreboard");

        CraftScoreboard scoreboard = (CraftScoreboard) bukkitScoreboard;
        net.minecraft.world.scores.Scoreboard oldboard = getPlayerBoard(player).getHandle();
        net.minecraft.world.scores.Scoreboard newboard = scoreboard.getHandle();
        net.minecraft.server.level.ServerPlayer entityplayer = player.getHandle();

        if (oldboard == newboard) {
            return;
        }

        if (scoreboard == mainScoreboard) {
            playerBoards.remove(player);
        } else {
            playerBoards.put(player, scoreboard);
        }

        // Old objective tracking
        HashSet<net.minecraft.world.scores.Objective> removed = new HashSet<>();
        for (int i = 0; i < 3; ++i) {
            net.minecraft.world.scores.Objective scoreboardobjective = oldboard.getDisplayObjective(net.minecraft.world.scores.DisplaySlot.BY_ID.apply(i));
            if (scoreboardobjective != null && !removed.contains(scoreboardobjective)) {
                entityplayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetObjectivePacket(scoreboardobjective, 1));
                removed.add(scoreboardobjective);
            }
        }

        // Old team tracking
        Iterator<?> iterator = oldboard.getPlayerTeams().iterator();
        while (iterator.hasNext()) {
            net.minecraft.world.scores.PlayerTeam scoreboardteam = (net.minecraft.world.scores.PlayerTeam) iterator.next();
            entityplayer.connection.send(net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.createRemovePacket(scoreboardteam));
        }

        // The above is the reverse of the below method.
        server.getPlayerList().updateEntireScoreboard((net.minecraft.server.ServerScoreboard) newboard, player.getHandle());
    }

    // CraftBukkit method
    public void removePlayer(CraftPlayer player) {
        playerBoards.remove(player);
    }

    // CraftBukkit method
    public void forAllObjectives(net.minecraft.world.scores.criteria.ObjectiveCriteria criteria, net.minecraft.world.scores.ScoreHolder holder, Consumer<net.minecraft.world.scores.ScoreAccess> consumer) {
        for (CraftScoreboard scoreboard : scoreboards) {
            net.minecraft.world.scores.Scoreboard board = scoreboard.board;
            board.forAllObjectives(criteria, holder, (score) -> consumer.accept(score));
        }
    }
}
