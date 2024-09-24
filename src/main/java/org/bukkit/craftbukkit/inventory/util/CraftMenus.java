package org.bukkit.craftbukkit.inventory.util;

import static org.bukkit.craftbukkit.inventory.util.CraftMenuBuilder.*;
import org.bukkit.craftbukkit.inventory.CraftMenuType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.inventory.view.BeaconView;
import org.bukkit.inventory.view.BrewingStandView;
import org.bukkit.inventory.view.CrafterView;
import org.bukkit.inventory.view.EnchantmentView;
import org.bukkit.inventory.view.FurnaceView;
import org.bukkit.inventory.view.LecternView;
import org.bukkit.inventory.view.LoomView;
import org.bukkit.inventory.view.MerchantView;
import org.bukkit.inventory.view.StonecutterView;

public final class CraftMenus {

    public record MenuTypeData<V extends InventoryView>(Class<V> viewClass, CraftMenuBuilder menuBuilder) {
    }

    private static final CraftMenuBuilder STANDARD = (player, menuType) -> menuType.create(player.nextContainerCounter(), player.getInventory());

    public static <V extends InventoryView> MenuTypeData<V> getMenuTypeData(CraftMenuType<?> menuType) {
        // this isn't ideal as both dispenser and dropper are 3x3, InventoryType can't currently handle generic 3x3s with size 9
        // this needs to be removed when inventory creation is overhauled
        if (menuType == MenuType.GENERIC_3X3) {
            return asType(new MenuTypeData<>(InventoryView.class, tileEntity(net.minecraft.world.level.block.entity.DispenserBlockEntity::new, net.minecraft.world.level.block.Blocks.DISPENSER)));
        }
        if (menuType == MenuType.CRAFTER_3X3) {
            return asType(new MenuTypeData<>(CrafterView.class, tileEntity(net.minecraft.world.level.block.entity.CrafterBlockEntity::new, net.minecraft.world.level.block.Blocks.CRAFTER)));
        }
        if (menuType == MenuType.ANVIL) {
            return asType(new MenuTypeData<>(AnvilView.class, worldAccess(net.minecraft.world.inventory.AnvilMenu::new)));
        }
        if (menuType == MenuType.BEACON) {
            return asType(new MenuTypeData<>(BeaconView.class, tileEntity(net.minecraft.world.level.block.entity.BeaconBlockEntity::new, net.minecraft.world.level.block.Blocks.BEACON)));
        }
        if (menuType == MenuType.BLAST_FURNACE) {
            return asType(new MenuTypeData<>(FurnaceView.class, tileEntity(net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity::new, net.minecraft.world.level.block.Blocks.BLAST_FURNACE)));
        }
        if (menuType == MenuType.BREWING_STAND) {
            return asType(new MenuTypeData<>(BrewingStandView.class, tileEntity(net.minecraft.world.level.block.entity.BrewingStandBlockEntity::new, net.minecraft.world.level.block.Blocks.BREWING_STAND)));
        }
        if (menuType == MenuType.CRAFTING) {
            return asType(new MenuTypeData<>(InventoryView.class, worldAccess(net.minecraft.world.inventory.CraftingMenu::new)));
        }
        if (menuType == MenuType.ENCHANTMENT) {
            return asType(new MenuTypeData<>(EnchantmentView.class, (player, type) -> {
                return new net.minecraft.world.SimpleMenuProvider((syncId, inventory, human) -> {
                    return worldAccess(net.minecraft.world.inventory.EnchantmentMenu::new).build(player, type);
                }, net.minecraft.network.chat.Component.empty()).createMenu(player.nextContainerCounter(), player.getInventory(), player);
            }));
        }
        if (menuType == MenuType.FURNACE) {
            return asType(new MenuTypeData<>(FurnaceView.class, tileEntity(net.minecraft.world.level.block.entity.FurnaceBlockEntity::new, net.minecraft.world.level.block.Blocks.FURNACE)));
        }
        if (menuType == MenuType.GRINDSTONE) {
            return asType(new MenuTypeData<>(InventoryView.class, worldAccess(net.minecraft.world.inventory.GrindstoneMenu::new)));
        }
        // We really don't need to be creating a tile entity for hopper but currently InventoryType doesn't have capacity
        // to understand otherwise
        if (menuType == MenuType.HOPPER) {
            return asType(new MenuTypeData<>(InventoryView.class, tileEntity(net.minecraft.world.level.block.entity.HopperBlockEntity::new, net.minecraft.world.level.block.Blocks.HOPPER)));
        }
        // We also don't need to create a tile entity for lectern, but again InventoryType isn't smart enough to know any better
        if (menuType == MenuType.LECTERN) {
            return asType(new MenuTypeData<>(LecternView.class, tileEntity(net.minecraft.world.level.block.entity.LecternBlockEntity::new, net.minecraft.world.level.block.Blocks.LECTERN)));
        }
        if (menuType == MenuType.LOOM) {
            return asType(new MenuTypeData<>(LoomView.class, STANDARD));
        }
        if (menuType == MenuType.MERCHANT) {
            return asType(new MenuTypeData<>(MerchantView.class, STANDARD));
        }
        if (menuType == MenuType.SMITHING) {
            return asType(new MenuTypeData<>(InventoryView.class, worldAccess(net.minecraft.world.inventory.SmithingMenu::new)));
        }
        if (menuType == MenuType.SMOKER) {
            return asType(new MenuTypeData<>(FurnaceView.class, tileEntity(net.minecraft.world.level.block.entity.SmokerBlockEntity::new, net.minecraft.world.level.block.Blocks.SMOKER)));
        }
        if (menuType == MenuType.CARTOGRAPHY_TABLE) {
            return asType(new MenuTypeData<>(InventoryView.class, worldAccess(net.minecraft.world.inventory.CartographyTableMenu::new)));
        }
        if (menuType == MenuType.STONECUTTER) {
            return asType(new MenuTypeData<>(StonecutterView.class, worldAccess(net.minecraft.world.inventory.StonecutterMenu::new)));
        }

        return asType(new MenuTypeData<>(InventoryView.class, STANDARD));
    }

    private static <V extends InventoryView> MenuTypeData<V> asType(MenuTypeData<?> data) {
        return (MenuTypeData<V>) data;
    }
}
