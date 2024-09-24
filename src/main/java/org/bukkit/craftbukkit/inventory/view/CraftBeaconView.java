package org.bukkit.craftbukkit.inventory.view;

import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.view.BeaconView;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public class CraftBeaconView extends CraftInventoryView<net.minecraft.world.inventory.BeaconMenu> implements BeaconView {

    public CraftBeaconView(final HumanEntity player, final Inventory viewing, final net.minecraft.world.inventory.BeaconMenu container) {
        super(player, viewing, container);
    }

    @Override
    public int getTier() {
        return container.getLevels();
    }

    @Nullable
    @Override
    public PotionEffectType getPrimaryEffect() {
        return container.getPrimaryEffect() != null ? CraftPotionEffectType.minecraftHolderToBukkit(container.getPrimaryEffect()) : null;
    }

    @Nullable
    @Override
    public PotionEffectType getSecondaryEffect() {
        return container.getSecondaryEffect() != null ? CraftPotionEffectType.minecraftHolderToBukkit(container.getSecondaryEffect()) : null;
    }

    @Override
    public void setPrimaryEffect(@Nullable final PotionEffectType effectType) {
        container.setData(net.minecraft.world.level.block.entity.BeaconBlockEntity.DATA_PRIMARY, net.minecraft.world.inventory.BeaconMenu.encodeEffect(CraftPotionEffectType.bukkitToMinecraftHolder(effectType)));
    }

    @Override
    public void setSecondaryEffect(@Nullable final PotionEffectType effectType) {
        container.setData(net.minecraft.world.level.block.entity.BeaconBlockEntity.DATA_SECONDARY, net.minecraft.world.inventory.BeaconMenu.encodeEffect(CraftPotionEffectType.bukkitToMinecraftHolder(effectType)));
    }
}
