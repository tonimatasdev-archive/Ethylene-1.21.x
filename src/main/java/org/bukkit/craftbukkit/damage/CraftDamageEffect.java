package org.bukkit.craftbukkit.damage;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.damage.DamageEffect;

public class CraftDamageEffect implements DamageEffect {

    private final net.minecraft.world.damagesource.DamageEffects damageEffects;

    public CraftDamageEffect(net.minecraft.world.damagesource.DamageEffects damageEffects) {
        this.damageEffects = damageEffects;
    }

    public net.minecraft.world.damagesource.DamageEffects getHandle() {
        return this.damageEffects;
    }

    @Override
    public Sound getSound() {
        return CraftSound.minecraftToBukkit(this.getHandle().sound());
    }

    public static DamageEffect getById(String id) {
        for (net.minecraft.world.damagesource.DamageEffects damageEffects : net.minecraft.world.damagesource.DamageEffects.values()) {
            if (damageEffects.getSerializedName().equalsIgnoreCase(id)) {
                return toBukkit(damageEffects);
            }
        }
        return null;
    }

    public static DamageEffect toBukkit(net.minecraft.world.damagesource.DamageEffects damageEffects) {
        return new CraftDamageEffect(damageEffects);
    }
}
