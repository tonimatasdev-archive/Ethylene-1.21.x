package org.bukkit.craftbukkit.inventory;

import net.ethylenemc.interfaces.world.EthyleneContainer;
import org.bukkit.block.DecoratedPot;
import org.bukkit.inventory.DecoratedPotInventory;
import org.bukkit.inventory.ItemStack;

public class CraftInventoryDecoratedPot extends CraftInventory implements DecoratedPotInventory {

    public CraftInventoryDecoratedPot(net.minecraft.world.Container inventory) {
        super(inventory);
    }

    @Override
    public void setItem(ItemStack item) {
        setItem(0, item);
    }

    @Override
    public ItemStack getItem() {
        return getItem(0);
    }

    @Override
    public DecoratedPot getHolder() {
        return (DecoratedPot) ((EthyleneContainer) inventory).getOwner();
    }
}
