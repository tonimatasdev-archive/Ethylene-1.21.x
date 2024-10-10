package net.ethylenemc.interfaces.world.level.block.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface EthyleneBeehiveBlockEntity {
    int getMaxBees();
    
    void setMaxBees(int value);

    List<Entity> releaseBees(BlockState iblockdata, BeehiveBlockEntity.BeeReleaseStatus tileentitybeehive_releasestatus, boolean force);
}
