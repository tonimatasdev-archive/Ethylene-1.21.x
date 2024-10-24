package net.ethylenemc.ethylene.interfaces.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.selector.EntitySelector;

public interface EthyEntityArgument {
    EntitySelector parse(StringReader stringreader, boolean flag, boolean overridePermissions) throws CommandSyntaxException;
}
