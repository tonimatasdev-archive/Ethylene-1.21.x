package org.bukkit.craftbukkit.inventory;

import java.util.Iterator;
import java.util.Map;
import org.bukkit.inventory.Recipe;

public class RecipeIterator implements Iterator<Recipe> {
    private final Iterator<Map.Entry<net.minecraft.world.item.crafting.RecipeType<?>, net.minecraft.world.item.crafting.RecipeHolder<?>>> recipes;

    public RecipeIterator() {
        this.recipes = net.minecraft.server.MinecraftServer.getServer().getRecipeManager().byType.entries().iterator();
    }

    @Override
    public boolean hasNext() {
        return recipes.hasNext();
    }

    @Override
    public Recipe next() {
        return recipes.next().getValue().toBukkitRecipe();
    }

    @Override
    public void remove() {
        recipes.remove();
    }
}
