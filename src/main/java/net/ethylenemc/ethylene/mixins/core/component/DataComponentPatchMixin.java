package net.ethylenemc.ethylene.mixins.core.component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.ethylenemc.ethylene.interfaces.core.component.EthyDataComponentPatch;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(DataComponentPatch.Builder.class)
public class DataComponentPatchMixin implements EthyDataComponentPatch {
    @Shadow @Final private Reference2ObjectMap<DataComponentType<?>, Optional<?>> map;

    @Unique
    @Override
    public void copy(DataComponentPatch orig) {
        this.map.putAll(orig.map);
    }

    @Unique
    @Override
    public void clear(DataComponentType<?> type) {
        this.map.remove(type);
    }

    @Unique
    @Override
    public boolean isSet(DataComponentType<?> type) {
        return map.containsKey(type);
    }

    @Unique
    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof DataComponentPatch.Builder patch) {
            return this.map.equals(patch.map);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }
}
