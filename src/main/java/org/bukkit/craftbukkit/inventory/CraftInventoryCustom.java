package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.ethylenemc.interfaces.world.EthyleneContainer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

public class CraftInventoryCustom extends CraftInventory {
    public CraftInventoryCustom(InventoryHolder owner, InventoryType type) {
        super(new MinecraftInventory(owner, type));
    }

    public CraftInventoryCustom(InventoryHolder owner, InventoryType type, String title) {
        super(new MinecraftInventory(owner, type, title));
    }

    public CraftInventoryCustom(InventoryHolder owner, int size) {
        super(new MinecraftInventory(owner, size));
    }

    public CraftInventoryCustom(InventoryHolder owner, int size, String title) {
        super(new MinecraftInventory(owner, size, title));
    }

    static class MinecraftInventory implements net.minecraft.world.Container, EthyleneContainer {
        private final net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> items;
        private int maxStack = MAX_STACK;
        private final List<HumanEntity> viewers;
        private final String title;
        private InventoryType type;
        private final InventoryHolder owner;

        public MinecraftInventory(InventoryHolder owner, InventoryType type) {
            this(owner, type.getDefaultSize(), type.getDefaultTitle());
            this.type = type;
        }

        public MinecraftInventory(InventoryHolder owner, InventoryType type, String title) {
            this(owner, type.getDefaultSize(), title);
            this.type = type;
        }

        public MinecraftInventory(InventoryHolder owner, int size) {
            this(owner, size, "Chest");
        }

        public MinecraftInventory(InventoryHolder owner, int size, String title) {
            Preconditions.checkArgument(title != null, "title cannot be null");
            this.items = net.minecraft.core.NonNullList.withSize(size, net.minecraft.world.item.ItemStack.EMPTY);
            this.title = title;
            this.viewers = new ArrayList<HumanEntity>();
            this.owner = owner;
            this.type = InventoryType.CHEST;
        }

        @Override
        public int getContainerSize() {
            return items.size();
        }

        @Override
        public net.minecraft.world.item.ItemStack getItem(int i) {
            return items.get(i);
        }

        @Override
        public net.minecraft.world.item.ItemStack removeItem(int i, int j) {
            net.minecraft.world.item.ItemStack stack = this.getItem(i);
            net.minecraft.world.item.ItemStack result;
            if (stack == net.minecraft.world.item.ItemStack.EMPTY) return stack;
            if (stack.getCount() <= j) {
                this.setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
                result = stack;
            } else {
                result = CraftItemStack.copyNMSStack(stack, j);
                stack.shrink(j);
            }
            this.setChanged();
            return result;
        }

        @Override
        public net.minecraft.world.item.ItemStack removeItemNoUpdate(int i) {
            net.minecraft.world.item.ItemStack stack = this.getItem(i);
            net.minecraft.world.item.ItemStack result;
            if (stack == net.minecraft.world.item.ItemStack.EMPTY) return stack;
            if (stack.getCount() <= 1) {
                this.setItem(i, null);
                result = stack;
            } else {
                result = CraftItemStack.copyNMSStack(stack, 1);
                stack.shrink(1);
            }
            return result;
        }

        @Override
        public void setItem(int i, net.minecraft.world.item.ItemStack itemstack) {
            items.set(i, itemstack);
            if (itemstack != net.minecraft.world.item.ItemStack.EMPTY && this.getMaxStackSize() > 0 && itemstack.getCount() > this.getMaxStackSize()) {
                itemstack.setCount(this.getMaxStackSize());
            }
        }

        @Override
        public int getMaxStackSize() {
            return maxStack;
        }

        @Override
        public void setMaxStackSize(int size) {
            maxStack = size;
        }

        @Override
        public void setChanged() {}

        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player entityhuman) {
            return true;
        }

        @Override
        public List<net.minecraft.world.item.ItemStack> getContents() {
            return items;
        }

        @Override
        public void onOpen(CraftHumanEntity who) {
            viewers.add(who);
        }

        @Override
        public void onClose(CraftHumanEntity who) {
            viewers.remove(who);
        }

        @Override
        public List<HumanEntity> getViewers() {
            return viewers;
        }

        public InventoryType getType() {
            return type;
        }

        @Override
        public InventoryHolder getOwner() {
            return owner;
        }

        @Override
        public boolean canPlaceItem(int i, net.minecraft.world.item.ItemStack itemstack) {
            return true;
        }

        @Override
        public void startOpen(net.minecraft.world.entity.player.Player entityHuman) {

        }

        @Override
        public void stopOpen(net.minecraft.world.entity.player.Player entityHuman) {

        }

        @Override
        public void clearContent() {
            items.clear();
        }

        @Override
        public Location getLocation() {
            return null;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public boolean isEmpty() {
            Iterator iterator = this.items.iterator();

            net.minecraft.world.item.ItemStack itemstack;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                itemstack = (net.minecraft.world.item.ItemStack) iterator.next();
            } while (itemstack.isEmpty());

            return false;
        }
    }
}
