package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.util.Handleable;
import org.jetbrains.annotations.NotNull;

public class CraftMusicInstrument extends MusicInstrument implements Handleable<net.minecraft.world.item.Instrument> {

    public static MusicInstrument minecraftToBukkit(net.minecraft.world.item.Instrument minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.INSTRUMENT, Registry.INSTRUMENT);
    }

    public static MusicInstrument minecraftHolderToBukkit(net.minecraft.core.Holder<net.minecraft.world.item.Instrument> minecraft) {
        return minecraftToBukkit(minecraft.value());
    }

    public static net.minecraft.world.item.Instrument bukkitToMinecraft(MusicInstrument bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static net.minecraft.core.Holder<net.minecraft.world.item.Instrument> bukkitToMinecraftHolder(MusicInstrument bukkit) {
        Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<net.minecraft.world.item.Instrument> registry = CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.INSTRUMENT);

        if (registry.wrapAsHolder(bukkitToMinecraft(bukkit)) instanceof net.minecraft.core.Holder.Reference<net.minecraft.world.item.Instrument> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own instrument without properly registering it.");
    }

    public static String bukkitToString(MusicInstrument bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return bukkit.getKey().toString();
    }

    public static MusicInstrument stringToBukkit(String string) {
        Preconditions.checkArgument(string != null);

        return Registry.INSTRUMENT.get(NamespacedKey.fromString(string));
    }

    private final NamespacedKey key;
    private final net.minecraft.world.item.Instrument handle;

    public CraftMusicInstrument(NamespacedKey key, net.minecraft.world.item.Instrument handle) {
        this.key = key;
        this.handle = handle;
    }

    @Override
    public net.minecraft.world.item.Instrument getHandle() {
        return handle;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof CraftMusicInstrument)) {
            return false;
        }

        return getKey().equals(((MusicInstrument) other).getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public String toString() {
        return "CraftMusicInstrument{key=" + key + "}";
    }
}
