package net.ethylenemc.interfaces.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface EthyleneContainerOpenersCounter {
    boolean getOpened();
    
    void setOpened(boolean value);
    
    void onAPIOpen(Level world, BlockPos blockposition, BlockState iblockdata);

    void onAPIClose(Level world, BlockPos blockposition, BlockState iblockdata);

    void openerAPICountChanged(Level world, BlockPos blockposition, BlockState iblockdata, int i, int j);
}
