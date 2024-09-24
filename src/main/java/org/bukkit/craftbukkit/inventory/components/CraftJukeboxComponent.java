package org.bukkit.craftbukkit.inventory.components;

import com.google.common.base.Preconditions;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.JukeboxSong;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.CraftJukeboxSong;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.meta.components.JukeboxPlayableComponent;

@SerializableAs("net.minecraft.world.item.JukeboxPlayable")
public final class CraftJukeboxComponent implements JukeboxPlayableComponent {

    private net.minecraft.world.item.JukeboxPlayable handle;

    public CraftJukeboxComponent(net.minecraft.world.item.JukeboxPlayable jukebox) {
        this.handle = jukebox;
    }

    public CraftJukeboxComponent(CraftJukeboxComponent jukebox) {
        this.handle = jukebox.handle;
    }

    public CraftJukeboxComponent(Map<String, Object> map) {
        String song = SerializableMeta.getObject(String.class, map, "song", false);
        Boolean showTooltip = SerializableMeta.getObject(Boolean.class, map, "show-in-tooltip", true);

        this.handle = new net.minecraft.world.item.JukeboxPlayable(new net.minecraft.world.item.EitherHolder<>(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.JUKEBOX_SONG, net.minecraft.resources.ResourceLocation.parse(song))), (showTooltip != null) ? showTooltip : true);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("song", getSongKey().toString());
        result.put("show-in-tooltip", isShowInTooltip());
        return result;
    }

    public net.minecraft.world.item.JukeboxPlayable getHandle() {
        return handle;
    }

    @Override
    public JukeboxSong getSong() {
        Optional<net.minecraft.core.Holder<net.minecraft.world.item.JukeboxSong>> song = handle.song().unwrap(CraftRegistry.getMinecraftRegistry());
        return (song.isPresent()) ? CraftJukeboxSong.minecraftHolderToBukkit(song.get()) : null;
    }

    @Override
    public NamespacedKey getSongKey() {
        return CraftNamespacedKey.fromMinecraft(handle.song().key().location());
    }

    @Override
    public void setSong(JukeboxSong song) {
        Preconditions.checkArgument(song != null, "song cannot be null");

        handle = new net.minecraft.world.item.JukeboxPlayable(new net.minecraft.world.item.EitherHolder<>(CraftJukeboxSong.bukkitToMinecraftHolder(song)), handle.showInTooltip());
    }

    @Override
    public void setSongKey(NamespacedKey song) {
        Preconditions.checkArgument(song != null, "song cannot be null");

        handle = new net.minecraft.world.item.JukeboxPlayable(new net.minecraft.world.item.EitherHolder<>(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.JUKEBOX_SONG, CraftNamespacedKey.toMinecraft(song))), handle.showInTooltip());
    }

    @Override
    public boolean isShowInTooltip() {
        return handle.showInTooltip();
    }

    @Override
    public void setShowInTooltip(boolean show) {
        handle = new net.minecraft.world.item.JukeboxPlayable(handle.song(), show);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.handle);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CraftJukeboxComponent other = (CraftJukeboxComponent) obj;
        return Objects.equals(this.handle, other.handle);
    }

    @Override
    public String toString() {
        return "CraftJukeboxComponent{" + "handle=" + handle + '}';
    }
}
