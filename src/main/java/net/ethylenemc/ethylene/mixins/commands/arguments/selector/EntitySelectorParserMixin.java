package net.ethylenemc.ethylene.mixins.commands.arguments.selector;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.ethylenemc.ethylene.interfaces.commands.arguments.selector.EthyEntitySelectorParser;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(EntitySelectorParser.class)
public abstract class EntitySelectorParserMixin implements EthyEntitySelectorParser {
    @Shadow protected abstract void parseSelector() throws CommandSyntaxException;

    @Shadow public abstract EntitySelector parse() throws CommandSyntaxException;

    @Unique
    private AtomicBoolean ethylene$overridePermissions = new AtomicBoolean(false);

    @Unique
    @Override
    public void parseSelector(boolean overridePermissions) throws CommandSyntaxException {
        ethylene$overridePermissions.set(overridePermissions);
        parseSelector();
    }

    @Unique
    @Override
    public EntitySelector parse(boolean overridePermissions) throws CommandSyntaxException {
        ethylene$overridePermissions.set(overridePermissions);
        return parse();
    }
    
    @Redirect(method = "parseSelector", at = @At(value = "FIELD", target = "Lnet/minecraft/commands/arguments/selector/EntitySelectorParser;usesSelectors:Z", opcode = Opcodes.PUTFIELD))
    private void ethylene$parseSelector(EntitySelectorParser instance, boolean value) {
        instance.usesSelectors = !ethylene$overridePermissions.getAndSet(false);
    }
}
