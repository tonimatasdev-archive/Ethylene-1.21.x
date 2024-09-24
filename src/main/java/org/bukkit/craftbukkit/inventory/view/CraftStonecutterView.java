package org.bukkit.craftbukkit.inventory.view;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.inventory.view.StonecutterView;
import org.jetbrains.annotations.NotNull;

public class CraftStonecutterView extends CraftInventoryView<net.minecraft.world.inventory.StonecutterMenu> implements StonecutterView {

    public CraftStonecutterView(final HumanEntity player, final Inventory viewing, final net.minecraft.world.inventory.StonecutterMenu container) {
        super(player, viewing, container);
    }

    @Override
    public int getSelectedRecipeIndex() {
        return container.getSelectedRecipeIndex();
    }

    @NotNull
    @Override
    public List<StonecuttingRecipe> getRecipes() {
        final List<StonecuttingRecipe> recipes = new ArrayList<>();
        for (final net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.StonecutterRecipe> recipe : container.getRecipes()) {
            recipes.add((StonecuttingRecipe) recipe.toBukkitRecipe());
        }
        return recipes;
    }

    @Override
    public int getRecipeAmount() {
        return container.getNumRecipes();
    }
}
