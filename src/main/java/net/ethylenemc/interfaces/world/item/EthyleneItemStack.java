package net.ethylenemc.interfaces.world.item;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;

public interface EthyleneItemStack {
    void restorePatch(DataComponentPatch datacomponentpatch);

    void setItem(Item item);
}
