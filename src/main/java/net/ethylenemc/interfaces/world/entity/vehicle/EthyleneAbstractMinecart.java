package net.ethylenemc.interfaces.world.entity.vehicle;

import org.bukkit.util.Vector;

public interface EthyleneAbstractMinecart {
    boolean slowWhenEmpty();

    void slowWhenEmpty(boolean value);

    double maxSpeed();
    
    void maxSpeed(double value);

    Vector getFlyingVelocityMod();

    void setFlyingVelocityMod(Vector value);

    Vector getDerailedVelocityMod();

    void setDerailedVelocityMod(Vector value);
}
