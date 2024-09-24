package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.inventory.components.CraftFoodComponent;
import org.bukkit.craftbukkit.inventory.components.CraftJukeboxComponent;
import org.bukkit.craftbukkit.inventory.components.CraftToolComponent;
import org.bukkit.craftbukkit.util.CraftLegacy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class CraftItemFactory implements ItemFactory {
    static final Color DEFAULT_LEATHER_COLOR = Color.fromRGB(0xA06540);
    private static final CraftItemFactory instance;
    private static final net.minecraft.util.RandomSource randomSource = net.minecraft.util.RandomSource.create();

    static {
        instance = new CraftItemFactory();
        ConfigurationSerialization.registerClass(SerializableMeta.class);
        ConfigurationSerialization.registerClass(CraftFoodComponent.class);
        ConfigurationSerialization.registerClass(CraftFoodComponent.CraftFoodEffect.class);
        ConfigurationSerialization.registerClass(CraftToolComponent.class);
        ConfigurationSerialization.registerClass(CraftToolComponent.CraftToolRule.class);
        ConfigurationSerialization.registerClass(CraftJukeboxComponent.class);
    }

    private CraftItemFactory() {
    }

    @Override
    public boolean isApplicable(ItemMeta meta, ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        }
        return isApplicable(meta, itemstack.getType());
    }

    @Override
    public boolean isApplicable(ItemMeta meta, Material type) {
        type = CraftLegacy.fromLegacy(type); // This may be called from legacy item stacks, try to get the right material
        if (type == null || meta == null) {
            return false;
        }

        Preconditions.checkArgument(meta instanceof CraftMetaItem, "Meta of %s not created by %s", meta.getClass().toString(), CraftItemFactory.class.getName());

        return ((CraftMetaItem) meta).applicableTo(type);
    }

    @Override
    public ItemMeta getItemMeta(Material material) {
        Preconditions.checkArgument(material != null, "Material cannot be null");
        return getItemMeta(material, null);
    }

    private ItemMeta getItemMeta(Material material, CraftMetaItem meta) {
        material = CraftLegacy.fromLegacy(material); // This may be called from legacy item stacks, try to get the right material

        if (!material.isItem()) {
            // default behavior for none items is a new CraftMetaItem
            return new CraftMetaItem(meta);
        }

        return ((CraftItemType<?>) material.asItemType()).getItemMeta(meta);
    }

    @Override
    public boolean equals(ItemMeta meta1, ItemMeta meta2) {
        if (meta1 == meta2) {
            return true;
        }

        if (meta1 != null) {
            Preconditions.checkArgument(meta1 instanceof CraftMetaItem, "First meta of %s does not belong to %s", meta1.getClass().getName(), CraftItemFactory.class.getName());
        } else {
            return ((CraftMetaItem) meta2).isEmpty();
        }
        if (meta2 != null) {
            Preconditions.checkArgument(meta2 instanceof CraftMetaItem, "Second meta of %s does not belong to %s", meta2.getClass().getName(), CraftItemFactory.class.getName());
        } else {
            return ((CraftMetaItem) meta1).isEmpty();
        }

        return equals((CraftMetaItem) meta1, (CraftMetaItem) meta2);
    }

    boolean equals(CraftMetaItem meta1, CraftMetaItem meta2) {
        /*
         * This couldn't be done inside of the objects themselves, else force recursion.
         * This is a fairly clean way of implementing it, by dividing the methods into purposes and letting each method perform its own function.
         *
         * The common and uncommon were split, as both could have variables not applicable to the other, like a skull and book.
         * Each object needs its chance to say "hey wait a minute, we're not equal," but without the redundancy of using the 1.equals(2) && 2.equals(1) checking the 'commons' twice.
         *
         * Doing it this way fills all conditions of the .equals() method.
         */
        return meta1.equalsCommon(meta2) && meta1.notUncommon(meta2) && meta2.notUncommon(meta1);
    }

    public static CraftItemFactory instance() {
        return instance;
    }

    @Override
    public ItemMeta asMetaFor(ItemMeta meta, ItemStack stack) {
        Preconditions.checkArgument(stack != null, "ItemStack stack cannot be null");
        return asMetaFor(meta, stack.getType());
    }

    @Override
    public ItemMeta asMetaFor(ItemMeta meta, Material material) {
        Preconditions.checkArgument(material != null, "Material cannot be null");
        Preconditions.checkArgument(meta instanceof CraftMetaItem, "ItemMeta of %s not created by %s", (meta != null ? meta.getClass().toString() : "null"), CraftItemFactory.class.getName());
        return getItemMeta(material, (CraftMetaItem) meta);
    }

    @Override
    public Color getDefaultLeatherColor() {
        return DEFAULT_LEATHER_COLOR;
    }

    @Override
    public ItemStack createItemStack(String input) throws IllegalArgumentException {
        try {
            net.minecraft.commands.arguments.item.ItemParser.ItemResult arg = new net.minecraft.commands.arguments.item.ItemParser(net.minecraft.server.MinecraftServer.getDefaultRegistryAccess()).parse(new StringReader(input));

            net.minecraft.world.item.Item item = arg.item().value();
            net.minecraft.world.item.ItemStack nmsItemStack = new net.minecraft.world.item.ItemStack(item);

            net.minecraft.core.component.DataComponentPatch nbt = arg.components();
            if (nbt != null) {
                nmsItemStack.applyComponents(nbt);
            }

            return CraftItemStack.asCraftMirror(nmsItemStack);
        } catch (CommandSyntaxException ex) {
            throw new IllegalArgumentException("Could not parse ItemStack: " + input, ex);
        }
    }

    @Override
    public Material getSpawnEgg(EntityType type) {
        if (type == EntityType.UNKNOWN) {
            return null;
        }
        net.minecraft.world.entity.EntityType<?> nmsType = CraftEntityType.bukkitToMinecraft(type);
        net.minecraft.world.item.Item nmsItem = net.minecraft.world.item.SpawnEggItem.byId(nmsType);

        if (nmsItem == null) {
            return null;
        }

        return CraftItemType.minecraftToBukkit(nmsItem);
    }

    @Override
    public ItemStack enchantItem(Entity entity, ItemStack itemStack, int level, boolean allowTreasures) {
        Preconditions.checkArgument(entity != null, "The entity must not be null");

        return enchantItem(((CraftEntity) entity).getHandle().random, itemStack, level, allowTreasures);
    }

    @Override
    public ItemStack enchantItem(final World world, final ItemStack itemStack, final int level, final boolean allowTreasures) {
        Preconditions.checkArgument(world != null, "The world must not be null");

        return enchantItem(((CraftWorld) world).getHandle().random, itemStack, level, allowTreasures);
    }

    @Override
    public ItemStack enchantItem(final ItemStack itemStack, final int level, final boolean allowTreasures) {
        return enchantItem(randomSource, itemStack, level, allowTreasures);
    }

    private static ItemStack enchantItem(net.minecraft.util.RandomSource source, ItemStack itemStack, int level, boolean allowTreasures) {
        Preconditions.checkArgument(itemStack != null, "ItemStack must not be null");
        Preconditions.checkArgument(!itemStack.getType().isAir(), "ItemStack must not be air");
        itemStack = CraftItemStack.asCraftCopy(itemStack);
        CraftItemStack craft = (CraftItemStack) itemStack;
        net.minecraft.core.RegistryAccess registry = CraftRegistry.getMinecraftRegistry();
        Optional<net.minecraft.core.HolderSet.Named<net.minecraft.world.item.enchantment.Enchantment>> optional = (allowTreasures) ? Optional.empty() : registry.registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getTag(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE);
        return CraftItemStack.asCraftMirror(net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(source, craft.handle, level, registry, optional));
    }
}
