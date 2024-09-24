package net.ethylenemc.interfaces.world.level;

import org.bukkit.craftbukkit.command.ProxiedNativeCommandSender;
import org.bukkit.craftbukkit.command.ServerCommandSender;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLightningStrike;
import org.bukkit.craftbukkit.inventory.CraftMetaBook;
import org.bukkit.craftbukkit.inventory.CraftMetaBookSigned;

public interface EthyleneWorldGenLevel {
    net.minecraft.server.level.ServerLevel getMinecraftWorld();

    CraftMetaBookSigned
}
