package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.entity.Wolf;

public class CraftWolf extends CraftTameableAnimal implements Wolf {
    public CraftWolf(CraftServer server, net.minecraft.world.entity.animal.Wolf wolf) {
        super(server, wolf);
    }

    @Override
    public boolean isAngry() {
        return getHandle().isAngry();
    }

    @Override
    public void setAngry(boolean angry) {
        if (angry) {
            getHandle().startPersistentAngerTimer();
        } else {
            getHandle().stopBeingAngry();
        }
    }

    @Override
    public net.minecraft.world.entity.animal.Wolf getHandle() {
        return (net.minecraft.world.entity.animal.Wolf) entity;
    }

    @Override
    public DyeColor getCollarColor() {
        return DyeColor.getByWoolData((byte) getHandle().getCollarColor().getId());
    }

    @Override
    public void setCollarColor(DyeColor color) {
        getHandle().setCollarColor(net.minecraft.world.item.DyeColor.byId(color.getWoolData()));
    }

    @Override
    public boolean isWet() {
        return getHandle().isWet();
    }

    @Override
    public float getTailAngle() {
        return getHandle().getTailAngle();
    }

    @Override
    public boolean isInterested() {
        return getHandle().isInterested();
    }

    @Override
    public void setInterested(boolean flag) {
        getHandle().setIsInterested(flag);
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

    public static class CraftVariant implements Variant, Handleable<net.minecraft.world.entity.animal.WolfVariant> {

        public static Variant minecraftToBukkit(net.minecraft.world.entity.animal.WolfVariant minecraft) {
            return CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.WOLF_VARIANT, Registry.WOLF_VARIANT);
        }

        public static Variant minecraftHolderToBukkit(net.minecraft.core.Holder<net.minecraft.world.entity.animal.WolfVariant> minecraft) {
            return minecraftToBukkit(minecraft.value());
        }

        public static net.minecraft.world.entity.animal.WolfVariant bukkitToMinecraft(Variant bukkit) {
            return CraftRegistry.bukkitToMinecraft(bukkit);
        }

        public static net.minecraft.core.Holder<net.minecraft.world.entity.animal.WolfVariant> bukkitToMinecraftHolder(Variant bukkit) {
            Preconditions.checkArgument(bukkit != null);

            net.minecraft.core.Registry<net.minecraft.world.entity.animal.WolfVariant> registry = CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.WOLF_VARIANT);

            if (registry.wrapAsHolder(bukkitToMinecraft(bukkit)) instanceof net.minecraft.core.Holder.Reference<net.minecraft.world.entity.animal.WolfVariant> holder) {
                return holder;
            }

            throw new IllegalArgumentException("No Reference holder found for " + bukkit
                    + ", this can happen if a plugin creates its own wolf variant with out properly registering it.");
        }

        private final NamespacedKey key;
        private final net.minecraft.world.entity.animal.WolfVariant variant;

        public CraftVariant(NamespacedKey key, net.minecraft.world.entity.animal.WolfVariant variant) {
            this.key = key;
            this.variant = variant;
        }

        @Override
        public net.minecraft.world.entity.animal.WolfVariant getHandle() {
            return variant;
        }

        @Override
        public NamespacedKey getKey() {
            return key;
        }

        @Override
        public String toString() {
            return key.toString();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof CraftVariant otherVariant)) {
                return false;
            }

            return getKey().equals(otherVariant.getKey());
        }

        @Override
        public int hashCode() {
            return getKey().hashCode();
        }
    }
}
