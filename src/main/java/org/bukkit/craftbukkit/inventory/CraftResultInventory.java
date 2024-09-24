package org.bukkit.craftbukkit.inventory;

import org.bukkit.inventory.ItemStack;

public class CraftResultInventory extends CraftInventory {

    private final net.minecraft.world.Container resultInventory;

    public CraftResultInventory(net.minecraft.world.Container inventory, net.minecraft.world.Container resultInventory) {
        super(inventory);
        this.resultInventory = resultInventory;
    }

    public net.minecraft.world.Container getResultInventory() {
        return resultInventory;
    }

    public net.minecraft.world.Container getIngredientsInventory() {
        return inventory;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < getIngredientsInventory().getContainerSize()) {
            net.minecraft.world.item.ItemStack item = getIngredientsInventory().getItem(slot);
            return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
        } else {
            net.minecraft.world.item.ItemStack item = getResultInventory().getItem(slot - getIngredientsInventory().getContainerSize());
            return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
        }
    }

    @Override
    public void setItem(int index, ItemStack item) {
        if (index < getIngredientsInventory().getContainerSize()) {
            getIngredientsInventory().setItem(index, CraftItemStack.asNMSCopy(item));
        } else {
            getResultInventory().setItem((index - getIngredientsInventory().getContainerSize()), CraftItemStack.asNMSCopy(item));
        }
    }

    @Override
    public int getSize() {
        return getResultInventory().getContainerSize() + getIngredientsInventory().getContainerSize();
    }
}
