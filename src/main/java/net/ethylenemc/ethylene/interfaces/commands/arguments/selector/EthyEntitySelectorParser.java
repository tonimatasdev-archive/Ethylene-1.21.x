package net.ethylenemc.ethylene.interfaces.commands.arguments.selector;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.selector.EntitySelector;

public interface EthyEntitySelectorParser {
    void parseSelector(boolean overridePermissions) throws CommandSyntaxException;

    EntitySelector parse(boolean overridePermissions) throws CommandSyntaxException;
}
