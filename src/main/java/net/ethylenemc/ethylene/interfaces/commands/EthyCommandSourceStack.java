package net.ethylenemc.ethylene.interfaces.commands;

import org.bukkit.command.CommandSender;

public interface EthyCommandSourceStack {
    boolean hasPermission(int i, String bukkitPermission);

    CommandSender getBukkitSender();
}
