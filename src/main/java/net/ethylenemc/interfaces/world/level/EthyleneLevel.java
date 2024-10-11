package net.ethylenemc.interfaces.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CapturedBlockState;

import java.util.Map;

public interface EthyleneLevel {
    CraftWorld getWorld();

    CraftServer getCraftServer();

    ResourceKey<LevelStem> getTypeKey();
    
    void captureTreeGeneration(boolean value);
    
    void captureBlockStates(boolean value);

    Map<BlockPos, CapturedBlockState> capturedBlockStates();

    void notifyAndUpdatePhysics(BlockPos blockposition, LevelChunk chunk, BlockState oldBlock, BlockState newBlock, BlockState actualBlock, int i, int j);
}
