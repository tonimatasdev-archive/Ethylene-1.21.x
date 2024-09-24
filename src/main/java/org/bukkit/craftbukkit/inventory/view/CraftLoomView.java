package org.bukkit.craftbukkit.inventory.view;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.banner.PatternType;
import org.bukkit.craftbukkit.block.banner.CraftPatternType;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.view.LoomView;

public class CraftLoomView extends CraftInventoryView<net.minecraft.world.inventory.LoomMenu> implements LoomView {

    public CraftLoomView(final HumanEntity player, final Inventory viewing, final net.minecraft.world.inventory.LoomMenu container) {
        super(player, viewing, container);
    }

    @Override
    public List<PatternType> getSelectablePatterns() {
        final List<net.minecraft.core.Holder<net.minecraft.world.level.block.entity.BannerPattern>> selectablePatterns = container.getSelectablePatterns();
        final List<PatternType> patternTypes = new ArrayList<>(selectablePatterns.size());
        for (final net.minecraft.core.Holder<net.minecraft.world.level.block.entity.BannerPattern> selectablePattern : selectablePatterns) {
            patternTypes.add(CraftPatternType.minecraftHolderToBukkit(selectablePattern));
        }
        return patternTypes;
    }

    @Override
    public int getSelectedPatternIndex() {
        return container.getSelectedBannerPatternIndex();
    }
}
