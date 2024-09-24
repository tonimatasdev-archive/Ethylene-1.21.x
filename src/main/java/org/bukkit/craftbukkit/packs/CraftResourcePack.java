package org.bukkit.craftbukkit.packs;

import java.util.UUID;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.packs.ResourcePack;

public class CraftResourcePack implements ResourcePack {

    private final net.minecraft.server.MinecraftServer.ServerResourcePackInfo handle;

    public CraftResourcePack(net.minecraft.server.MinecraftServer.ServerResourcePackInfo handle) {
        this.handle = handle;
    }

    @Override
    public UUID getId() {
        return this.handle.id();
    }

    @Override
    public String getUrl() {
        return this.handle.url();
    }

    @Override
    public String getHash() {
        return this.handle.hash();
    }

    @Override
    public String getPrompt() {
        return (this.handle.prompt() == null) ? "" : CraftChatMessage.fromComponent(this.handle.prompt());
    }

    @Override
    public boolean isRequired() {
        return this.handle.isRequired();
    }

    @Override
    public String toString() {
        return "CraftResourcePack{id=" + this.getId() + ",url=" + this.getUrl() + ",hash=" + this.getHash() + ",prompt=" + this.getPrompt() + ",required=" + this.isRequired() + "}";
    }
}
