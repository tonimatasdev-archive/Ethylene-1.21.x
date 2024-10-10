package net.ethylenemc.interfaces.world.entity.vehicle;

import org.bukkit.util.Vector;

public interface EthyleneBoat {
    double maxSpeed();

    void maxSpeed(double value);

    double occupiedDeceleration();
    
    void occupiedDeceleration(double value);

    double unoccupiedDeceleration();
    
    void unoccupiedDeceleration(double value);

    boolean landBoats();
    
    void landBoats(boolean value);
}
