package org.bukkit.craftbukkit.inventory;

import net.ethylenemc.interfaces.world.item.trading.EthyleneMerchant;
import net.ethylenemc.interfaces.world.item.trading.EthyleneMerchantOffer;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

public class CraftInventoryMerchant extends CraftInventory implements MerchantInventory {

    private final net.minecraft.world.item.trading.Merchant merchant;

    public CraftInventoryMerchant(net.minecraft.world.item.trading.Merchant merchant, net.minecraft.world.inventory.MerchantContainer inventory) {
        super(inventory);
        this.merchant = merchant;
    }

    @Override
    public int getSelectedRecipeIndex() {
        return getInventory().selectionHint;
    }

    @Override
    public MerchantRecipe getSelectedRecipe() {
        net.minecraft.world.item.trading.MerchantOffer nmsRecipe = getInventory().getActiveOffer();
        return (nmsRecipe == null) ? null : ((EthyleneMerchantOffer) nmsRecipe).asBukkit();
    }

    @Override
    public net.minecraft.world.inventory.MerchantContainer getInventory() {
        return (net.minecraft.world.inventory.MerchantContainer) inventory;
    }

    @Override
    public Merchant getMerchant() {
        return ((EthyleneMerchant) merchant).getCraftMerchant();
    }
}
