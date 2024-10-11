package net.ethylenemc;

import net.minecraft.world.entity.animal.allay.Allay;
import org.bukkit.TreeType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class EthyleneCaptures {
    // Allay#duplicateAllay
    public static AtomicReference<Allay> duplicateAllay = new AtomicReference<>();
    
    // ServerPlayer#nextContainerCounter
    public static int nextContainerCounter;
    
    // EndDragonFight#respawnDragon
    public static AtomicBoolean respawnDragon = new AtomicBoolean(false);
    
    // SaplingBlock.treeType
    public static TreeType treeType;
}
