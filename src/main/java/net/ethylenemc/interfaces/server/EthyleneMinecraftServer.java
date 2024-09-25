package net.ethylenemc.interfaces.server;

public interface EthyleneMinecraftServer {
    java.util.Queue<Runnable> getProcessQueue();

    boolean isDebugging();
}
