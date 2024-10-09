package net.ethylenemc.interfaces.world.entity.projectile;

public interface EthyleneFishingHook {
    int getMinWaitTime();
    void setMinWaitTime(int value);
    int getMaxWaitTime();
    void setMaxWaitTime(int value);
    int getMinLureTime();
    void setMinLureTime(int value);
    int getMaxLureTime();
    void setMaxLureTime(int value);
    float getMinLureAngle();
    void setMinLureAngle(float value);
    float getMaxLureAngle();
    void setMaxLureAngle(float value);
    boolean getApplyLure();
    void setApplyLure(boolean value);
    boolean getRainInfluenced();
    void setRainInfluenced(boolean value);
    boolean getSkyInfluenced();
    void setSkyInfluenced(boolean value);
}
