package org.bukkit.craftbukkit.inventory;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LlamaInventory;

public class CraftInventoryLlama extends CraftInventoryAbstractHorse implements LlamaInventory {

    private final net.minecraft.world.Container bodyArmorInventory;

    public CraftInventoryLlama(net.minecraft.world.Container inventory, net.minecraft.world.Container bodyArmorInventory) {
        super(inventory);
        this.bodyArmorInventory = bodyArmorInventory;
    }

    @Override
    public ItemStack getDecor() {
        net.minecraft.world.item.ItemStack item = bodyArmorInventory.getItem(0);
        return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
    }

    @Override
    public void setDecor(ItemStack stack) {
        bodyArmorInventory.setItem(0, CraftItemStack.asNMSCopy(stack));
    }
}
