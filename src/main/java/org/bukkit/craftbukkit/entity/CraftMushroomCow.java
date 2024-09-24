package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.entity.MushroomCow;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CraftMushroomCow extends CraftCow implements MushroomCow {
    public CraftMushroomCow(CraftServer server, net.minecraft.world.entity.animal.MushroomCow entity) {
        super(server, entity);
    }

    @Override
    public boolean hasEffectsForNextStew() {
        net.minecraft.world.item.component.SuspiciousStewEffects stewEffects = this.getHandle().stewEffects;
        return stewEffects != null && !stewEffects.effects().isEmpty();
    }

    @Override
    public List<PotionEffect> getEffectsForNextStew() {
        net.minecraft.world.item.component.SuspiciousStewEffects stewEffects = this.getHandle().stewEffects;
        if (stewEffects != null) {
            return stewEffects.effects().stream().map(recordSuspiciousEffect -> CraftPotionUtil.toBukkit(recordSuspiciousEffect.createEffectInstance())).toList();
        }
        return ImmutableList.of();
    }

    @Override
    public boolean addEffectToNextStew(PotionEffect potionEffect, boolean overwrite) {
        Preconditions.checkArgument(potionEffect != null, "PotionEffect cannot be null");
        net.minecraft.world.effect.MobEffectInstance minecraftPotionEffect = CraftPotionUtil.fromBukkit(potionEffect);
        if (!overwrite && this.hasEffectForNextStew(potionEffect.getType())) {
            return false;
        }
        net.minecraft.world.item.component.SuspiciousStewEffects stewEffects = this.getHandle().stewEffects;
        if (stewEffects == null) {
            stewEffects = net.minecraft.world.item.component.SuspiciousStewEffects.EMPTY;
        }
        net.minecraft.world.item.component.SuspiciousStewEffects.Entry recordSuspiciousEffect = new net.minecraft.world.item.component.SuspiciousStewEffects.Entry(minecraftPotionEffect.getEffect(), minecraftPotionEffect.getDuration());
        this.removeEffectFromNextStew(potionEffect.getType()); // Avoid duplicates of effects
        getHandle().stewEffects = stewEffects.withEffectAdded(recordSuspiciousEffect);
        return true;
    }

    @Override
    public boolean removeEffectFromNextStew(PotionEffectType potionEffectType) {
        Preconditions.checkArgument(potionEffectType != null, "potionEffectType cannot be null");
        if (!hasEffectForNextStew(potionEffectType)) {
            return false;
        }

        net.minecraft.world.item.component.SuspiciousStewEffects stewEffects = this.getHandle().stewEffects;
        if (stewEffects == null) {
            return false;
        }

        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> minecraftPotionEffectType = CraftPotionEffectType.bukkitToMinecraftHolder(potionEffectType);
        getHandle().stewEffects = new net.minecraft.world.item.component.SuspiciousStewEffects(stewEffects.effects().stream().filter((effect) -> !effect.effect().equals(minecraftPotionEffectType)).toList());
        return true;
    }

    @Override
    public boolean hasEffectForNextStew(PotionEffectType potionEffectType) {
        Preconditions.checkArgument(potionEffectType != null, "potionEffectType cannot be null");
        net.minecraft.world.item.component.SuspiciousStewEffects stewEffects = this.getHandle().stewEffects;
        if (stewEffects == null) {
            return false;
        }
        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> minecraftPotionEffectType = CraftPotionEffectType.bukkitToMinecraftHolder(potionEffectType);
        return stewEffects.effects().stream().anyMatch(recordSuspiciousEffect -> recordSuspiciousEffect.effect().equals(minecraftPotionEffectType));
    }

    @Override
    public void clearEffectsForNextStew() {
        this.getHandle().stewEffects = null;
    }

    @Override
    public net.minecraft.world.entity.animal.MushroomCow getHandle() {
        return (net.minecraft.world.entity.animal.MushroomCow) entity;
    }

    @Override
    public Variant getVariant() {
        return Variant.values()[getHandle().getVariant().ordinal()];
    }

    @Override
    public void setVariant(Variant variant) {
        Preconditions.checkArgument(variant != null, "Variant cannot be null");

        getHandle().setVariant(net.minecraft.world.entity.animal.MushroomCow.MushroomType.values()[variant.ordinal()]);
    }

    @Override
    public String toString() {
        return "CraftMushroomCow";
    }
}
