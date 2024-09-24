package org.bukkit.craftbukkit.command;

import java.net.SocketAddress;
import org.bukkit.command.RemoteConsoleCommandSender;

public class CraftRemoteConsoleCommandSender extends ServerCommandSender implements RemoteConsoleCommandSender {

    private final net.minecraft.server.rcon.RconConsoleSource listener;

    public CraftRemoteConsoleCommandSender(net.minecraft.server.rcon.RconConsoleSource listener) {
        this.listener = listener;
    }

    public net.minecraft.server.rcon.RconConsoleSource getListener() {
        return listener;
    }

    @Override
    public SocketAddress getAddress() {
       return listener.socketAddress;
    }

    @Override
    public void sendMessage(String message) {
        listener.sendSystemMessage(net.minecraft.network.chat.Component.literal(message + "\n")); // Send a newline after each message, to preserve formatting.
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public String getName() {
        return "Rcon";
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
        throw new UnsupportedOperationException("Cannot change operator status of remote controller.");
    }
}
