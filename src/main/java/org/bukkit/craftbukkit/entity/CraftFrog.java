package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import java.util.Locale;

import net.ethylenemc.interfaces.world.entity.EthyleneEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.entity.Entity;

public class CraftFrog extends CraftAnimals implements org.bukkit.entity.Frog {

    public CraftFrog(CraftServer server, net.minecraft.world.entity.animal.frog.Frog entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.frog.Frog getHandle() {
        return (net.minecraft.world.entity.animal.frog.Frog) entity;
    }

    @Override
    public String toString() {
        return "CraftFrog";
    }

    @Override
    public Entity getTongueTarget() {
        return getHandle().getTongueTarget().map(entity1 -> ((EthyleneEntity) entity1).getBukkitEntity()).orElse(null);
    }

    @Override
    public void setTongueTarget(Entity target) {
        if (target == null) {
            getHandle().eraseTongueTarget();
        } else {
            getHandle().setTongueTarget(((CraftEntity) target).getHandle());
        }
    }

    @Override
    public Variant getVariant() {
        return CraftVariant.minecraftHolderToBukkit(getHandle().getVariant());
    }

    @Override
    public void setVariant(Variant variant) {
        Preconditions.checkArgument(variant != null, "variant");

        getHandle().setVariant(CraftVariant.bukkitToMinecraftHolder(variant));
    }

    public static class CraftVariant implements Variant, Handleable<net.minecraft.world.entity.animal.FrogVariant> {
        private static int count = 0;

        public static Variant minecraftToBukkit(net.minecraft.world.entity.animal.FrogVariant minecraft) {
            return CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.FROG_VARIANT, Registry.FROG_VARIANT);
        }

        public static Variant minecraftHolderToBukkit(net.minecraft.core.Holder<net.minecraft.world.entity.animal.FrogVariant> minecraft) {
            return minecraftToBukkit(minecraft.value());
        }

        public static net.minecraft.world.entity.animal.FrogVariant bukkitToMinecraft(Variant bukkit) {
            return CraftRegistry.bukkitToMinecraft(bukkit);
        }

        public static net.minecraft.core.Holder<net.minecraft.world.entity.animal.FrogVariant> bukkitToMinecraftHolder(Variant bukkit) {
            return CraftRegistry.bukkitToMinecraftHolder(bukkit, net.minecraft.core.registries.Registries.FROG_VARIANT);
        }

        private final NamespacedKey key;
        private final net.minecraft.world.entity.animal.FrogVariant frogVariant;
        private final String name;
        private final int ordinal;

        public CraftVariant(NamespacedKey key, net.minecraft.world.entity.animal.FrogVariant frogVariant) {
            this.key = key;
            this.frogVariant = frogVariant;
            // For backwards compatibility, minecraft values will still return the uppercase name without the namespace,
            // in case plugins use for example the name as key in a config file to receive variant specific values.
            // Custom variants will return the key with namespace. For a plugin this should look than like a new variant
            // (which can always be added in new minecraft versions and the plugin should therefore handle it accordingly).
            if (NamespacedKey.MINECRAFT.equals(key.getNamespace())) {
                this.name = key.getKey().toUpperCase(Locale.ROOT);
            } else {
                this.name = key.toString();
            }
            this.ordinal = count++;
        }

        @Override
        public net.minecraft.world.entity.animal.FrogVariant getHandle() {
            return frogVariant;
        }

        @Override
        public NamespacedKey getKey() {
            return key;
        }

        @Override
        public int compareTo(Variant variant) {
            return ordinal - variant.ordinal();
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

            if (!(other instanceof CraftVariant)) {
                return false;
            }

            return getKey().equals(((Variant) other).getKey());
        }

        @Override
        public int hashCode() {
            return getKey().hashCode();
        }
    }
}
