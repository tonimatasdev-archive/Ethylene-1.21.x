package org.bukkit.craftbukkit.structure;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlockStates;
import org.bukkit.structure.Palette;

public class CraftPalette implements Palette {

    private final net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.Palette palette;
    private final net.minecraft.core.RegistryAccess registry;

    public CraftPalette(net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.Palette palette, net.minecraft.core.RegistryAccess registry) {
        this.palette = palette;
        this.registry = registry;
    }

    @Override
    public List<BlockState> getBlocks() {
        List<BlockState> blocks = new ArrayList<>();
        for (net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
            blocks.add(CraftBlockStates.getBlockState(registry, blockInfo.pos(), blockInfo.state(), blockInfo.nbt()));
        }
        return blocks;
    }

    @Override
    public int getBlockCount() {
        return palette.blocks().size();
    }
}
