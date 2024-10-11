package org.bukkit.craftbukkit.inventory.view;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.view.BrewingStandView;

public class CraftBrewingStandView extends CraftInventoryView<net.minecraft.world.inventory.BrewingStandMenu, BrewerInventory> implements BrewingStandView {

    public CraftBrewingStandView(final HumanEntity player, final BrewerInventory viewing, final net.minecraft.world.inventory.BrewingStandMenu container) {
        super(player, viewing, container);
    }

    @Override
    public int getFuelLevel() {
        return container.getFuel();
    }

    @Override
    public int getBrewingTicks() {
        return container.getBrewingTicks();
    }

    @Override
    public void setFuelLevel(final int fuelLevel) {
        Preconditions.checkArgument(fuelLevel > 0, "The given fuel level must be greater than 0");
        container.setData(net.minecraft.world.level.block.entity.BrewingStandBlockEntity.DATA_FUEL_USES, fuelLevel);
    }

    @Override
    public void setBrewingTicks(final int brewingTicks) {
        Preconditions.checkArgument(brewingTicks > 0, "The given brewing ticks must be greater than 0");
        container.setData(net.minecraft.world.level.block.entity.BrewingStandBlockEntity.DATA_BREW_TIME, brewingTicks);
    }
}
