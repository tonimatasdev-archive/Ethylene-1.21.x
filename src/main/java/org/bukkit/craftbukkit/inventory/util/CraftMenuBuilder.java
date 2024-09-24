package org.bukkit.craftbukkit.inventory.util;


public interface CraftMenuBuilder {

    net.minecraft.world.inventory.AbstractContainerMenu build(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.inventory.MenuType<?> type);

    static CraftMenuBuilder worldAccess(LocationBoundContainerBuilder builder) {
        return (net.minecraft.server.level.ServerPlayer player, net.minecraft.world.inventory.MenuType<?> type) -> {
            return builder.build(player.nextContainerCounter(), player.getInventory(), net.minecraft.world.inventory.ContainerLevelAccess.create(player.level(), player.blockPosition()));
        };
    }

    static CraftMenuBuilder tileEntity(TileEntityObjectBuilder objectBuilder, net.minecraft.world.level.block.Block block) {
        return (net.minecraft.server.level.ServerPlayer player, net.minecraft.world.inventory.MenuType<?> type) -> {
            return objectBuilder.build(player.blockPosition(), block.defaultBlockState()).createMenu(player.nextContainerCounter(), player.getInventory(), player);
        };
    }

    interface TileEntityObjectBuilder {

        net.minecraft.world.MenuProvider build(net.minecraft.core.BlockPos blockPosition, net.minecraft.world.level.block.state.BlockState blockData);
    }

    interface LocationBoundContainerBuilder {

        net.minecraft.world.inventory.AbstractContainerMenu build(int syncId, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.inventory.ContainerLevelAccess access);
    }
}
