package org.bukkit.craftbukkit.entity;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Color;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.potion.CraftPotionType;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.entity.Arrow;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class CraftArrow extends CraftAbstractArrow implements Arrow {

    public CraftArrow(CraftServer server, net.minecraft.world.entity.projectile.Arrow entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.projectile.Arrow getHandle() {
        return (net.minecraft.world.entity.projectile.Arrow) entity;
    }

    @Override
    public String toString() {
        return "CraftTippedArrow";
    }

    @Override
    public boolean addCustomEffect(PotionEffect effect, boolean override) {
        if (hasCustomEffect(effect.getType())) {
            if (!override) {
                return false;
            }
            removeCustomEffect(effect.getType());
        }
        getHandle().addEffect(CraftPotionUtil.fromBukkit(effect));
        getHandle().updateColor();
        return true;
    }

    @Override
    public void clearCustomEffects() {
        net.minecraft.world.item.alchemy.PotionContents old = getHandle().getPotionContents();
        getHandle().setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(old.potion(), old.customColor(), List.of()));
        getHandle().updateColor();
    }

    @Override
    public List<PotionEffect> getCustomEffects() {
        ImmutableList.Builder<PotionEffect> builder = ImmutableList.builder();
        for (net.minecraft.world.effect.MobEffectInstance effect : getHandle().getPotionContents().customEffects()) {
            builder.add(CraftPotionUtil.toBukkit(effect));
        }
        return builder.build();
    }

    @Override
    public boolean hasCustomEffect(PotionEffectType type) {
        for (net.minecraft.world.effect.MobEffectInstance effect : getHandle().getPotionContents().customEffects()) {
            if (CraftPotionUtil.equals(effect.getEffect(), type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasCustomEffects() {
        return !getHandle().getPotionContents().customEffects().isEmpty();
    }

    @Override
    public boolean removeCustomEffect(PotionEffectType effect) {
        if (!hasCustomEffect(effect)) {
            return false;
        }
        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> minecraft = CraftPotionEffectType.bukkitToMinecraftHolder(effect);

        net.minecraft.world.item.alchemy.PotionContents old = getHandle().getPotionContents();
        getHandle().setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(old.potion(), old.customColor(), old.customEffects().stream().filter((mobEffect) -> !mobEffect.getEffect().equals(minecraft)).toList()));
        return true;
    }

    @Override
    public void setBasePotionData(PotionData data) {
        setBasePotionType(CraftPotionUtil.fromBukkit(data));
    }

    @Override
    public PotionData getBasePotionData() {
        return CraftPotionUtil.toBukkit(getBasePotionType());
    }

    @Override
    public void setBasePotionType(PotionType potionType) {
        if (potionType != null) {
            getHandle().setPotionContents(getHandle().getPotionContents().withPotion(CraftPotionType.bukkitToMinecraftHolder(potionType)));
        } else {
            net.minecraft.world.item.alchemy.PotionContents old = getHandle().getPotionContents();
            getHandle().setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(Optional.empty(), old.customColor(), old.customEffects()));
        }
    }

    @Override
    public PotionType getBasePotionType() {
        return getHandle().getPotionContents().potion().map(CraftPotionType::minecraftHolderToBukkit).orElse(null);
    }

    @Override
    public void setColor(Color color) {
        int colorRGB = (color == null) ? -1 : color.asRGB();
        net.minecraft.world.item.alchemy.PotionContents old = getHandle().getPotionContents();
        getHandle().setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(old.potion(), Optional.of(colorRGB), old.customEffects()));
    }

    @Override
    public Color getColor() {
        if (getHandle().getColor() <= -1) {
            return null;
        }
        return Color.fromRGB(getHandle().getColor());
    }
}
