package net.ethylenemc.interfaces.world.inventory;

import net.minecraft.world.item.crafting.RecipeHolder;

public interface EthyleneCraftingContainer {
    default RecipeHolder<?> getCurrentRecipe() {
        return null;
    }
}
