package net.ethylenemc.interfaces.world.entity;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.bukkit.craftbukkit.attribute.CraftAttributeMap;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import java.util.Set;
import java.util.UUID;

public interface EthyleneLivingEntity {
    int expToDrop();
    
    void expToDrop(int value);
    
    CraftAttributeMap craftAttributes();

    boolean collides();
    
    void collides(boolean value);

    Set<UUID> collidableExemptions();

    boolean bukkitPickUpLoot();
    
    void bukkitPickUpLoot(boolean value);

    boolean addEffect(MobEffectInstance mobeffect, EntityPotionEffectEvent.Cause cause);

    boolean removeEffect(Holder<MobEffect> holder, EntityPotionEffectEvent.Cause cause);
}
