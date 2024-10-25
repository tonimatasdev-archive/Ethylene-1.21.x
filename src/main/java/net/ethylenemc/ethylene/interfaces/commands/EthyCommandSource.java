package net.ethylenemc.ethylene.interfaces.commands;

import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;

public interface EthyCommandSource {
    CommandSender getBukkitSender(CommandSourceStack wrapper);
}
