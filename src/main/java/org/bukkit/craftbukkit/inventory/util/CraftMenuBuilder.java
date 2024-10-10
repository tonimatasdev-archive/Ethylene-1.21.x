package org.bukkit.craftbukkit.inventory.util;


import net.ethylenemc.EthyleneCaptures;

public interface CraftMenuBuilder {

    net.minecraft.world.inventory.AbstractContainerMenu build(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.inventory.MenuType<?> type);

    static CraftMenuBuilder worldAccess(LocationBoundContainerBuilder builder) {
        return (net.minecraft.server.level.ServerPlayer player, net.minecraft.world.inventory.MenuType<?> type) -> {
            player.nextContainerCounter();
            return builder.build(EthyleneCaptures.nextContainerCounter, player.getInventory(), net.minecraft.world.inventory.ContainerLevelAccess.create(player.level(), player.blockPosition())); // Ethylene - Method doesn't return an int
        };
    }

    static CraftMenuBuilder tileEntity(TileEntityObjectBuilder objectBuilder, net.minecraft.world.level.block.Block block) {
        return (net.minecraft.server.level.ServerPlayer player, net.minecraft.world.inventory.MenuType<?> type) -> {
            player.nextContainerCounter();
            return objectBuilder.build(player.blockPosition(), block.defaultBlockState()).createMenu(EthyleneCaptures.nextContainerCounter, player.getInventory(), player); // Ethylene - Method doesn't return an int
        };
    }

    interface TileEntityObjectBuilder {

        net.minecraft.world.MenuProvider build(net.minecraft.core.BlockPos blockPosition, net.minecraft.world.level.block.state.BlockState blockData);
    }

    interface LocationBoundContainerBuilder {

        net.minecraft.world.inventory.AbstractContainerMenu build(int syncId, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.inventory.ContainerLevelAccess access);
    }
}
