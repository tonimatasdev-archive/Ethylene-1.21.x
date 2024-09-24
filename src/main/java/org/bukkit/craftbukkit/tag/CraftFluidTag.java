package org.bukkit.craftbukkit.tag;

import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Fluid;
import org.bukkit.craftbukkit.CraftFluid;

public class CraftFluidTag extends CraftTag<net.minecraft.world.level.material.Fluid, Fluid> {

    public CraftFluidTag(net.minecraft.core.Registry<net.minecraft.world.level.material.Fluid> registry, net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> tag) {
        super(registry, tag);
    }

    @Override
    public boolean isTagged(Fluid fluid) {
        return CraftFluid.bukkitToMinecraft(fluid).is(tag);
    }

    @Override
    public Set<Fluid> getValues() {
        return getHandle().stream().map(net.minecraft.core.Holder::value).map(CraftFluid::minecraftToBukkit).collect(Collectors.toUnmodifiableSet());
    }
}
