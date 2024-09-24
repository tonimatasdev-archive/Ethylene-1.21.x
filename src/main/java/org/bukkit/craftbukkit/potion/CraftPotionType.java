package org.bukkit.craftbukkit.potion;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.legacy.FieldRename;
import org.bukkit.craftbukkit.util.ApiVersion;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class CraftPotionType implements PotionType.InternalPotionData {

    public static PotionType minecraftHolderToBukkit(net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> minecraft) {
        return minecraftToBukkit(minecraft.value());
    }

    public static PotionType minecraftToBukkit(net.minecraft.world.item.alchemy.Potion minecraft) {
        Preconditions.checkArgument(minecraft != null);

        net.minecraft.core.Registry<net.minecraft.world.item.alchemy.Potion> registry = CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.POTION);
        PotionType bukkit = Registry.POTION.get(CraftNamespacedKey.fromMinecraft(registry.getResourceKey(minecraft).orElseThrow().location()));

        Preconditions.checkArgument(bukkit != null);

        return bukkit;
    }

    public static net.minecraft.world.item.alchemy.Potion bukkitToMinecraft(PotionType bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.POTION)
                .getOptional(CraftNamespacedKey.toMinecraft(bukkit.getKey())).orElseThrow();
    }

    public static net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> bukkitToMinecraftHolder(PotionType bukkit) {
        Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<net.minecraft.world.item.alchemy.Potion> registry = CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.POTION);

        if (registry.wrapAsHolder(bukkitToMinecraft(bukkit)) instanceof net.minecraft.core.Holder.Reference<net.minecraft.world.item.alchemy.Potion> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own sound effect with out properly registering it.");
    }

    public static String bukkitToString(PotionType bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return bukkit.getKey().toString();
    }

    public static PotionType stringToBukkit(String string) {
        Preconditions.checkArgument(string != null);

        // We currently do not have any version-dependent remapping, so we can use current version
        // First convert from when only the names where saved
        string = FieldRename.convertPotionTypeName(ApiVersion.CURRENT, string);
        string = string.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(string);

        // Now also convert from when keys where saved
        return CraftRegistry.get(Registry.POTION, key, ApiVersion.CURRENT);
    }

    private final NamespacedKey key;
    private final net.minecraft.world.item.alchemy.Potion potion;
    private final Supplier<List<PotionEffect>> potionEffects;
    private final Supplier<Boolean> upgradeable;
    private final Supplier<Boolean> extendable;
    private final Supplier<Integer> maxLevel;

    public CraftPotionType(NamespacedKey key, net.minecraft.world.item.alchemy.Potion potion) {
        this.key = key;
        this.potion = potion;
        this.potionEffects = Suppliers.memoize(() -> potion.getEffects().stream().map(CraftPotionUtil::toBukkit).toList());
        this.upgradeable = Suppliers.memoize(() -> Registry.POTION.get(new NamespacedKey(key.getNamespace(), "strong_" + key.getKey())) != null);
        this.extendable = Suppliers.memoize(() -> Registry.POTION.get(new NamespacedKey(key.getNamespace(), "long_" + key.getKey())) != null);
        this.maxLevel = Suppliers.memoize(() -> isUpgradeable() ? 2 : 1);
    }

    @Override
    public PotionEffectType getEffectType() {
        return getPotionEffects().isEmpty() ? null : getPotionEffects().get(0).getType();
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        return potionEffects.get();
    }

    @Override
    public boolean isInstant() {
        return potion.hasInstantEffects();
    }

    @Override
    public boolean isUpgradeable() {
        return upgradeable.get();
    }

    @Override
    public boolean isExtendable() {
        return extendable.get();
    }

    @Override
    public int getMaxLevel() {
        return maxLevel.get();
    }
}
