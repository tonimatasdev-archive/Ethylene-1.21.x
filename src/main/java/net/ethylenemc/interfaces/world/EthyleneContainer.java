package net.ethylenemc.interfaces.world;

import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;

public interface EthyleneContainer {
    java.util.List<ItemStack> getContents();

    java.util.List<org.bukkit.entity.HumanEntity> getViewers();

    org.bukkit.inventory.InventoryHolder getOwner();

    void setMaxStackSize(int size);

    org.bukkit.Location getLocation();

    int MAX_STACK = 99;
}
