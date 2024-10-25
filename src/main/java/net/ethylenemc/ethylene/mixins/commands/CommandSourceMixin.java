package net.ethylenemc.ethylene.mixins.commands;

import net.ethylenemc.ethylene.interfaces.commands.EthyCommandSource;
import net.minecraft.commands.CommandSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandSource.class)
public interface CommandSourceMixin extends EthyCommandSource {
}
