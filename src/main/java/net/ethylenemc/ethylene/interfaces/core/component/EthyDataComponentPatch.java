package net.ethylenemc.ethylene.interfaces.core.component;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

public interface EthyDataComponentPatch {
    void copy(DataComponentPatch orig);

    void clear(DataComponentType<?> type);

    boolean isSet(DataComponentType<?> type);

    boolean isEmpty();
}
