package net.ethylenemc.interfaces;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.MenuProvider;

import javax.sound.sampled.CompoundControl;

public interface EthyleneDoubleInventory extends MenuProvider {
    CompoundContainer getInventoryLargeChest();
}
