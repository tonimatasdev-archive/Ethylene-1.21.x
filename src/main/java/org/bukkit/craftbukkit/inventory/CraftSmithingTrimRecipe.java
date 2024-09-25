package org.bukkit.craftbukkit.inventory;

import net.ethylenemc.EthyleneStatic;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTrimRecipe;

public class CraftSmithingTrimRecipe extends SmithingTrimRecipe implements CraftRecipe {

    public CraftSmithingTrimRecipe(NamespacedKey key, RecipeChoice template, RecipeChoice base, RecipeChoice addition) {
        super(key, template, base, addition);
    }

    public static CraftSmithingTrimRecipe fromBukkitRecipe(SmithingTrimRecipe recipe) {
        if (recipe instanceof CraftSmithingTrimRecipe) {
            return (CraftSmithingTrimRecipe) recipe;
        }
        CraftSmithingTrimRecipe ret = new CraftSmithingTrimRecipe(recipe.getKey(), recipe.getTemplate(), recipe.getBase(), recipe.getAddition());
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        EthyleneStatic.getServer().getRecipeManager().addRecipe(new net.minecraft.world.item.crafting.RecipeHolder<>(CraftNamespacedKey.toMinecraft(this.getKey()), new net.minecraft.world.item.crafting.SmithingTrimRecipe(toNMS(this.getTemplate(), false), toNMS(this.getBase(), false), toNMS(this.getAddition(), false))));
    }
}
