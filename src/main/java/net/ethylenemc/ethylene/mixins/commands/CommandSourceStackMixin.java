package net.ethylenemc.ethylene.mixins.commands;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.tree.CommandNode;
import net.ethylenemc.ethylene.interfaces.commands.EthyCommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin implements EthyCommandSourceStack {
    @Shadow public abstract boolean hasPermission(int i);

    @Shadow public abstract ServerLevel getLevel();

    @Shadow @Final private int permissionLevel;

    @Shadow @Final public CommandSource source;
    @Unique
    public volatile CommandNode currentCommand;
    
    @Inject(method = "hasPermission", at = @At("HEAD"), cancellable = true)
    private void ethylene$hasPermission(int i, CallbackInfoReturnable<Boolean> cir) {
        CommandNode currentCommand = this.currentCommand;
        
        if (currentCommand != null) {
            cir.setReturnValue(hasPermission(i, org.bukkit.craftbukkit.command.VanillaCommandWrapper.getPermission(currentCommand)));
        }
    }

    @Unique
    @Override
    public boolean hasPermission(int i, String bukkitPermission) {
        return ((getLevel() == null || !getLevel().getCraftServer().ignoreVanillaPermissions) && this.permissionLevel >= i) || getBukkitSender().hasPermission(bukkitPermission);
    }
    
    @ModifyExpressionValue(method = "broadcastToAdmins", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;isOp(Lcom/mojang/authlib/GameProfile;)Z"))
    private boolean ethylene$broadcastToAdmins(boolean original, @Local ServerPlayer player) {
        return player.getBukkitEntity().hasPermission("minecraft.admin.command_feedback");
    }
    
    @Unique
    @Override
    public CommandSender getBukkitSender() {
        return source.getBukkitSender((CommandSourceStack) (Object) this);
    }
}
