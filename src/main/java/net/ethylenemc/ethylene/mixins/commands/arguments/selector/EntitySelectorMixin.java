package net.ethylenemc.ethylene.mixins.commands.arguments.selector;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin {
    @Shadow protected abstract void checkPermissions(CommandSourceStack commandSourceStack) throws CommandSyntaxException;

    @Redirect(method = "checkPermissions", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandSourceStack;hasPermission(I)Z"))
    private boolean ethylene$checkPermissions(CommandSourceStack instance, int i) {
        return instance.hasPermission(2, "minecraft.command.selector"); // CraftBukkit
    }
}
