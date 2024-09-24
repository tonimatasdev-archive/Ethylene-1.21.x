package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockType;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.attribute.CraftAttribute;
import org.bukkit.craftbukkit.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftItemType<M extends ItemMeta> implements ItemType.Typed<M>, Handleable<net.minecraft.world.item.Item> {

    private final NamespacedKey key;
    private final net.minecraft.world.item.Item item;
    private final Supplier<CraftItemMetas.ItemMetaData<M>> itemMetaData;

    public static Material minecraftToBukkit(net.minecraft.world.item.Item item) {
        return CraftMagicNumbers.getMaterial(item);
    }

    public static net.minecraft.world.item.Item bukkitToMinecraft(Material material) {
        return CraftMagicNumbers.getItem(material);
    }

    public static ItemType minecraftToBukkitNew(net.minecraft.world.item.Item minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.ITEM, Registry.ITEM);
    }

    public static net.minecraft.world.item.Item bukkitToMinecraftNew(ItemType bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public CraftItemType(NamespacedKey key, net.minecraft.world.item.Item item) {
        this.key = key;
        this.item = item;
        this.itemMetaData = Suppliers.memoize(() -> CraftItemMetas.getItemMetaData(this));
    }

    @NotNull
    @Override
    public Typed<ItemMeta> typed() {
        return this.typed(ItemMeta.class);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <Other extends ItemMeta> Typed<Other> typed(@NotNull final Class<Other> itemMetaType) {
        if (itemMetaType.isAssignableFrom(this.itemMetaData.get().metaClass())) return (Typed<Other>) this;

        throw new IllegalArgumentException("Cannot type item type " + this.key.toString() + " to meta type " + itemMetaType.getSimpleName());
    }

    @NotNull
    @Override
    public ItemStack createItemStack() {
        return this.createItemStack(1, null);
    }

    @NotNull
    @Override
    public ItemStack createItemStack(final int amount) {
        return this.createItemStack(amount, null);
    }

    @NotNull
    @Override
    public ItemStack createItemStack(Consumer<? super M> metaConfigurator) {
        return this.createItemStack(1, metaConfigurator);
    }

    @NotNull
    @Override
    public ItemStack createItemStack(final int amount, @Nullable final Consumer<? super M> metaConfigurator) {
        final ItemStack itemStack = new ItemStack(this.asMaterial(), amount);
        if (metaConfigurator != null) {
            final ItemMeta itemMeta = itemStack.getItemMeta();
            metaConfigurator.accept((M) itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    @Override
    public net.minecraft.world.item.Item getHandle() {
        return item;
    }

    public M getItemMeta(net.minecraft.world.item.ItemStack itemStack) {
        return itemMetaData.get().fromItemStack().apply(itemStack);
    }

    public M getItemMeta(ItemMeta itemMeta) {
        return itemMetaData.get().fromItemMeta().apply(this, (CraftMetaItem) itemMeta);
    }

    @Override
    public boolean hasBlockType() {
        return item instanceof net.minecraft.world.item.BlockItem;
    }

    @NotNull
    @Override
    public BlockType getBlockType() {
        if (!(item instanceof net.minecraft.world.item.BlockItem block)) {
            throw new IllegalStateException("The item type " + getKey() + " has no corresponding block type");
        }

        return CraftBlockType.minecraftToBukkitNew(block.getBlock());
    }

    @Override
    public Class<M> getItemMetaClass() {
        if (this == ItemType.AIR) {
            throw new UnsupportedOperationException("Air does not have ItemMeta");
        }
        return itemMetaData.get().metaClass();
    }

    @Override
    public int getMaxStackSize() {
        // Based of the material enum air is only 0, in PerMaterialTest it is also set as special case
        // the item info itself would return 64
        if (this == AIR) {
            return 0;
        }
        return item.components().getOrDefault(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 64);
    }

    @Override
    public short getMaxDurability() {
        return item.components().getOrDefault(net.minecraft.core.component.DataComponents.MAX_DAMAGE, 0).shortValue();
    }

    @Override
    public boolean isEdible() {
        return item.components().has(net.minecraft.core.component.DataComponents.FOOD);
    }

    @Override
    public boolean isRecord() {
        return item.components().has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE);
    }

    @Override
    public boolean isFuel() {
        return net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.isFuel(new net.minecraft.world.item.ItemStack(item));
    }

    @Override
    public boolean isCompostable() {
        return net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.containsKey(item);
    }

    @Override
    public float getCompostChance() {
        Preconditions.checkArgument(isCompostable(), "The item type " + getKey() + " is not compostable");
        return net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.getFloat(item);
    }

    @Override
    public ItemType getCraftingRemainingItem() {
        net.minecraft.world.item.Item expectedItem = item.getCraftingRemainingItem();
        return expectedItem == null ? null : minecraftToBukkitNew(expectedItem);
    }

//    @Override
//    public EquipmentSlot getEquipmentSlot() {
//        return CraftEquipmentSlot.getSlot(EntityInsentient.getEquipmentSlotForItem(CraftItemStack.asNMSCopy(ItemStack.of(this))));
//    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> defaultAttributes = ImmutableMultimap.builder();

        net.minecraft.world.item.component.ItemAttributeModifiers nmsDefaultAttributes = item.components().getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
        if (nmsDefaultAttributes.modifiers().isEmpty()) {
            nmsDefaultAttributes = item.getDefaultAttributeModifiers();
        }

        nmsDefaultAttributes.forEach(CraftEquipmentSlot.getNMS(slot), (key, value) -> {
            Attribute attribute = CraftAttribute.minecraftToBukkit(key.value());
            defaultAttributes.put(attribute, CraftAttributeInstance.convert(value, slot));
        });

        return defaultAttributes.build();
    }

    @Override
    public CreativeCategory getCreativeCategory() {
        return CreativeCategory.BUILDING_BLOCKS;
    }

    @Override
    public boolean isEnabledByFeature(@NotNull World world) {
        Preconditions.checkNotNull(world, "World cannot be null");
        return getHandle().isEnabled(((CraftWorld) world).getHandle().enabledFeatures());
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return item.getDescriptionId();
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public Material asMaterial() {
        return Registry.MATERIAL.get(this.key);
    }
}
