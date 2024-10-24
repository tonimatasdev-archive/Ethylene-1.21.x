package net.ethylenemc.interfaces.server;

import jline.console.ConsoleReader;
import joptsimple.OptionSet;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftServer;

public interface EthyleneMinecraftServer {
    java.util.Queue<Runnable> getProcessQueue();

    boolean isDebugging();

    ConsoleReader getReader();

    CraftServer getServer();
    
    OptionSet getOptions();

    ConsoleCommandSender getConsole();
}
