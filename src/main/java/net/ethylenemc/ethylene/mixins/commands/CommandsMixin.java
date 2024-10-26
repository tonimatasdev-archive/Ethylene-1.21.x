package net.ethylenemc.ethylene.mixins.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.ethylenemc.ethylene.interfaces.commands.EthyCommands;
import net.minecraft.commands.*;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

@Mixin(Commands.class)
public abstract class CommandsMixin implements EthyCommands {
    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Shadow public abstract void performCommand(ParseResults<CommandSourceStack> parseResults, String string);

    @Shadow protected abstract void fillUsableCommands(CommandNode<CommandSourceStack> commandNode, CommandNode<SharedSuggestionProvider> commandNode2, CommandSourceStack commandSourceStack, Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> map);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ethylene$init(Commands.CommandSelection commandSelection, CommandBuildContext commandBuildContext, CallbackInfo ci) {
        dispatcher.setConsumer(ExecutionCommandSource.resultConsumer());
    }

    @Unique
    @Override
    public void dispatchServerCommand(CommandSourceStack sender, String command) {
        Joiner joiner = Joiner.on(" ");
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        ServerCommandEvent event = new ServerCommandEvent(sender.getBukkitSender(), command);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        command = event.getCommand();

        String[] args = command.split(" ");

        String cmd = args[0];
        if (cmd.startsWith("minecraft:")) cmd = cmd.substring("minecraft:".length());
        if (cmd.startsWith("bukkit:")) cmd = cmd.substring("bukkit:".length());

        // Block disallowed commands
        if (cmd.equalsIgnoreCase("stop") || cmd.equalsIgnoreCase("kick") || cmd.equalsIgnoreCase("op")
                || cmd.equalsIgnoreCase("deop") || cmd.equalsIgnoreCase("ban") || cmd.equalsIgnoreCase("ban-ip")
                || cmd.equalsIgnoreCase("pardon") || cmd.equalsIgnoreCase("pardon-ip") || cmd.equalsIgnoreCase("reload")) {
            return;
        }

        // Handle vanilla commands;
        if (sender.getLevel().getCraftServer().getCommandBlockOverride(args[0])) {
            args[0] = "minecraft:" + args[0];
        }

        String newCommand = joiner.join(args);
        this.performPrefixedCommand(sender, newCommand, newCommand);
    }
    
    @Unique
    private static String label;
    
    @Unique
    @Override
    public void performPrefixedCommand(CommandSourceStack commandlistenerwrapper, String s, String label) {
        this.performCommand(this.dispatcher.parse(s, commandlistenerwrapper), s, label);
    }

    @Inject(method = "performPrefixedCommand", at = @At("HEAD"))
    private void ethylene$performPrefixedCommand(CommandSourceStack commandSourceStack, String string, CallbackInfo ci) {
        CommandsMixin.label = string;
    }

    @Unique
    @Override
    public void performCommand(ParseResults<CommandSourceStack> parseresults, String s, String label) {
        CommandsMixin.label = label;
        this.performCommand(parseresults, s);
    }
    
    @Inject(method = "performCommand", at = @At("HEAD"))
    private void ethylene$performCommand(ParseResults<CommandSourceStack> parseResults, String string, CallbackInfo ci) {
        CommandsMixin.label = string;
    }
    
    @Unique
    private static ContextChain<CommandSourceStack> finishParsing(ParseResults<CommandSourceStack> parseresults, String s, CommandSourceStack commandlistenerwrapper, String label) {
        CommandsMixin.label = label;
        return Commands.finishParsing(parseresults, s, commandlistenerwrapper);
    }

    @Inject(method = "finishParsing", at = @At("HEAD"))
    private static void ethylene$finishParsing(ParseResults<CommandSourceStack> parseResults, String string, CommandSourceStack commandSourceStack, CallbackInfoReturnable<ContextChain<CommandSourceStack>> cir) {
        CommandsMixin.label = string;
    }
    
    @ModifyArg(method = "method_54493", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/ClickEvent;<init>(Lnet/minecraft/network/chat/ClickEvent$Action;Ljava/lang/String;)V"), index = 1)
    private static String ethylene$method_54493(String string) {
        return CommandsMixin.label;
    }

    /**
     * @author TonimatasDEV
     * @reason Why don't overwrite it?
     */
    @Overwrite
    public void sendCommands(ServerPlayer serverPlayer) {
        // CraftBukkit start
        // Register Vanilla commands into builtRoot as before
        Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> map = Maps.newIdentityHashMap(); // Use identity to prevent aliasing issues
        RootCommandNode vanillaRoot = new RootCommandNode();

        RootCommandNode<CommandSourceStack> vanilla = serverPlayer.server.vanillaCommandDispatcher.getDispatcher().getRoot();
        map.put(vanilla, vanillaRoot);
        this.fillUsableCommands(vanilla, vanillaRoot, serverPlayer.createCommandSourceStack(), (Map) map);

        // Now build the global commands in a second pass
        RootCommandNode<SharedSuggestionProvider> rootcommandnode = new RootCommandNode();

        map.put(this.dispatcher.getRoot(), rootcommandnode);
        this.fillUsableCommands(this.dispatcher.getRoot(), rootcommandnode, serverPlayer.createCommandSourceStack(), map);
        
        Collection<String> bukkit = new LinkedHashSet<>();
        for (CommandNode node : rootcommandnode.getChildren()) {
            bukkit.add(node.getName());
        }

        PlayerCommandSendEvent event = new PlayerCommandSendEvent(serverPlayer.getBukkitEntity(), new LinkedHashSet<>(bukkit));
        event.getPlayer().getServer().getPluginManager().callEvent(event);

        // Remove labels that were removed during the event
        for (String orig : bukkit) {
            if (!event.getCommands().contains(orig)) {
                rootcommandnode.removeCommand(orig);
            }
        }
        // CraftBukkit end
        serverPlayer.connection.send(new ClientboundCommandsPacket(rootcommandnode));
    }
}
