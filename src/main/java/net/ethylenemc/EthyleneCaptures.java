package net.ethylenemc;

import net.minecraft.world.entity.animal.allay.Allay;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class EthyleneCaptures {
    // Allay#duplicateAllay
    public static AtomicReference<Allay> duplicateAllay = new AtomicReference<>();
    
    // ServerPlayer#nextContainerCounter
    public static int nextContainerCounter;
}
