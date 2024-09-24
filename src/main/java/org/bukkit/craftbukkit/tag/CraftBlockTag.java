package org.bukkit.craftbukkit.tag;

import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.craftbukkit.block.CraftBlockType;

public class CraftBlockTag extends CraftTag<net.minecraft.world.level.block.Block, Material> {

    public CraftBlockTag(net.minecraft.core.Registry<net.minecraft.world.level.block.Block> registry, net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> tag) {
        super(registry, tag);
    }

    @Override
    public boolean isTagged(Material item) {
        net.minecraft.world.level.block.Block block = CraftBlockType.bukkitToMinecraft(item);

        // SPIGOT-6952: A Material is not necessary a block, in this case return false
        if (block == null) {
            return false;
        }

        return block.builtInRegistryHolder().is(tag);
    }

    @Override
    public Set<Material> getValues() {
        return getHandle().stream().map((block) -> CraftBlockType.minecraftToBukkit(block.value())).collect(Collectors.toUnmodifiableSet());
    }
}
