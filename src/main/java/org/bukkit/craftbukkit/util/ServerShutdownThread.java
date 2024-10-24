package org.bukkit.craftbukkit.util;


import net.ethylenemc.interfaces.server.EthyleneMinecraftServer;

public class ServerShutdownThread extends Thread {
    private final net.minecraft.server.MinecraftServer server;

    public ServerShutdownThread(net.minecraft.server.MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            server.close();
        } finally {
            try {
                ((EthyleneMinecraftServer) server).getReader().getTerminal().restore();
            } catch (Exception e) {
            }
        }
    }
}
