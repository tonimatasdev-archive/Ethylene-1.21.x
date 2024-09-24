package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.inventory.meta.CompassMeta;

@DelegateDeserialization(SerializableMeta.class)
public class CraftMetaCompass extends CraftMetaItem implements CompassMeta {

    static final ItemMetaKeyType<net.minecraft.world.item.component.LodestoneTracker> LODESTONE_TARGET = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.LODESTONE_TRACKER, "LodestoneDimension");
    static final ItemMetaKey LODESTONE_POS = new ItemMetaKey("lodestone");
    static final ItemMetaKey LODESTONE_POS_WORLD = new ItemMetaKey("LodestonePosWorld");
    static final ItemMetaKey LODESTONE_POS_X = new ItemMetaKey("LodestonePosX");
    static final ItemMetaKey LODESTONE_POS_Y = new ItemMetaKey("LodestonePosY");
    static final ItemMetaKey LODESTONE_POS_Z = new ItemMetaKey("LodestonePosZ");
    static final ItemMetaKey LODESTONE_TRACKED = new ItemMetaKey("LodestoneTracked");

    private net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> lodestoneWorld;
    private int lodestoneX;
    private int lodestoneY;
    private int lodestoneZ;
    private boolean tracked = true;

    CraftMetaCompass(CraftMetaItem meta) {
        super(meta);
        if (!(meta instanceof CraftMetaCompass)) {
            return;
        }
        CraftMetaCompass compassMeta = (CraftMetaCompass) meta;
        lodestoneWorld = compassMeta.lodestoneWorld;
        lodestoneX = compassMeta.lodestoneX;
        lodestoneY = compassMeta.lodestoneY;
        lodestoneZ = compassMeta.lodestoneZ;
        tracked = compassMeta.tracked;
    }

    CraftMetaCompass(net.minecraft.core.component.DataComponentPatch tag) {
        super(tag);
        getOrEmpty(tag, LODESTONE_TARGET).ifPresent((lodestoneTarget) -> {
            lodestoneTarget.target().ifPresent((target) -> {
                lodestoneWorld = target.dimension();
                net.minecraft.core.BlockPos pos = target.pos();
                lodestoneX = pos.getX();
                lodestoneY = pos.getY();
                lodestoneZ = pos.getZ();
            });
            tracked = lodestoneTarget.tracked();
        });
    }

    CraftMetaCompass(Map<String, Object> map) {
        super(map);
        String lodestoneWorldString = SerializableMeta.getString(map, LODESTONE_POS_WORLD.BUKKIT, true);
        if (lodestoneWorldString != null) {
            lodestoneWorld = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, net.minecraft.resources.ResourceLocation.tryParse(lodestoneWorldString));
            lodestoneX = (Integer) map.get(LODESTONE_POS_X.BUKKIT);
            lodestoneY = (Integer) map.get(LODESTONE_POS_Y.BUKKIT);
            lodestoneZ = (Integer) map.get(LODESTONE_POS_Z.BUKKIT);
        } else {
            // legacy
            Location lodestone = SerializableMeta.getObject(Location.class, map, LODESTONE_POS.BUKKIT, true);
            if (lodestone != null && lodestone.getWorld() != null) {
                setLodestone(lodestone);
            }
        }
        tracked = SerializableMeta.getBoolean(map, LODESTONE_TRACKED.BUKKIT);
    }

    @Override
    void applyToItem(CraftMetaItem.Applicator tag) {
        super.applyToItem(tag);

        Optional<net.minecraft.core.GlobalPos> target = Optional.empty();
        if (lodestoneWorld != null) {
            target = Optional.of(new net.minecraft.core.GlobalPos(lodestoneWorld, new net.minecraft.core.BlockPos(lodestoneX, lodestoneY, lodestoneZ)));
        }

        if (target.isPresent() || hasLodestoneTracked()) {
            tag.put(LODESTONE_TARGET, new net.minecraft.world.item.component.LodestoneTracker(target, tracked));
        }
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && isCompassEmpty();
    }

    boolean isCompassEmpty() {
        return !(hasLodestone() || hasLodestoneTracked());
    }

    @Override
    public CraftMetaCompass clone() {
        CraftMetaCompass clone = ((CraftMetaCompass) super.clone());
        return clone;
    }

    @Override
    public boolean hasLodestone() {
        return lodestoneWorld != null;
    }

    @Override
    public Location getLodestone() {
        if (lodestoneWorld == null) {
            return null;
        }
        net.minecraft.server.level.ServerLevel worldServer = net.minecraft.server.MinecraftServer.getServer().getLevel(lodestoneWorld);
        World world = worldServer != null ? worldServer.getWorld() : null;
        return new Location(world, lodestoneX, lodestoneY, lodestoneZ); // world may be null here, if the referenced world is not loaded
    }

    @Override
    public void setLodestone(Location lodestone) {
        Preconditions.checkArgument(lodestone == null || lodestone.getWorld() != null, "world is null");
        if (lodestone == null) {
            this.lodestoneWorld = null;
        } else {
            this.lodestoneWorld = ((CraftWorld) lodestone.getWorld()).getHandle().dimension();
            this.lodestoneX = lodestone.getBlockX();
            this.lodestoneY = lodestone.getBlockY();
            this.lodestoneZ = lodestone.getBlockZ();
        }
    }

    boolean hasLodestoneTracked() {
        return !tracked;
    }

    @Override
    public boolean isLodestoneTracked() {
        return tracked;
    }

    @Override
    public void setLodestoneTracked(boolean tracked) {
        this.tracked = tracked;
    }

    @Override
    int applyHash() {
        final int original;
        int hash = original = super.applyHash();
        if (hasLodestone()) {
            hash = 73 * hash + lodestoneWorld.hashCode();
            hash = 73 * hash + lodestoneX;
            hash = 73 * hash + lodestoneY;
            hash = 73 * hash + lodestoneZ;
        }
        if (hasLodestoneTracked()) {
            hash = 73 * hash + (isLodestoneTracked() ? 1231 : 1237);
        }

        return original != hash ? CraftMetaCompass.class.hashCode() ^ hash : hash;
    }

    @Override
    boolean equalsCommon(CraftMetaItem meta) {
        if (!super.equalsCommon(meta)) {
            return false;
        }
        if (meta instanceof CraftMetaCompass) {
            CraftMetaCompass that = (CraftMetaCompass) meta;

            return (this.hasLodestone() ? that.hasLodestone() && this.lodestoneWorld.equals(that.lodestoneWorld)
                    && this.lodestoneX == that.lodestoneX && this.lodestoneY == that.lodestoneY
                    && this.lodestoneZ == that.lodestoneZ : !that.hasLodestone())
                    && this.tracked == that.tracked;
        }
        return true;
    }

    @Override
    boolean notUncommon(CraftMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof CraftMetaCompass || isCompassEmpty());
    }

    @Override
    Builder<String, Object> serialize(Builder<String, Object> builder) {
        super.serialize(builder);

        if (hasLodestone()) {
            builder.put(LODESTONE_POS_WORLD.BUKKIT, lodestoneWorld.location().toString());
            builder.put(LODESTONE_POS_X.BUKKIT, lodestoneX);
            builder.put(LODESTONE_POS_Y.BUKKIT, lodestoneY);
            builder.put(LODESTONE_POS_Z.BUKKIT, lodestoneZ);
        }
        if (hasLodestoneTracked()) {
            builder.put(LODESTONE_TRACKED.BUKKIT, tracked);
        }

        return builder;
    }
}
