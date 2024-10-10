package net.ethylenemc.interfaces.world.level.block.entity;

import org.bukkit.potion.PotionEffect;

public interface EthyleneBeaconBlockEntity {
    PotionEffect getPrimaryEffect();

    PotionEffect getSecondaryEffect();
}
