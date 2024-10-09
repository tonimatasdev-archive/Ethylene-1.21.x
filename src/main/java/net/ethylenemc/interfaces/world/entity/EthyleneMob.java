package net.ethylenemc.interfaces.world.entity;

import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;

public interface EthyleneMob {
    boolean setTarget(LivingEntity entityliving, EntityTargetEvent.TargetReason reason, boolean fireEvent);
}
