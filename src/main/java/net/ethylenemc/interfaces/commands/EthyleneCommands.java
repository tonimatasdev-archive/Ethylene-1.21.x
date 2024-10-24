package net.ethylenemc.interfaces.commands;

import net.minecraft.commands.CommandSourceStack;

public interface EthyleneCommands {
    void performPrefixedCommand(CommandSourceStack commandlistenerwrapper, String s, String label);
}
