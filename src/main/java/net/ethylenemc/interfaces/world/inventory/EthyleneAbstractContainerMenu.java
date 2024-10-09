package net.ethylenemc.interfaces.world.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.inventory.InventoryView;

public interface EthyleneAbstractContainerMenu {
    InventoryView getBukkitView();

    void setCheckReachable(boolean check);

    void transferTo(AbstractContainerMenu other, org.bukkit.craftbukkit.entity.CraftHumanEntity player);

    Component getTitle();

    void setTitle(Component title);

    void broadcastCarriedItem();
}
