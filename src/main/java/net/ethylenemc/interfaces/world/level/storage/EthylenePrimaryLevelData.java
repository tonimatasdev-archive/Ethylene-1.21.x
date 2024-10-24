package net.ethylenemc.interfaces.world.level.storage;

import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.LevelStem;

public interface EthylenePrimaryLevelData {
    void setCustomDimensions(Registry<LevelStem> value);
    
    void checkName(String name);
}
