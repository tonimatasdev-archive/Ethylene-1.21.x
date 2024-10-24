package net.ethylenemc.ethylene;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;

public class Ethylene implements ModInitializer {
    @Override
    public void onInitialize() {
        LogUtils.getLogger().info("Ethylene has been enabled correctly.");
    }
}
