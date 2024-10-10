package net.ethylenemc.interfaces.world.entity.monster;

import net.minecraft.world.entity.Entity;

public interface EthyleneCreeper {
    Entity entityIgniter();
    
    void entityIgniter(Entity value);

    void setPowered(boolean value);
}
