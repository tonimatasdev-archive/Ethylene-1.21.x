package org.bukkit.craftbukkit.attribute;

import com.google.common.base.Preconditions;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

public class CraftAttributeMap implements Attributable {

    private final net.minecraft.world.entity.ai.attributes.AttributeMap handle;

    public CraftAttributeMap(net.minecraft.world.entity.ai.attributes.AttributeMap handle) {
        this.handle = handle;
    }

    @Override
    public AttributeInstance getAttribute(Attribute attribute) {
        Preconditions.checkArgument(attribute != null, "attribute");
        net.minecraft.world.entity.ai.attributes.AttributeInstance nms = handle.getInstance(CraftAttribute.bukkitToMinecraftHolder(attribute));

        return (nms == null) ? null : new CraftAttributeInstance(nms, attribute);
    }
}
