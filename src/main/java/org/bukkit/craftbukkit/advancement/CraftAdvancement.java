package org.bukkit.craftbukkit.advancement;

import java.util.Collection;
import java.util.Collections;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public class CraftAdvancement implements org.bukkit.advancement.Advancement {

    private final net.minecraft.advancements.AdvancementHolder handle;

    public CraftAdvancement(net.minecraft.advancements.AdvancementHolder handle) {
        this.handle = handle;
    }

    public net.minecraft.advancements.AdvancementHolder getHandle() {
        return handle;
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(handle.id());
    }

    @Override
    public Collection<String> getCriteria() {
        return Collections.unmodifiableCollection(handle.value().criteria().keySet());
    }

    @Override
    public AdvancementDisplay getDisplay() {
        if (handle.value().display().isEmpty()) {
            return null;
        }

        return new CraftAdvancementDisplay(handle.value().display().get());
    }
}
