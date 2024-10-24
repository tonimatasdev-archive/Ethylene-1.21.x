package net.ethylenemc.interfaces.world.level.block.entity;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

import java.util.Set;

public interface EthyleneBlockEntity {
    Set<DataComponentType<?>> applyComponentsSet(DataComponentMap datacomponentmap, DataComponentPatch datacomponentpatch);
}
