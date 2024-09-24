package org.bukkit.craftbukkit.inventory;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.bukkit.MusicInstrument;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.CraftMusicInstrument;
import org.bukkit.inventory.meta.MusicInstrumentMeta;

@DelegateDeserialization(SerializableMeta.class)
public class CraftMetaMusicInstrument extends CraftMetaItem implements MusicInstrumentMeta {

    static final ItemMetaKeyType<net.minecraft.core.Holder<net.minecraft.world.item.Instrument>> GOAT_HORN_INSTRUMENT = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.INSTRUMENT, "instrument");
    private MusicInstrument instrument;

    CraftMetaMusicInstrument(CraftMetaItem meta) {
        super(meta);

        if (meta instanceof CraftMetaMusicInstrument) {
            CraftMetaMusicInstrument craftMetaMusicInstrument = (CraftMetaMusicInstrument) meta;
            this.instrument = craftMetaMusicInstrument.instrument;
        }
    }

    CraftMetaMusicInstrument(net.minecraft.core.component.DataComponentPatch tag) {
        super(tag);

        getOrEmpty(tag, GOAT_HORN_INSTRUMENT).ifPresent((instrument) -> {
            this.instrument = CraftMusicInstrument.minecraftHolderToBukkit(instrument);
        });
    }

    CraftMetaMusicInstrument(Map<String, Object> map) {
        super(map);

        String instrumentString = SerializableMeta.getString(map, GOAT_HORN_INSTRUMENT.BUKKIT, true);
        if (instrumentString != null) {
            this.instrument = CraftMusicInstrument.stringToBukkit(instrumentString);
        }
    }

    @Override
    void applyToItem(CraftMetaItem.Applicator tag) {
        super.applyToItem(tag);

        if (instrument != null) {
            tag.put(GOAT_HORN_INSTRUMENT, CraftMusicInstrument.bukkitToMinecraftHolder(instrument));
        }
    }

    @Override
    boolean equalsCommon(CraftMetaItem meta) {
        if (!super.equalsCommon(meta)) {
            return false;
        }
        if (meta instanceof CraftMetaMusicInstrument) {
            CraftMetaMusicInstrument that = (CraftMetaMusicInstrument) meta;
            return this.instrument == that.instrument;
        }
        return true;
    }

    @Override
    boolean notUncommon(CraftMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof CraftMetaMusicInstrument || isInstrumentEmpty());
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && isInstrumentEmpty();
    }

    boolean isInstrumentEmpty() {
        return instrument == null;
    }

    @Override
    int applyHash() {
        final int orginal;
        int hash = orginal = super.applyHash();

        if (hasInstrument()) {
            hash = 61 * hash + instrument.hashCode();
        }

        return orginal != hash ? CraftMetaMusicInstrument.class.hashCode() ^ hash : hash;
    }

    @Override
    public CraftMetaMusicInstrument clone() {
        CraftMetaMusicInstrument meta = (CraftMetaMusicInstrument) super.clone();
        meta.instrument = this.instrument;
        return meta;
    }

    @Override
    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        super.serialize(builder);

        if (hasInstrument()) {
            builder.put(GOAT_HORN_INSTRUMENT.BUKKIT, CraftMusicInstrument.bukkitToString(instrument));
        }

        return builder;
    }

    @Override
    public MusicInstrument getInstrument() {
        return instrument;
    }

    public boolean hasInstrument() {
        return instrument != null;
    }

    @Override
    public void setInstrument(MusicInstrument instrument) {
        this.instrument = instrument;
    }
}
