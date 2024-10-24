package net.ethylenemc.ethylene.mixins.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.ethylenemc.ethylene.interfaces.commands.arguments.EthyEntityArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(EntityArgument.class)
public abstract class EntityArgumentMixin implements EthyEntityArgument {
    @Unique
    private AtomicBoolean ethylene$overridePermissions = new AtomicBoolean(false);
    
    @Shadow protected abstract net.minecraft.commands.arguments.selector.EntitySelector parse(com.mojang.brigadier.StringReader stringReader, boolean bl) throws CommandSyntaxException;

    @Unique
    @Override
    public EntitySelector parse(StringReader stringreader, boolean flag, boolean overridePermissions) throws CommandSyntaxException {
        ethylene$overridePermissions.set(overridePermissions);
        return this.parse(stringreader, flag);
    }
    
    @Redirect(method = "parse(Lcom/mojang/brigadier/StringReader;Z)Lnet/minecraft/commands/arguments/selector/EntitySelector;", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/selector/EntitySelectorParser;parse()Lnet/minecraft/commands/arguments/selector/EntitySelector;"))
    private EntitySelector ethylene$parse(EntitySelectorParser instance) throws CommandSyntaxException {
        return instance.parse(ethylene$overridePermissions.getAndSet(false));
    }
}
