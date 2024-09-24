package org.bukkit.craftbukkit.entity;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.potion.CraftPotionType;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;

public class CraftAreaEffectCloud extends CraftEntity implements AreaEffectCloud {

    public CraftAreaEffectCloud(CraftServer server, net.minecraft.world.entity.AreaEffectCloud entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.AreaEffectCloud getHandle() {
        return (net.minecraft.world.entity.AreaEffectCloud) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftAreaEffectCloud";
    }

    @Override
    public int getDuration() {
        return getHandle().getDuration();
    }

    @Override
    public void setDuration(int duration) {
        getHandle().setDuration(duration);
    }

    @Override
    public int getWaitTime() {
        return getHandle().waitTime;
    }

    @Override
    public void setWaitTime(int waitTime) {
        getHandle().setWaitTime(waitTime);
    }

    @Override
    public int getReapplicationDelay() {
        return getHandle().reapplicationDelay;
    }

    @Override
    public void setReapplicationDelay(int delay) {
        getHandle().reapplicationDelay = delay;
    }

    @Override
    public int getDurationOnUse() {
        return getHandle().durationOnUse;
    }

    @Override
    public void setDurationOnUse(int duration) {
        getHandle().durationOnUse = duration;
    }

    @Override
    public float getRadius() {
        return getHandle().getRadius();
    }

    @Override
    public void setRadius(float radius) {
        getHandle().setRadius(radius);
    }

    @Override
    public float getRadiusOnUse() {
        return getHandle().radiusOnUse;
    }

    @Override
    public void setRadiusOnUse(float radius) {
        getHandle().setRadiusOnUse(radius);
    }

    @Override
    public float getRadiusPerTick() {
        return getHandle().radiusPerTick;
    }

    @Override
    public void setRadiusPerTick(float radius) {
        getHandle().setRadiusPerTick(radius);
    }

    @Override
    public Particle getParticle() {
        return CraftParticle.minecraftToBukkit(getHandle().getParticle().getType());
    }

    @Override
    public void setParticle(Particle particle) {
        setParticle(particle, null);
    }

    @Override
    public <T> void setParticle(Particle particle, T data) {
        getHandle().setParticle(CraftParticle.createParticleParam(particle, data));
    }

    @Override
    public Color getColor() {
        return Color.fromRGB(getHandle().potionContents.getColor());
    }

    @Override
    public void setColor(Color color) {
        net.minecraft.world.item.alchemy.PotionContents old = getHandle().potionContents;
        getHandle().setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(old.potion(), Optional.of(color.asRGB()), old.customEffects()));
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
        net.minecraft.world.item.alchemy.PotionContents old = getHandle().potionContents;
        getHandle().setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(old.potion(), old.customColor(), List.of()));
        getHandle().updateColor();
    }

    @Override
    public List<PotionEffect> getCustomEffects() {
        ImmutableList.Builder<PotionEffect> builder = ImmutableList.builder();
        for (net.minecraft.world.effect.MobEffectInstance effect : getHandle().potionContents.customEffects()) {
            builder.add(CraftPotionUtil.toBukkit(effect));
        }
        return builder.build();
    }

    @Override
    public boolean hasCustomEffect(PotionEffectType type) {
        for (net.minecraft.world.effect.MobEffectInstance effect : getHandle().potionContents.customEffects()) {
            if (CraftPotionUtil.equals(effect.getEffect(), type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasCustomEffects() {
        return !getHandle().potionContents.customEffects().isEmpty();
    }

    @Override
    public boolean removeCustomEffect(PotionEffectType effect) {
        if (!hasCustomEffect(effect)) {
            return false;
        }
        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> minecraft = CraftPotionEffectType.bukkitToMinecraftHolder(effect);

        net.minecraft.world.item.alchemy.PotionContents old = getHandle().potionContents;
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
            getHandle().setPotionContents(getHandle().potionContents.withPotion(CraftPotionType.bukkitToMinecraftHolder(potionType)));
        } else {
            net.minecraft.world.item.alchemy.PotionContents old = getHandle().potionContents;
            getHandle().setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(Optional.empty(), old.customColor(), old.customEffects()));
        }
    }

    @Override
    public PotionType getBasePotionType() {
        return getHandle().potionContents.potion().map(CraftPotionType::minecraftHolderToBukkit).orElse(null);
    }

    @Override
    public ProjectileSource getSource() {
        net.minecraft.world.entity.LivingEntity source = getHandle().getOwner();
        return (source == null) ? null : (LivingEntity) source.getBukkitEntity();
    }

    @Override
    public void setSource(ProjectileSource shooter) {
        if (shooter instanceof CraftLivingEntity craftLivingEntity) {
            getHandle().setOwner(craftLivingEntity.getHandle());
        } else {
            getHandle().setOwner(null);
        }
    }
}
