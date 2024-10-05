package org.bukkit.craftbukkit.generator.structure;

import com.google.common.base.Suppliers;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;

import java.util.function.Supplier;

public class CraftStructure extends Structure implements Handleable<net.minecraft.world.level.levelgen.structure.Structure> {

    public static Structure minecraftToBukkit(net.minecraft.world.level.levelgen.structure.Structure minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.STRUCTURE, Registry.STRUCTURE);
    }

    public static net.minecraft.world.level.levelgen.structure.Structure bukkitToMinecraft(Structure bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    private final NamespacedKey key;
    private final net.minecraft.world.level.levelgen.structure.Structure structure;
    private final Supplier<StructureType> structureType;

    public CraftStructure(NamespacedKey key, net.minecraft.world.level.levelgen.structure.Structure structure) {
        this.key = key;
        this.structure = structure;
        this.structureType = Suppliers.memoize(() -> CraftStructureType.minecraftToBukkit(structure.type()));
    }

    @Override
    public net.minecraft.world.level.levelgen.structure.Structure getHandle() {
        return structure;
    }

    @Override
    public StructureType getStructureType() {
        return structureType.get();
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
