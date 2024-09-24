package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.block.banner.CraftPatternType;
import org.bukkit.inventory.meta.BannerMeta;

@DelegateDeserialization(SerializableMeta.class)
public class CraftMetaBanner extends CraftMetaItem implements BannerMeta {

    static final ItemMetaKeyType<net.minecraft.world.level.block.entity.BannerPatternLayers> PATTERNS = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.BANNER_PATTERNS, "patterns");

    private List<Pattern> patterns = new ArrayList<Pattern>();

    CraftMetaBanner(CraftMetaItem meta) {
        super(meta);

        if (!(meta instanceof CraftMetaBanner)) {
            return;
        }

        CraftMetaBanner banner = (CraftMetaBanner) meta;
        patterns = new ArrayList<Pattern>(banner.patterns);
    }

    CraftMetaBanner(net.minecraft.core.component.DataComponentPatch tag) {
        super(tag);

        getOrEmpty(tag, PATTERNS).ifPresent((entityTag) -> {
            List<net.minecraft.world.level.block.entity.BannerPatternLayers.Layer> patterns = entityTag.layers();
            for (int i = 0; i < Math.min(patterns.size(), 20); i++) {
                net.minecraft.world.level.block.entity.BannerPatternLayers.Layer p = patterns.get(i);
                DyeColor color = DyeColor.getByWoolData((byte) p.color().getId());
                PatternType pattern = CraftPatternType.minecraftHolderToBukkit(p.pattern());

                if (color != null && pattern != null) {
                    this.patterns.add(new Pattern(color, pattern));
                }
            }
        });
    }

    CraftMetaBanner(Map<String, Object> map) {
        super(map);

        Iterable<?> rawPatternList = SerializableMeta.getObject(Iterable.class, map, PATTERNS.BUKKIT, true);
        if (rawPatternList == null) {
            return;
        }

        for (Object obj : rawPatternList) {
            Preconditions.checkArgument(obj instanceof Pattern, "Object (%s) in pattern list is not valid", obj.getClass());
            addPattern((Pattern) obj);
        }
    }

    @Override
    void applyToItem(CraftMetaItem.Applicator tag) {
        super.applyToItem(tag);

        List<net.minecraft.world.level.block.entity.BannerPatternLayers.Layer> newPatterns = new ArrayList<>();

        for (Pattern p : patterns) {
            newPatterns.add(new net.minecraft.world.level.block.entity.BannerPatternLayers.Layer(CraftPatternType.bukkitToMinecraftHolder(p.getPattern()), net.minecraft.world.item.DyeColor.byId(p.getColor().getWoolData())));
        }

        tag.put(PATTERNS, new net.minecraft.world.level.block.entity.BannerPatternLayers(newPatterns));
    }

    @Override
    public List<Pattern> getPatterns() {
        return new ArrayList<Pattern>(patterns);
    }

    @Override
    public void setPatterns(List<Pattern> patterns) {
        this.patterns = new ArrayList<Pattern>(patterns);
    }

    @Override
    public void addPattern(Pattern pattern) {
        patterns.add(pattern);
    }

    @Override
    public Pattern getPattern(int i) {
        return patterns.get(i);
    }

    @Override
    public Pattern removePattern(int i) {
        return patterns.remove(i);
    }

    @Override
    public void setPattern(int i, Pattern pattern) {
        patterns.set(i, pattern);
    }

    @Override
    public int numberOfPatterns() {
        return patterns.size();
    }

    @Override
    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        super.serialize(builder);

        if (!patterns.isEmpty()) {
            builder.put(PATTERNS.BUKKIT, ImmutableList.copyOf(patterns));
        }

        return builder;
    }

    @Override
    int applyHash() {
        final int original;
        int hash = original = super.applyHash();
        if (!patterns.isEmpty()) {
            hash = 31 * hash + patterns.hashCode();
        }
        return original != hash ? CraftMetaBanner.class.hashCode() ^ hash : hash;
    }

    @Override
    public boolean equalsCommon(CraftMetaItem meta) {
        if (!super.equalsCommon(meta)) {
            return false;
        }
        if (meta instanceof CraftMetaBanner) {
            CraftMetaBanner that = (CraftMetaBanner) meta;

            return patterns.equals(that.patterns);
        }
        return true;
    }

    @Override
    boolean notUncommon(CraftMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof CraftMetaBanner || patterns.isEmpty());
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && patterns.isEmpty();
    }

    @Override
    public CraftMetaBanner clone() {
        CraftMetaBanner meta = (CraftMetaBanner) super.clone();
        meta.patterns = new ArrayList<>(patterns);
        return meta;
    }
}
