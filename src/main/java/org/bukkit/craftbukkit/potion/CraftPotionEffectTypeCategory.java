package org.bukkit.craftbukkit.potion;

import com.google.common.base.Preconditions;
import org.bukkit.potion.PotionEffectTypeCategory;

public final class CraftPotionEffectTypeCategory {

    public static PotionEffectTypeCategory minecraftToBukkit(net.minecraft.world.effect.MobEffectCategory minecraft) {
        Preconditions.checkArgument(minecraft != null);
        return PotionEffectTypeCategory.valueOf(minecraft.name());
    }

    public static net.minecraft.world.effect.MobEffectCategory bukkitToMinecraft(PotionEffectTypeCategory bukkit) {
        Preconditions.checkArgument(bukkit != null);
        return net.minecraft.world.effect.MobEffectCategory.valueOf(bukkit.name());
    }
}
