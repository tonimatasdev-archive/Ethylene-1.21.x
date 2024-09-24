package org.bukkit.craftbukkit.inventory.view;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.view.LecternView;

public class CraftLecternView extends CraftInventoryView<net.minecraft.world.inventory.LecternMenu> implements LecternView {

    public CraftLecternView(final HumanEntity player, final Inventory viewing, final net.minecraft.world.inventory.LecternMenu container) {
        super(player, viewing, container);
    }

    @Override
    public int getPage() {
        return container.getPage();
    }

    @Override
    public void setPage(final int page) {
        Preconditions.checkArgument(page >= 0, "The minimum page is 0");
        container.setData(net.minecraft.world.level.block.entity.LecternBlockEntity.DATA_PAGE, page);
    }
}
