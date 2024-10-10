package net.ethylenemc.interfaces.world.level.saveddata.maps;

import net.minecraft.world.level.saveddata.maps.MapId;

import java.util.UUID;

public interface EthyleneMapItemSavedData {
    UUID uniqueId();
    
    void uniqueId(UUID value);

    MapId id();
}
