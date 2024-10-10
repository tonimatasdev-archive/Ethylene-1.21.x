package net.ethylenemc.interfaces.server.level;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.jetbrains.annotations.Nullable;

public interface EthyleneServerPlayer {
    CraftPlayer.TransferCookieConnection getTransferCookieConnection();
    
    String displayName();
    
    void displayName(String value);
    
    Component listName();
    
    void listName(Component value);

    Location compassTarget();

    long getPlayerTime();

    WeatherType getPlayerWeather();

    void resetPlayerWeather();

    void setPlayerWeather(WeatherType type, boolean plugin);

    long timeOffset();
    
    void timeOffset(long value);

    boolean relativeTime();

    void relativeTime(boolean value);

    void setRespawnPosition(ResourceKey<Level> resourcekey, @Nullable BlockPos blockposition, float f, boolean flag, boolean flag1, PlayerSpawnChangeEvent.Cause cause);
    
    int newExp();
    
    void newExp(int value);
    
    int newLevel();
    
    void newLevel(int value);
    
    int newTotalExp();
    
    void newTotalExp(int value);
    
    boolean keepLevel();
    
    void keepLevel(boolean value);
    
    void maxHealthCache(double value);
}

