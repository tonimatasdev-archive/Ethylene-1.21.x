package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import org.bukkit.JukeboxSong;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.util.Handleable;
import org.jetbrains.annotations.NotNull;

public class CraftJukeboxSong implements JukeboxSong, Handleable<net.minecraft.world.item.JukeboxSong> {

    public static JukeboxSong minecraftToBukkit(net.minecraft.world.item.JukeboxSong minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.JUKEBOX_SONG, Registry.JUKEBOX_SONG);
    }

    public static JukeboxSong minecraftHolderToBukkit(net.minecraft.core.Holder<net.minecraft.world.item.JukeboxSong> minecraft) {
        return minecraftToBukkit(minecraft.value());
    }

    public static net.minecraft.world.item.JukeboxSong bukkitToMinecraft(JukeboxSong bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static net.minecraft.core.Holder<net.minecraft.world.item.JukeboxSong> bukkitToMinecraftHolder(JukeboxSong bukkit) {
        Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<net.minecraft.world.item.JukeboxSong> registry = CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.JUKEBOX_SONG);

        if (registry.wrapAsHolder(bukkitToMinecraft(bukkit)) instanceof net.minecraft.core.Holder.Reference<net.minecraft.world.item.JukeboxSong> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own trim pattern without properly registering it.");
    }

    private final NamespacedKey key;
    private final net.minecraft.world.item.JukeboxSong handle;

    public CraftJukeboxSong(NamespacedKey key, net.minecraft.world.item.JukeboxSong handle) {
        this.key = key;
        this.handle = handle;
    }

    @Override
    public net.minecraft.world.item.JukeboxSong getHandle() {
        return handle;
    }

    @Override
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return ((net.minecraft.network.chat.contents.TranslatableContents) handle.description().getContents()).getKey();
    }
}
