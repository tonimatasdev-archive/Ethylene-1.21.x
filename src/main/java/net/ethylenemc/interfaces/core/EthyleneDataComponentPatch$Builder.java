package net.ethylenemc.interfaces.core;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

public interface EthyleneDataComponentPatch$Builder {
    void copy(DataComponentPatch orig);

    void clear(DataComponentType<?> type);

    boolean isSet(DataComponentType<?> type);

    boolean isEmpty();
}
