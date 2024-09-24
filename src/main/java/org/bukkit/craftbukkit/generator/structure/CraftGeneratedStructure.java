package org.bukkit.craftbukkit.generator.structure;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructurePiece;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.BoundingBox;

public class CraftGeneratedStructure implements GeneratedStructure {

    private final net.minecraft.world.level.levelgen.structure.StructureStart handle;
    private List<StructurePiece> pieces;

    public CraftGeneratedStructure(net.minecraft.world.level.levelgen.structure.StructureStart handle) {
        this.handle = handle;
    }

    @Override
    public BoundingBox getBoundingBox() {
        net.minecraft.world.level.levelgen.structure.BoundingBox bb = handle.getBoundingBox();
        return new BoundingBox(bb.minX(), bb.minY(), bb.minZ(), bb.maxX(), bb.maxY(), bb.maxZ());
    }

    @Override
    public Structure getStructure() {
        return CraftStructure.minecraftToBukkit(handle.getStructure());
    }

    @Override
    public Collection<StructurePiece> getPieces() {
        if (pieces == null) { // Cache the pieces on first request
            ImmutableList.Builder<StructurePiece> builder = new ImmutableList.Builder<>();
            for (net.minecraft.world.level.levelgen.structure.StructurePiece piece : handle.getPieces()) {
                builder.add(new CraftStructurePiece(piece));
            }

            pieces = builder.build();
        }

        return this.pieces;
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return handle.persistentDataContainer;
    }
}
