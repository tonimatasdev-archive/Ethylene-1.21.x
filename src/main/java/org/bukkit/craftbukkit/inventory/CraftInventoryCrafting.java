package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.List;

import net.ethylenemc.interfaces.world.EthyleneContainer;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CraftInventoryCrafting extends CraftInventory implements CraftingInventory {
    private final net.minecraft.world.Container resultInventory;

    public CraftInventoryCrafting(net.minecraft.world.inventory.CraftingContainer inventory, net.minecraft.world.Container resultInventory) {
        super(inventory);
        this.resultInventory = resultInventory;
    }

    public net.minecraft.world.Container getResultInventory() {
        return resultInventory;
    }

    public net.minecraft.world.inventory.CraftingContainer getMatrixInventory() {
        return (net.minecraft.world.inventory.CraftingContainer) inventory;
    }

    @Override
    public int getSize() {
        return getResultInventory().getContainerSize() + getMatrixInventory().getContainerSize();
    }

    @Override
    public void setContents(ItemStack[] items) {
        Preconditions.checkArgument(items.length <= getSize(), "Invalid inventory size (%s); expected %s or less", items.length, getSize());
        setContents(items[0], Arrays.copyOfRange(items, 1, items.length));
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] items = new ItemStack[getSize()];
        List<net.minecraft.world.item.ItemStack> mcResultItems = ((EthyleneContainer) getResultInventory()).getContents();

        int i = 0;
        for (i = 0; i < mcResultItems.size(); i++) {
            items[i] = CraftItemStack.asCraftMirror(mcResultItems.get(i));
        }

        List<net.minecraft.world.item.ItemStack> mcItems = ((EthyleneContainer) getMatrixInventory()).getContents();

        for (int j = 0; j < mcItems.size(); j++) {
            items[i + j] = CraftItemStack.asCraftMirror(mcItems.get(j));
        }

        return items;
    }

    public void setContents(ItemStack result, ItemStack[] contents) {
        setResult(result);
        setMatrix(contents);
    }

    @Override
    public CraftItemStack getItem(int index) {
        if (index < getResultInventory().getContainerSize()) {
            net.minecraft.world.item.ItemStack item = getResultInventory().getItem(index);
            return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
        } else {
            net.minecraft.world.item.ItemStack item = getMatrixInventory().getItem(index - getResultInventory().getContainerSize());
            return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
        }
    }

    @Override
    public void setItem(int index, ItemStack item) {
        if (index < getResultInventory().getContainerSize()) {
            getResultInventory().setItem(index, CraftItemStack.asNMSCopy(item));
        } else {
            getMatrixInventory().setItem((index - getResultInventory().getContainerSize()), CraftItemStack.asNMSCopy(item));
        }
    }

    @Override
    public ItemStack[] getMatrix() {
        List<net.minecraft.world.item.ItemStack> matrix = ((EthyleneContainer) getMatrixInventory()).getContents();

        return asCraftMirror(matrix);
    }

    @Override
    public ItemStack getResult() {
        net.minecraft.world.item.ItemStack item = getResultInventory().getItem(0);
        if (!item.isEmpty()) return CraftItemStack.asCraftMirror(item);
        return null;
    }

    @Override
    public void setMatrix(ItemStack[] contents) {
        Preconditions.checkArgument(contents.length <= getMatrixInventory().getContainerSize(), "Invalid inventory size (%s); expected %s or less", contents.length, getMatrixInventory().getContainerSize());

        for (int i = 0; i < getMatrixInventory().getContainerSize(); i++) {
            if (i < contents.length) {
                getMatrixInventory().setItem(i, CraftItemStack.asNMSCopy(contents[i]));
            } else {
                getMatrixInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void setResult(ItemStack item) {
        List<net.minecraft.world.item.ItemStack> contents = ((EthyleneContainer) getResultInventory()).getContents();
        contents.set(0, CraftItemStack.asNMSCopy(item));
    }

    @Override
    public Recipe getRecipe() {
        net.minecraft.world.item.crafting.RecipeHolder<?> recipe = getMatrixInventory().getCurrentRecipe();
        return recipe == null ? null : recipe.toBukkitRecipe();
    }
}
