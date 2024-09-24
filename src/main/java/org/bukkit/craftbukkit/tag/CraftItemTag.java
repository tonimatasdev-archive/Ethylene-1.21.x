package org.bukkit.craftbukkit.tag;

import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemType;

public class CraftItemTag extends CraftTag<net.minecraft.world.item.Item, Material> {

    public CraftItemTag(net.minecraft.core.Registry<net.minecraft.world.item.Item> registry, net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag) {
        super(registry, tag);
    }

    @Override
    public boolean isTagged(Material item) {
        net.minecraft.world.item.Item minecraft = CraftItemType.bukkitToMinecraft(item);

        // SPIGOT-6952: A Material is not necessary an item, in this case return false
        if (minecraft == null) {
            return false;
        }

        return minecraft.builtInRegistryHolder().is(tag);
    }

    @Override
    public Set<Material> getValues() {
        return getHandle().stream().map((item) -> CraftItemType.minecraftToBukkit(item.value())).collect(Collectors.toUnmodifiableSet());
    }
}
