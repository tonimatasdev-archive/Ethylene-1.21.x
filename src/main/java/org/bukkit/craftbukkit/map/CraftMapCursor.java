package org.bukkit.craftbukkit.map;

import java.util.Locale;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.map.MapCursor;

public final class CraftMapCursor {

    public static final class CraftType implements MapCursor.Type, Handleable<net.minecraft.world.level.saveddata.maps.MapDecorationType> {

        private static int count = 0;

        public static MapCursor.Type minecraftToBukkit(net.minecraft.world.level.saveddata.maps.MapDecorationType minecraft) {
            return CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.MAP_DECORATION_TYPE, Registry.MAP_DECORATION_TYPE);
        }

        public static MapCursor.Type minecraftHolderToBukkit(net.minecraft.core.Holder<net.minecraft.world.level.saveddata.maps.MapDecorationType> minecraft) {
            return minecraftToBukkit(minecraft.value());
        }

        public static net.minecraft.world.level.saveddata.maps.MapDecorationType bukkitToMinecraft(MapCursor.Type bukkit) {
            return CraftRegistry.bukkitToMinecraft(bukkit);
        }

        public static net.minecraft.core.Holder<net.minecraft.world.level.saveddata.maps.MapDecorationType> bukkitToMinecraftHolder(MapCursor.Type bukkit) {
            return CraftRegistry.bukkitToMinecraftHolder(bukkit, net.minecraft.core.registries.Registries.MAP_DECORATION_TYPE);
        }

        private final NamespacedKey key;
        private final net.minecraft.world.level.saveddata.maps.MapDecorationType mapDecorationType;
        private final String name;
        private final int ordinal;

        public CraftType(NamespacedKey key, net.minecraft.world.level.saveddata.maps.MapDecorationType mapDecorationType) {
            this.key = key;
            this.mapDecorationType = mapDecorationType;
            // For backwards compatibility, minecraft values will still return the uppercase name without the namespace,
            // in case plugins use for example the name as key in a config file to receive type specific values.
            // Custom types will return the key with namespace. For a plugin this should look than like a new type
            // (which can always be added in new minecraft versions and the plugin should therefore handle it accordingly).
            if (NamespacedKey.MINECRAFT.equals(key.getNamespace())) {
                this.name = key.getKey().toUpperCase(Locale.ROOT);
            } else {
                this.name = key.toString();
            }
            this.ordinal = count++;
        }

        @Override
        public net.minecraft.world.level.saveddata.maps.MapDecorationType getHandle() {
            return mapDecorationType;
        }

        @Override
        public NamespacedKey getKey() {
            return key;
        }

        @Override
        public int compareTo(MapCursor.Type type) {
            return ordinal - type.ordinal();
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int ordinal() {
            return ordinal;
        }

        @Override
        public String toString() {
            // For backwards compatibility
            return name();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof CraftType)) {
                return false;
            }

            return getKey().equals(((MapCursor.Type) other).getKey());
        }

        @Override
        public int hashCode() {
            return getKey().hashCode();
        }

        @Override
        public byte getValue() {
            return (byte) CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.MAP_DECORATION_TYPE).getId(getHandle());
        }
    }
}
