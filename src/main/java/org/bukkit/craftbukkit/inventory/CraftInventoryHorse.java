package org.bukkit.craftbukkit.inventory;

import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;

public class CraftInventoryHorse extends CraftInventoryAbstractHorse implements HorseInventory {

    private final net.minecraft.world.Container bodyArmorInventory;

    public CraftInventoryHorse(net.minecraft.world.Container inventory, net.minecraft.world.Container bodyArmorInventory) {
        super(inventory);
        this.bodyArmorInventory = bodyArmorInventory;
    }

    @Override
    public ItemStack getArmor() {
        net.minecraft.world.item.ItemStack item = bodyArmorInventory.getItem(0);
        return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
    }

    @Override
    public void setArmor(ItemStack stack) {
        bodyArmorInventory.setItem(0, CraftItemStack.asNMSCopy(stack));
    }
}
