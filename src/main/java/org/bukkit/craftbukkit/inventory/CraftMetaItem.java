package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.mojang.serialization.DynamicOps;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.ethylenemc.EthyleneStatic;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.Overridden;
import org.bukkit.craftbukkit.attribute.CraftAttribute;
import org.bukkit.craftbukkit.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.inventory.CraftMetaItem.ItemMetaKey.Specific;
import org.bukkit.craftbukkit.inventory.components.CraftFoodComponent;
import org.bukkit.craftbukkit.inventory.components.CraftJukeboxComponent;
import org.bukkit.craftbukkit.inventory.components.CraftToolComponent;
import org.bukkit.craftbukkit.inventory.tags.DeprecatedCustomTagContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNBTTagConfigSerializer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.components.JukeboxPlayableComponent;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;

/**
 * Children must include the following:
 *
 * <li> Constructor(CraftMetaItem meta)
 * <li> Constructor(net.minecraft.nbt.CompoundTag tag)
 * <li> Constructor(Map&lt;String, Object&gt; map)
 * <br><br>
 * <li> void applyToItem(CraftMetaItem.Applicator tag)
 * <li> boolean applicableTo(Material type)
 * <br><br>
 * <li> boolean equalsCommon(CraftMetaItem meta)
 * <li> boolean notUncommon(CraftMetaItem meta)
 * <br><br>
 * <li> boolean isEmpty()
 * <li> boolean is{Type}Empty()
 * <br><br>
 * <li> int applyHash()
 * <li> public Class clone()
 * <br><br>
 * <li> Builder&lt;String, Object&gt; serialize(Builder&lt;String, Object&gt; builder)
 * <li> SerializableMeta.Deserializers deserializer()
 */
@DelegateDeserialization(SerializableMeta.class)
// Important: ItemMeta needs to be the first interface see #applicableTo(Material)
class CraftMetaItem implements ItemMeta, Damageable, Repairable, BlockDataMeta {

    static class ItemMetaKey {

        @Retention(RetentionPolicy.SOURCE)
        @Target(ElementType.FIELD)
        @interface Specific {
            enum To {
                BUKKIT,
                NBT,
                ;
            }
            To value();
        }

        final String BUKKIT;
        final String NBT;

        ItemMetaKey(final String both) {
            this(both, both);
        }

        ItemMetaKey(final String nbt, final String bukkit) {
            this.NBT = nbt;
            this.BUKKIT = bukkit;
        }
    }

    static final class ItemMetaKeyType<T> extends ItemMetaKey {

        final net.minecraft.core.component.DataComponentType<T> TYPE;

        ItemMetaKeyType(final net.minecraft.core.component.DataComponentType<T> type) {
            this(type, null, null);
        }

        ItemMetaKeyType(final net.minecraft.core.component.DataComponentType<T> type, final String both) {
            this(type, both, both);
        }

        ItemMetaKeyType(final net.minecraft.core.component.DataComponentType<T> type, final String nbt, final String bukkit) {
            super(nbt, bukkit);
            this.TYPE = type;
        }
    }

    static final class Applicator {

        private final net.minecraft.core.component.DataComponentPatch.Builder builder = net.minecraft.core.component.DataComponentPatch.builder();

        <T> Applicator put(ItemMetaKeyType<T> key, T value) {
            builder.set(key.TYPE, value);
            return this;
        }

        <T> Applicator putIfAbsent(net.minecraft.core.component.TypedDataComponent<?> component) {
            if (!builder.isSet(component.type())) {
                builder.set(component);
            }
            return this;
        }

        net.minecraft.core.component.DataComponentPatch build() {
            return builder.build();
        }
    }

    static final ItemMetaKeyType<net.minecraft.network.chat.Component> NAME = new ItemMetaKeyType(net.minecraft.core.component.DataComponents.CUSTOM_NAME, "display-name");
    static final ItemMetaKeyType<net.minecraft.network.chat.Component> ITEM_NAME = new ItemMetaKeyType(net.minecraft.core.component.DataComponents.ITEM_NAME, "item-name");
    static final ItemMetaKeyType<net.minecraft.world.item.component.ItemLore> LORE = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.LORE, "lore");
    static final ItemMetaKeyType<net.minecraft.world.item.component.CustomModelData> CUSTOM_MODEL_DATA = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA, "custom-model-data");
    static final ItemMetaKeyType<net.minecraft.world.item.enchantment.ItemEnchantments> ENCHANTMENTS = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.ENCHANTMENTS, "enchants");
    static final ItemMetaKeyType<Integer> REPAIR = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.REPAIR_COST, "repair-cost");
    static final ItemMetaKeyType<net.minecraft.world.item.component.ItemAttributeModifiers> ATTRIBUTES = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, "attribute-modifiers");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_IDENTIFIER = new ItemMetaKey("AttributeName");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_SLOT = new ItemMetaKey("Slot");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey HIDEFLAGS = new ItemMetaKey("ItemFlags");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<net.minecraft.util.Unit> HIDE_TOOLTIP = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.HIDE_TOOLTIP, "hide-tool-tip");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<net.minecraft.world.item.component.Unbreakable> UNBREAKABLE = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.UNBREAKABLE, "net.minecraft.world.item.component.Unbreakable");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<Boolean> ENCHANTMENT_GLINT_OVERRIDE = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, "enchantment-glint-override");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<net.minecraft.util.Unit> FIRE_RESISTANT = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.FIRE_RESISTANT, "fire-resistant");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<Integer> MAX_STACK_SIZE = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, "max-stack-size");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<net.minecraft.world.item.Rarity> RARITY = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.RARITY, "rarity");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<net.minecraft.world.food.FoodProperties> FOOD = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.FOOD, "food");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<net.minecraft.world.item.component.Tool> TOOL = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.TOOL, "tool");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<net.minecraft.world.item.JukeboxPlayable> JUKEBOX_PLAYABLE = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE, "jukebox-playable");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<Integer> DAMAGE = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.DAMAGE, "Damage");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<Integer> MAX_DAMAGE = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.MAX_DAMAGE, "max-damage");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<net.minecraft.world.item.component.BlockItemStateProperties> BLOCK_DATA = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.BLOCK_STATE, "BlockStateTag");
    static final ItemMetaKey BUKKIT_CUSTOM_TAG = new ItemMetaKey("PublicBukkitValues");
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<net.minecraft.util.Unit> HIDE_ADDITIONAL_TOOLTIP = new ItemMetaKeyType(net.minecraft.core.component.DataComponents.HIDE_ADDITIONAL_TOOLTIP);
    @Specific(Specific.To.NBT)
    static final ItemMetaKeyType<net.minecraft.world.item.component.CustomData> CUSTOM_DATA = new ItemMetaKeyType<>(net.minecraft.core.component.DataComponents.CUSTOM_DATA);

    // We store the raw original JSON representation of all text data. See SPIGOT-5063, SPIGOT-5656, SPIGOT-5304
    private net.minecraft.network.chat.Component displayName;
    private net.minecraft.network.chat.Component itemName;
    private List<net.minecraft.network.chat.Component> lore; // null and empty are two different states internally
    private Integer customModelData;
    private Map<String, String> blockData;
    private Map<Enchantment, Integer> enchantments;
    private Multimap<Attribute, AttributeModifier> attributeModifiers;
    private int repairCost;
    private int hideFlag;
    private boolean hideTooltip;
    private boolean unbreakable;
    private Boolean enchantmentGlintOverride;
    private boolean fireResistant;
    private Integer maxStackSize;
    private ItemRarity rarity;
    private CraftFoodComponent food;
    private CraftToolComponent tool;
    private CraftJukeboxComponent jukebox;
    private int damage;
    private Integer maxDamage;

    private static final Set<net.minecraft.core.component.DataComponentType> HANDLED_TAGS = Sets.newHashSet();
    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();

    private net.minecraft.nbt.CompoundTag customTag;
    protected net.minecraft.core.component.DataComponentPatch.Builder unhandledTags = net.minecraft.core.component.DataComponentPatch.builder();
    private Set<net.minecraft.core.component.DataComponentType<?>> removedTags = Sets.newHashSet();
    private CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer(DATA_TYPE_REGISTRY);

    private int version = CraftMagicNumbers.INSTANCE.getDataVersion(); // Internal use only

    CraftMetaItem(CraftMetaItem meta) {
        if (meta == null) {
            return;
        }

        this.displayName = meta.displayName;
        this.itemName = meta.itemName;

        if (meta.lore != null) {
            this.lore = new ArrayList<net.minecraft.network.chat.Component>(meta.lore);
        }

        this.customModelData = meta.customModelData;
        this.blockData = meta.blockData;

        if (meta.enchantments != null) {
            this.enchantments = new LinkedHashMap<Enchantment, Integer>(meta.enchantments);
        }

        if (meta.hasAttributeModifiers()) {
            this.attributeModifiers = LinkedHashMultimap.create(meta.attributeModifiers);
        }

        this.repairCost = meta.repairCost;
        this.hideFlag = meta.hideFlag;
        this.hideTooltip = meta.hideTooltip;
        this.unbreakable = meta.unbreakable;
        this.enchantmentGlintOverride = meta.enchantmentGlintOverride;
        this.fireResistant = meta.fireResistant;
        this.maxStackSize = meta.maxStackSize;
        this.rarity = meta.rarity;
        if (meta.hasFood()) {
            this.food = new CraftFoodComponent(meta.food);
        }
        if (meta.hasTool()) {
            this.tool = new CraftToolComponent(meta.tool);
        }
        if (meta.hasJukeboxPlayable()) {
            this.jukebox = new CraftJukeboxComponent(meta.jukebox);
        }
        this.damage = meta.damage;
        this.maxDamage = meta.maxDamage;
        this.unhandledTags.copy(meta.unhandledTags.build());
        this.removedTags.addAll(meta.removedTags);
        this.persistentDataContainer.putAll(meta.persistentDataContainer.getRaw());

        this.customTag = meta.customTag;

        this.version = meta.version;
    }

    CraftMetaItem(net.minecraft.core.component.DataComponentPatch tag) {
        getOrEmpty(tag, NAME).ifPresent((component) -> {
            displayName = component;
        });
        getOrEmpty(tag, ITEM_NAME).ifPresent((component) -> {
            itemName = component;
        });

        getOrEmpty(tag, LORE).ifPresent((l) -> {
            List<net.minecraft.network.chat.Component> list = l.lines();
            lore = new ArrayList<net.minecraft.network.chat.Component>(list.size());
            for (int index = 0; index < list.size(); index++) {
                net.minecraft.network.chat.Component line = list.get(index);
                lore.add(line);
            }
        });

        getOrEmpty(tag, CUSTOM_MODEL_DATA).ifPresent((i) -> {
            customModelData = i.value();
        });
        getOrEmpty(tag, BLOCK_DATA).ifPresent((t) -> {
            blockData = t.properties();
        });

        getOrEmpty(tag, ENCHANTMENTS).ifPresent((en) -> {
            this.enchantments = buildEnchantments(en);
            if (!en.showInTooltip) {
                addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        });
        getOrEmpty(tag, ATTRIBUTES).ifPresent((en) -> {
            this.attributeModifiers = buildModifiers(en);
            if (!en.showInTooltip()) {
                addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }
        });

        getOrEmpty(tag, REPAIR).ifPresent((i) -> {
            repairCost = i;
        });

        getOrEmpty(tag, HIDE_ADDITIONAL_TOOLTIP).ifPresent((h) -> {
            addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        });
        getOrEmpty(tag, HIDE_TOOLTIP).ifPresent((u) -> {
            hideTooltip = true;
        });
        getOrEmpty(tag, UNBREAKABLE).ifPresent((u) -> {
            unbreakable = true;
            if (!u.showInTooltip()) {
                addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
        });
        getOrEmpty(tag, ENCHANTMENT_GLINT_OVERRIDE).ifPresent((override) -> {
            enchantmentGlintOverride = override;
        });
        getOrEmpty(tag, FIRE_RESISTANT).ifPresent((u) -> {
            fireResistant = true;
        });
        getOrEmpty(tag, MAX_STACK_SIZE).ifPresent((i) -> {
            maxStackSize = i;
        });
        getOrEmpty(tag, RARITY).ifPresent((enumItemRarity) -> {
            rarity = ItemRarity.valueOf(enumItemRarity.name());
        });
        getOrEmpty(tag, FOOD).ifPresent((foodInfo) -> {
            food = new CraftFoodComponent(foodInfo);
        });
        getOrEmpty(tag, TOOL).ifPresent((toolInfo) -> {
            tool = new CraftToolComponent(toolInfo);
        });
        getOrEmpty(tag, JUKEBOX_PLAYABLE).ifPresent((jukeboxPlayable) -> {
            jukebox = new CraftJukeboxComponent(jukeboxPlayable);
        });
        getOrEmpty(tag, DAMAGE).ifPresent((i) -> {
            damage = i;
        });
        getOrEmpty(tag, MAX_DAMAGE).ifPresent((i) -> {
            maxDamage = i;
        });
        getOrEmpty(tag, CUSTOM_DATA).ifPresent((customData) -> {
            customTag = customData.copyTag();
            if (customTag.contains(BUKKIT_CUSTOM_TAG.NBT)) {
                net.minecraft.nbt.CompoundTag compound = customTag.getCompound(BUKKIT_CUSTOM_TAG.NBT);
                Set<String> keys = compound.getAllKeys();
                for (String key : keys) {
                    persistentDataContainer.put(key, compound.get(key).copy());
                }

                customTag.remove(BUKKIT_CUSTOM_TAG.NBT);
            }

            if (customTag.isEmpty()) {
                customTag = null;
            }
        });

        Set<Map.Entry<net.minecraft.core.component.DataComponentType<?>, Optional<?>>> keys = tag.entrySet();
        for (Map.Entry<net.minecraft.core.component.DataComponentType<?>, Optional<?>> key : keys) {
            if (!getHandledTags().contains(key.getKey())) {
                key.getValue().ifPresent((value) -> {
                    unhandledTags.set((net.minecraft.core.component.DataComponentType) key.getKey(), value);
                });
            }

            if (key.getValue().isEmpty()) {
                removedTags.add(key.getKey());
            }
        }
    }

    static Map<Enchantment, Integer> buildEnchantments(net.minecraft.world.item.enchantment.ItemEnchantments tag) {
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<Enchantment, Integer>(tag.size());

        tag.entrySet().forEach((entry) -> {
            net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> id = entry.getKey();
            int level = entry.getIntValue();

            Enchantment enchant = CraftEnchantment.minecraftHolderToBukkit(id);
            if (enchant != null) {
                enchantments.put(enchant, level);
            }
        });

        return enchantments;
    }

    static Multimap<Attribute, AttributeModifier> buildModifiers(net.minecraft.world.item.component.ItemAttributeModifiers tag) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
        List<net.minecraft.world.item.component.ItemAttributeModifiers.Entry> mods = tag.modifiers();
        int size = mods.size();

        for (int i = 0; i < size; i++) {
            net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry = mods.get(i);
            net.minecraft.world.entity.ai.attributes.AttributeModifier nmsModifier = entry.modifier();
            if (nmsModifier == null) {
                continue;
            }

            AttributeModifier attribMod = CraftAttributeInstance.convert(nmsModifier);

            Attribute attribute = CraftAttribute.minecraftHolderToBukkit(entry.attribute());
            if (attribute == null) {
                continue;
            }

            if (entry.slot() != null) {
                net.minecraft.world.entity.EquipmentSlotGroup slotName = entry.slot();
                if (slotName == null) {
                    modifiers.put(attribute, attribMod);
                    continue;
                }

                org.bukkit.inventory.EquipmentSlotGroup slot = null;
                try {
                    slot = CraftEquipmentSlot.getSlot(slotName);
                } catch (IllegalArgumentException ex) {
                    // SPIGOT-4551 - Slot is invalid, should really match nothing but this is undefined behaviour anyway
                }

                if (slot == null) {
                    modifiers.put(attribute, attribMod);
                    continue;
                }

                attribMod = new AttributeModifier(attribMod.getKey(), attribMod.getAmount(), attribMod.getOperation(), slot);
            }
            modifiers.put(attribute, attribMod);
        }
        return modifiers;
    }

    CraftMetaItem(Map<String, Object> map) {
        displayName = CraftChatMessage.fromJSONOrString(SerializableMeta.getString(map, NAME.BUKKIT, true), true, false);
        itemName = CraftChatMessage.fromJSONOrNull(SerializableMeta.getString(map, ITEM_NAME.BUKKIT, true));

        Iterable<?> lore = SerializableMeta.getObject(Iterable.class, map, LORE.BUKKIT, true);
        if (lore != null) {
            safelyAdd(lore, this.lore = new ArrayList<net.minecraft.network.chat.Component>(), true);
        }

        Integer customModelData = SerializableMeta.getObject(Integer.class, map, CUSTOM_MODEL_DATA.BUKKIT, true);
        if (customModelData != null) {
            setCustomModelData(customModelData);
        }

        Object blockData = SerializableMeta.getObject(Object.class, map, BLOCK_DATA.BUKKIT, true);
        if (blockData != null) {
            Map<String, String> mapBlockData = new HashMap<>();

            if (blockData instanceof Map) {
                for (Entry<?, ?> entry : ((Map<?, ?>) blockData).entrySet()) {
                    mapBlockData.put(entry.getKey().toString(), entry.getValue().toString());
                }
            } else {
                // Legacy pre 1.20.5:
                net.minecraft.nbt.CompoundTag nbtBlockData = (net.minecraft.nbt.CompoundTag) CraftNBTTagConfigSerializer.deserialize(blockData);
                for (String key : nbtBlockData.getAllKeys()) {
                    mapBlockData.put(key, nbtBlockData.getString(key));
                }
            }

            this.blockData = mapBlockData;
        }

        enchantments = buildEnchantments(map, ENCHANTMENTS);
        attributeModifiers = buildModifiers(map, ATTRIBUTES);

        Integer repairCost = SerializableMeta.getObject(Integer.class, map, REPAIR.BUKKIT, true);
        if (repairCost != null) {
            setRepairCost(repairCost);
        }

        Iterable<?> hideFlags = SerializableMeta.getObject(Iterable.class, map, HIDEFLAGS.BUKKIT, true);
        if (hideFlags != null) {
            for (Object hideFlagObject : hideFlags) {
                String hideFlagString = (String) hideFlagObject;
                try {
                    ItemFlag hideFlatEnum = CraftItemFlag.stringToBukkit(hideFlagString);
                    addItemFlags(hideFlatEnum);
                } catch (IllegalArgumentException ex) {
                    // Ignore when we got a old String which does not map to a Enum value anymore
                }
            }
        }

        Boolean hideTooltip = SerializableMeta.getObject(Boolean.class, map, HIDE_TOOLTIP.BUKKIT, true);
        if (hideTooltip != null) {
            setHideTooltip(hideTooltip);
        }

        Boolean unbreakable = SerializableMeta.getObject(Boolean.class, map, UNBREAKABLE.BUKKIT, true);
        if (unbreakable != null) {
            setUnbreakable(unbreakable);
        }

        Boolean enchantmentGlintOverride = SerializableMeta.getObject(Boolean.class, map, ENCHANTMENT_GLINT_OVERRIDE.BUKKIT, true);
        if (enchantmentGlintOverride != null) {
            setEnchantmentGlintOverride(enchantmentGlintOverride);
        }

        Boolean fireResistant = SerializableMeta.getObject(Boolean.class, map, FIRE_RESISTANT.BUKKIT, true);
        if (fireResistant != null) {
            setFireResistant(fireResistant);
        }

        Integer maxStackSize = SerializableMeta.getObject(Integer.class, map, MAX_STACK_SIZE.BUKKIT, true);
        if (maxStackSize != null) {
            setMaxStackSize(maxStackSize);
        }

        String rarity = SerializableMeta.getString(map, RARITY.BUKKIT, true);
        if (rarity != null) {
            setRarity(ItemRarity.valueOf(rarity));
        }

        CraftFoodComponent food = SerializableMeta.getObject(CraftFoodComponent.class, map, FOOD.BUKKIT, true);
        if (food != null) {
            setFood(food);
        }

        CraftToolComponent tool = SerializableMeta.getObject(CraftToolComponent.class, map, TOOL.BUKKIT, true);
        if (tool != null) {
            setTool(tool);
        }

        CraftJukeboxComponent jukeboxPlayable = SerializableMeta.getObject(CraftJukeboxComponent.class, map, JUKEBOX_PLAYABLE.BUKKIT, true);
        if (jukeboxPlayable != null) {
            setJukeboxPlayable(jukeboxPlayable);
        }

        Integer damage = SerializableMeta.getObject(Integer.class, map, DAMAGE.BUKKIT, true);
        if (damage != null) {
            setDamage(damage);
        }

        Integer maxDamage = SerializableMeta.getObject(Integer.class, map, MAX_DAMAGE.BUKKIT, true);
        if (maxDamage != null) {
            setMaxDamage(maxDamage);
        }

        String internal = SerializableMeta.getString(map, "internal", true);
        if (internal != null) {
            ByteArrayInputStream buf = new ByteArrayInputStream(Base64.getDecoder().decode(internal));
            try {
                net.minecraft.nbt.CompoundTag internalTag = net.minecraft.nbt.NbtIo.readCompressed(buf, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                deserializeInternal(internalTag, map);
            } catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String unhandled = SerializableMeta.getString(map, "unhandled", true);
        if (unhandled != null) {
            ByteArrayInputStream buf = new ByteArrayInputStream(Base64.getDecoder().decode(unhandled));
            try {
                net.minecraft.nbt.CompoundTag unhandledTag = net.minecraft.nbt.NbtIo.readCompressed(buf, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                net.minecraft.core.component.DataComponentPatch unhandledPatch = net.minecraft.core.component.DataComponentPatch.CODEC.parse(EthyleneStatic.getDefaultRegistryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), unhandledTag).result().get();
                this.unhandledTags.copy(unhandledPatch);

                for (Entry<net.minecraft.core.component.DataComponentType<?>, Optional<?>> entry : unhandledPatch.entrySet()) {
                    // Move removed unhandled tags to dedicated removedTags
                    if (!entry.getValue().isPresent()) {
                        net.minecraft.core.component.DataComponentType<?> key = entry.getKey();

                        this.unhandledTags.clear(key);
                        this.removedTags.add(key);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Iterable<?> removed = SerializableMeta.getObject(Iterable.class, map, "removed", true);
        if (removed != null) {
            net.minecraft.core.RegistryAccess registryAccess = CraftRegistry.getMinecraftRegistry();
            net.minecraft.core.Registry<net.minecraft.core.component.DataComponentType<?>> componentTypeRegistry = registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE);

            for (Object removedObject : removed) {
                String removedString = (String) removedObject;

                net.minecraft.core.component.DataComponentType<?> component = componentTypeRegistry.get(net.minecraft.resources.ResourceLocation.parse(removedString));
                if (component != null) {
                    this.removedTags.add(component);
                }
            }
        }

        Object nbtMap = SerializableMeta.getObject(Object.class, map, BUKKIT_CUSTOM_TAG.BUKKIT, true); // We read both legacy maps and potential modern snbt strings here
        if (nbtMap != null) {
            this.persistentDataContainer.putAll((net.minecraft.nbt.CompoundTag) CraftNBTTagConfigSerializer.deserialize(nbtMap));
        }

        String custom = SerializableMeta.getString(map, "custom", true);
        if (custom != null) {
            ByteArrayInputStream buf = new ByteArrayInputStream(Base64.getDecoder().decode(custom));
            try {
                customTag = net.minecraft.nbt.NbtIo.readCompressed(buf, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            } catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void deserializeInternal(net.minecraft.nbt.CompoundTag tag, Object context) {
        // SPIGOT-4576: Need to migrate from internal to proper data
        if (tag.contains(ATTRIBUTES.NBT, CraftMagicNumbers.NBT.TAG_LIST)) {
            this.attributeModifiers = buildModifiersLegacy(tag, ATTRIBUTES);
        }
    }

    private static Multimap<Attribute, AttributeModifier> buildModifiersLegacy(net.minecraft.nbt.CompoundTag tag, ItemMetaKey key) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
        if (!tag.contains(key.NBT, CraftMagicNumbers.NBT.TAG_LIST)) {
            return modifiers;
        }
        net.minecraft.nbt.ListTag mods = tag.getList(key.NBT, CraftMagicNumbers.NBT.TAG_COMPOUND);
        int size = mods.size();

        for (int i = 0; i < size; i++) {
            net.minecraft.nbt.CompoundTag entry = mods.getCompound(i);
            if (entry.isEmpty()) {
                // entry is not an actual net.minecraft.nbt.CompoundTag. getCompound returns empty net.minecraft.nbt.CompoundTag in that case
                continue;
            }
            net.minecraft.world.entity.ai.attributes.AttributeModifier nmsModifier = net.minecraft.world.entity.ai.attributes.AttributeModifier.load(entry);
            if (nmsModifier == null) {
                continue;
            }

            AttributeModifier attribMod = CraftAttributeInstance.convert(nmsModifier);

            String attributeName = entry.getString(ATTRIBUTES_IDENTIFIER.NBT);
            if (attributeName == null || attributeName.isEmpty()) {
                continue;
            }

            Attribute attribute = CraftAttribute.stringToBukkit(attributeName);
            if (attribute == null) {
                continue;
            }

            if (entry.contains(ATTRIBUTES_SLOT.NBT, CraftMagicNumbers.NBT.TAG_STRING)) {
                String slotName = entry.getString(ATTRIBUTES_SLOT.NBT);
                if (slotName == null || slotName.isEmpty()) {
                    modifiers.put(attribute, attribMod);
                    continue;
                }

                EquipmentSlot slot = null;
                try {
                    slot = CraftEquipmentSlot.getSlot(net.minecraft.world.entity.EquipmentSlot.byName(slotName.toLowerCase(Locale.ROOT)));
                } catch (IllegalArgumentException ex) {
                    // SPIGOT-4551 - Slot is invalid, should really match nothing but this is undefined behaviour anyway
                }

                if (slot == null) {
                    modifiers.put(attribute, attribMod);
                    continue;
                }

                attribMod = new AttributeModifier(attribMod.getKey(), attribMod.getAmount(), attribMod.getOperation(), slot.getGroup());
            }
            modifiers.put(attribute, attribMod);
        }
        return modifiers;
    }

    static Map<Enchantment, Integer> buildEnchantments(Map<String, Object> map, ItemMetaKey key) {
        Map<?, ?> ench = SerializableMeta.getObject(Map.class, map, key.BUKKIT, true);
        if (ench == null) {
            return null;
        }

        Map<Enchantment, Integer> enchantments = new LinkedHashMap<Enchantment, Integer>(ench.size());
        for (Map.Entry<?, ?> entry : ench.entrySet()) {
            Enchantment enchantment = CraftEnchantment.stringToBukkit(entry.getKey().toString());
            if ((enchantment != null) && (entry.getValue() instanceof Integer)) {
                enchantments.put(enchantment, (Integer) entry.getValue());
            }
        }

        return enchantments;
    }

    static Multimap<Attribute, AttributeModifier> buildModifiers(Map<String, Object> map, ItemMetaKey key) {
        Map<?, ?> mods = SerializableMeta.getObject(Map.class, map, key.BUKKIT, true);
        Multimap<Attribute, AttributeModifier> result = LinkedHashMultimap.create();
        if (mods == null) {
            return result;
        }

        for (Object obj : mods.keySet()) {
            if (!(obj instanceof String)) {
                continue;
            }
            String attributeName = (String) obj;
            if (Strings.isNullOrEmpty(attributeName)) {
                continue;
            }
            List<?> list = SerializableMeta.getObject(List.class, mods, attributeName, true);
            if (list == null || list.isEmpty()) {
                return result;
            }

            for (Object o : list) {
                if (!(o instanceof AttributeModifier)) { // this catches null
                    continue;
                }
                AttributeModifier modifier = (AttributeModifier) o;
                Attribute attribute = CraftAttribute.stringToBukkit(attributeName);
                if (attribute == null) {
                    continue;
                }

                result.put(attribute, modifier);
            }
        }
        return result;
    }

    @Overridden
    void applyToItem(CraftMetaItem.Applicator itemTag) {
        if (hasDisplayName()) {
            itemTag.put(NAME, displayName);
        }

        if (hasItemName()) {
            itemTag.put(ITEM_NAME, itemName);
        }

        if (lore != null) {
            itemTag.put(LORE, new net.minecraft.world.item.component.ItemLore(lore));
        }

        if (hasCustomModelData()) {
            itemTag.put(CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(customModelData));
        }

        if (hasBlockData()) {
            itemTag.put(BLOCK_DATA, new net.minecraft.world.item.component.BlockItemStateProperties(blockData));
        }

        if (hideFlag != 0) {
            if (hasItemFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)) {
                itemTag.put(HIDE_ADDITIONAL_TOOLTIP, net.minecraft.util.Unit.INSTANCE);
            }
        }

        applyEnchantments(enchantments, itemTag, ENCHANTMENTS, ItemFlag.HIDE_ENCHANTS);
        applyModifiers(attributeModifiers, itemTag);

        if (hasRepairCost()) {
            itemTag.put(REPAIR, repairCost);
        }

        if (isHideTooltip()) {
            itemTag.put(HIDE_TOOLTIP, net.minecraft.util.Unit.INSTANCE);
        }

        if (isUnbreakable()) {
            itemTag.put(UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(!hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)));
        }

        if (hasEnchantmentGlintOverride()) {
            itemTag.put(ENCHANTMENT_GLINT_OVERRIDE, getEnchantmentGlintOverride());
        }

        if (isFireResistant()) {
            itemTag.put(FIRE_RESISTANT, net.minecraft.util.Unit.INSTANCE);
        }

        if (hasMaxStackSize()) {
            itemTag.put(MAX_STACK_SIZE, maxStackSize);
        }

        if (hasRarity()) {
            itemTag.put(RARITY, net.minecraft.world.item.Rarity.valueOf(rarity.name()));
        }

        if (hasFood()) {
            itemTag.put(FOOD, food.getHandle());
        }

        if (hasTool()) {
            itemTag.put(TOOL, tool.getHandle());
        }

        if (hasJukeboxPlayable()) {
            itemTag.put(JUKEBOX_PLAYABLE, jukebox.getHandle());
        }

        if (hasDamage()) {
            itemTag.put(DAMAGE, damage);
        }

        if (hasMaxDamage()) {
            itemTag.put(MAX_DAMAGE, maxDamage);
        }

        for (Map.Entry<net.minecraft.core.component.DataComponentType<?>, Optional<?>> e : unhandledTags.build().entrySet()) {
            e.getValue().ifPresent((value) -> {
                itemTag.builder.set((net.minecraft.core.component.DataComponentType) e.getKey(), value);
            });
        }

        for (net.minecraft.core.component.DataComponentType<?> removed : removedTags) {
            if (!itemTag.builder.isSet(removed)) {
                itemTag.builder.remove(removed);
            }
        }

        net.minecraft.nbt.CompoundTag customTag = (this.customTag != null) ? this.customTag.copy() : null;
        if (!persistentDataContainer.isEmpty()) {
            net.minecraft.nbt.CompoundTag bukkitCustomCompound = new net.minecraft.nbt.CompoundTag();
            Map<String, net.minecraft.nbt.Tag> rawPublicMap = persistentDataContainer.getRaw();

            for (Map.Entry<String, net.minecraft.nbt.Tag> nbtBaseEntry : rawPublicMap.entrySet()) {
                bukkitCustomCompound.put(nbtBaseEntry.getKey(), nbtBaseEntry.getValue());
            }

            if (customTag == null) {
                customTag = new net.minecraft.nbt.CompoundTag();
            }
            customTag.put(BUKKIT_CUSTOM_TAG.BUKKIT, bukkitCustomCompound);
        }

        if (customTag != null) {
            itemTag.put(CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(customTag));
        }
    }

    void applyEnchantments(Map<Enchantment, Integer> enchantments, CraftMetaItem.Applicator tag, ItemMetaKeyType<net.minecraft.world.item.enchantment.ItemEnchantments> key, ItemFlag itemFlag) {
        if (enchantments == null) {
            return;
        }

        net.minecraft.world.item.enchantment.ItemEnchantments.Mutable list = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            list.set(CraftEnchantment.bukkitToMinecraftHolder(entry.getKey()), entry.getValue());
        }

        list.showInTooltip = !hasItemFlag(itemFlag);
        tag.put(key, list.toImmutable());
    }

    void applyModifiers(Multimap<Attribute, AttributeModifier> modifiers, CraftMetaItem.Applicator tag) {
        if (modifiers == null || modifiers.isEmpty()) {
            if (hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)) {
                tag.put(ATTRIBUTES, new net.minecraft.world.item.component.ItemAttributeModifiers(Collections.emptyList(), false));
            }
            return;
        }

        net.minecraft.world.item.component.ItemAttributeModifiers.Builder list = net.minecraft.world.item.component.ItemAttributeModifiers.builder();
        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            net.minecraft.world.entity.ai.attributes.AttributeModifier nmsModifier = CraftAttributeInstance.convert(entry.getValue());

            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> name = CraftAttribute.bukkitToMinecraftHolder(entry.getKey());
            if (name == null) {
                continue;
            }

            net.minecraft.world.entity.EquipmentSlotGroup group = CraftEquipmentSlot.getNMSGroup(entry.getValue().getSlotGroup());
            list.add(name, nmsModifier, group);
        }
        tag.put(ATTRIBUTES, list.build().withTooltip(!hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)));
    }

    boolean applicableTo(Material type) {
        if (type == Material.AIR || !type.isItem()) {
            return false;
        }

        if (getClass() == CraftMetaItem.class) {
            return true;
        }

        // We assume that the corresponding bukkit interface is always the first one
        return type.asItemType().getItemMetaClass() == getClass().getInterfaces()[0];
    }

    @Overridden
    boolean isEmpty() {
        return !(hasDisplayName() || hasItemName() || hasLocalizedName() || hasEnchants() || (lore != null) || hasCustomModelData() || hasBlockData() || hasRepairCost() || !unhandledTags.build().isEmpty() || !removedTags.isEmpty() || !persistentDataContainer.isEmpty() || hideFlag != 0 || isHideTooltip() || isUnbreakable() || hasEnchantmentGlintOverride() || isFireResistant() || hasMaxStackSize() || hasRarity() || hasFood() || hasTool() || hasJukeboxPlayable() || hasDamage() || hasMaxDamage() || hasAttributeModifiers() || customTag != null);
    }

    @Override
    public String getDisplayName() {
        return CraftChatMessage.fromComponent(displayName);
    }

    @Override
    public final void setDisplayName(String name) {
        this.displayName = CraftChatMessage.fromStringOrNull(name);
    }

    @Override
    public boolean hasDisplayName() {
        return displayName != null;
    }

    @Override
    public String getItemName() {
        return CraftChatMessage.fromComponent(itemName);
    }

    @Override
    public final void setItemName(String name) {
        this.itemName = CraftChatMessage.fromStringOrNull(name);
    }

    @Override
    public boolean hasItemName() {
        return itemName != null;
    }

    @Override
    public String getLocalizedName() {
        return getDisplayName();
    }

    @Override
    public void setLocalizedName(String name) {
    }

    @Override
    public boolean hasLocalizedName() {
        return false;
    }

    @Override
    public boolean hasLore() {
        return this.lore != null && !this.lore.isEmpty();
    }

    @Override
    public boolean hasRepairCost() {
        return repairCost > 0;
    }

    @Override
    public boolean hasEnchant(Enchantment ench) {
        Preconditions.checkArgument(ench != null, "Enchantment cannot be null");
        return hasEnchants() && enchantments.containsKey(ench);
    }

    @Override
    public int getEnchantLevel(Enchantment ench) {
        Preconditions.checkArgument(ench != null, "Enchantment cannot be null");
        Integer level = hasEnchants() ? enchantments.get(ench) : null;
        if (level == null) {
            return 0;
        }
        return level;
    }

    @Override
    public Map<Enchantment, Integer> getEnchants() {
        return hasEnchants() ? ImmutableMap.copyOf(enchantments) : ImmutableMap.<Enchantment, Integer>of();
    }

    @Override
    public boolean addEnchant(Enchantment ench, int level, boolean ignoreRestrictions) {
        Preconditions.checkArgument(ench != null, "Enchantment cannot be null");
        if (enchantments == null) {
            enchantments = new LinkedHashMap<Enchantment, Integer>(4);
        }

        if (ignoreRestrictions || level >= ench.getStartLevel() && level <= ench.getMaxLevel()) {
            Integer old = enchantments.put(ench, level);
            return old == null || old != level;
        }
        return false;
    }

    @Override
    public boolean removeEnchant(Enchantment ench) {
        Preconditions.checkArgument(ench != null, "Enchantment cannot be null");
        boolean enchantmentRemoved = hasEnchants() && enchantments.remove(ench) != null;
        // If we no longer have any enchantments, then clear enchantment tag
        if (enchantmentRemoved && enchantments.isEmpty()) {
            enchantments = null;
        }
        return enchantmentRemoved;
    }

    @Override
    public void removeEnchantments() {
        if (hasEnchants()) {
            enchantments.clear();
        }
    }

    @Override
    public boolean hasEnchants() {
        return !(enchantments == null || enchantments.isEmpty());
    }

    @Override
    public boolean hasConflictingEnchant(Enchantment ench) {
        return checkConflictingEnchants(enchantments, ench);
    }

    @Override
    public void addItemFlags(ItemFlag... hideFlags) {
        for (ItemFlag f : hideFlags) {
            this.hideFlag |= getBitModifier(f);
        }
    }

    @Override
    public void removeItemFlags(ItemFlag... hideFlags) {
        for (ItemFlag f : hideFlags) {
            this.hideFlag &= ~getBitModifier(f);
        }
    }

    @Override
    public Set<ItemFlag> getItemFlags() {
        Set<ItemFlag> currentFlags = EnumSet.noneOf(ItemFlag.class);

        for (ItemFlag f : ItemFlag.values()) {
            if (hasItemFlag(f)) {
                currentFlags.add(f);
            }
        }

        return currentFlags;
    }

    @Override
    public boolean hasItemFlag(ItemFlag flag) {
        int bitModifier = getBitModifier(flag);
        return (this.hideFlag & bitModifier) == bitModifier;
    }

    private int getBitModifier(ItemFlag hideFlag) {
        return 1 << hideFlag.ordinal();
    }

    @Override
    public List<String> getLore() {
        return this.lore == null ? null : new ArrayList<String>(Lists.transform(this.lore, CraftChatMessage::fromComponent));
    }

    @Override
    public void setLore(List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            this.lore = null;
        } else {
            if (this.lore == null) {
                this.lore = new ArrayList<net.minecraft.network.chat.Component>(lore.size());
            } else {
                this.lore.clear();
            }
            safelyAdd(lore, this.lore, false);
        }
    }

    @Override
    public boolean hasCustomModelData() {
        return customModelData != null;
    }

    @Override
    public int getCustomModelData() {
        Preconditions.checkState(hasCustomModelData(), "We don't have net.minecraft.world.item.component.CustomModelData! Check hasCustomModelData first!");
        return customModelData;
    }

    @Override
    public void setCustomModelData(Integer data) {
        this.customModelData = data;
    }

    @Override
    public boolean hasBlockData() {
        return this.blockData != null;
    }

    @Override
    public BlockData getBlockData(Material material) {
        net.minecraft.world.level.block.state.BlockState defaultData = CraftBlockType.bukkitToMinecraft(material).defaultBlockState();
        return CraftBlockData.fromData((hasBlockData()) ? new net.minecraft.world.item.component.BlockItemStateProperties(blockData).apply(defaultData) : defaultData);
    }

    @Override
    public void setBlockData(BlockData blockData) {
        this.blockData = (blockData == null) ? null : ((CraftBlockData) blockData).toStates();
    }

    @Override
    public int getRepairCost() {
        return repairCost;
    }

    @Override
    public void setRepairCost(int cost) { // TODO: Does this have limits?
        repairCost = cost;
    }

    @Override
    public boolean isHideTooltip() {
        return this.hideTooltip;
    }

    @Override
    public void setHideTooltip(boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
    }

    @Override
    public boolean isUnbreakable() {
        return unbreakable;
    }

    @Override
    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    @Override
    public boolean hasEnchantmentGlintOverride() {
        return this.enchantmentGlintOverride != null;
    }

    @Override
    public Boolean getEnchantmentGlintOverride() {
        Preconditions.checkState(hasEnchantmentGlintOverride(), "We don't have enchantment_glint_override! Check hasEnchantmentGlintOverride first!");
        return this.enchantmentGlintOverride;
    }

    @Override
    public void setEnchantmentGlintOverride(Boolean override) {
        this.enchantmentGlintOverride = override;
    }

    @Override
    public boolean isFireResistant() {
        return this.fireResistant;
    }

    @Override
    public void setFireResistant(boolean fireResistant) {
        this.fireResistant = fireResistant;
    }

    @Override
    public boolean hasMaxStackSize() {
        return this.maxStackSize != null;
    }

    @Override
    public int getMaxStackSize() {
        Preconditions.checkState(hasMaxStackSize(), "We don't have max_stack_size! Check hasMaxStackSize first!");
        return this.maxStackSize;
    }

    @Override
    public void setMaxStackSize(Integer max) {
        Preconditions.checkArgument(max == null || max > 0, "max_stack_size must be > 0");
        Preconditions.checkArgument(max == null || max <= net.minecraft.world.item.Item.ABSOLUTE_MAX_STACK_SIZE, "max_stack_size must be <= 99");
        this.maxStackSize = max;
    }

    @Override
    public boolean hasRarity() {
        return this.rarity != null;
    }

    @Override
    public ItemRarity getRarity() {
        Preconditions.checkState(hasRarity(), "We don't have rarity! Check hasRarity first!");
        return this.rarity;
    }

    @Override
    public void setRarity(ItemRarity rarity) {
        this.rarity = rarity;
    }

    @Override
    public boolean hasFood() {
        return this.food != null;
    }

    @Override
    public FoodComponent getFood() {
        return (this.hasFood()) ? new CraftFoodComponent(this.food) : new CraftFoodComponent(new net.minecraft.world.food.FoodProperties(0, 0, false, 0, Optional.empty(), Collections.emptyList()));
    }

    @Override
    public void setFood(FoodComponent food) {
        this.food = (food == null) ? null : new CraftFoodComponent((CraftFoodComponent) food);
    }

    @Override
    public boolean hasTool() {
        return this.tool != null;
    }

    @Override
    public ToolComponent getTool() {
        return (this.hasTool()) ? new CraftToolComponent(this.tool) : new CraftToolComponent(new net.minecraft.world.item.component.Tool(Collections.emptyList(), 1.0F, 0));
    }

    @Override
    public void setTool(ToolComponent tool) {
        this.tool = (tool == null) ? null : new CraftToolComponent((CraftToolComponent) tool);
    }

    @Override
    public boolean hasJukeboxPlayable() {
        return this.jukebox != null;
    }

    @Override
    public JukeboxPlayableComponent getJukeboxPlayable() {
        return (this.hasJukeboxPlayable()) ? new CraftJukeboxComponent(this.jukebox) : new CraftJukeboxComponent(new net.minecraft.world.item.JukeboxPlayable(new net.minecraft.world.item.EitherHolder<>(net.minecraft.world.item.JukeboxSongs.THIRTEEN), true));
    }

    @Override
    public void setJukeboxPlayable(JukeboxPlayableComponent jukeboxPlayable) {
        this.jukebox = (jukeboxPlayable == null) ? null : new CraftJukeboxComponent((CraftJukeboxComponent) jukeboxPlayable);
    }

    @Override
    public boolean hasAttributeModifiers() {
        return attributeModifiers != null && !attributeModifiers.isEmpty();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        return hasAttributeModifiers() ? ImmutableMultimap.copyOf(attributeModifiers) : null;
    }

    private void checkAttributeList() {
        if (attributeModifiers == null) {
            attributeModifiers = LinkedHashMultimap.create();
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nullable EquipmentSlot slot) {
        checkAttributeList();
        SetMultimap<Attribute, AttributeModifier> result = LinkedHashMultimap.create();
        for (Map.Entry<Attribute, AttributeModifier> entry : attributeModifiers.entries()) {
            if (entry.getValue().getSlot() == null || entry.getValue().getSlot() == slot) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    @Override
    public Collection<AttributeModifier> getAttributeModifiers(@Nonnull Attribute attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        return attributeModifiers.containsKey(attribute) ? ImmutableList.copyOf(attributeModifiers.get(attribute)) : null;
    }

    @Override
    public boolean addAttributeModifier(@Nonnull Attribute attribute, @Nonnull AttributeModifier modifier) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        Preconditions.checkNotNull(modifier, "AttributeModifier cannot be null");
        checkAttributeList();
        for (Map.Entry<Attribute, AttributeModifier> entry : attributeModifiers.entries()) {
            Preconditions.checkArgument(!entry.getValue().getKey().equals(modifier.getKey()), "Cannot register AttributeModifier. Modifier is already applied! %s", modifier);
        }
        return attributeModifiers.put(attribute, modifier);
    }

    @Override
    public void setAttributeModifiers(@Nullable Multimap<Attribute, AttributeModifier> attributeModifiers) {
        if (attributeModifiers == null || attributeModifiers.isEmpty()) {
            this.attributeModifiers = LinkedHashMultimap.create();
            return;
        }

        checkAttributeList();
        this.attributeModifiers.clear();

        Iterator<Map.Entry<Attribute, AttributeModifier>> iterator = attributeModifiers.entries().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Attribute, AttributeModifier> next = iterator.next();

            if (next.getKey() == null || next.getValue() == null) {
                iterator.remove();
                continue;
            }
            this.attributeModifiers.put(next.getKey(), next.getValue());
        }
    }

    @Override
    public boolean removeAttributeModifier(@Nonnull Attribute attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        checkAttributeList();
        return !attributeModifiers.removeAll(attribute).isEmpty();
    }

    @Override
    public boolean removeAttributeModifier(@Nullable EquipmentSlot slot) {
        checkAttributeList();
        int removed = 0;
        Iterator<Map.Entry<Attribute, AttributeModifier>> iter = attributeModifiers.entries().iterator();

        while (iter.hasNext()) {
            Map.Entry<Attribute, AttributeModifier> entry = iter.next();
            // Explicitly match against null because (as of MC 1.13) AttributeModifiers without a -
            // set slot are active in any slot.
            if (entry.getValue().getSlot() == null || entry.getValue().getSlot() == slot) {
                iter.remove();
                ++removed;
            }
        }
        return removed > 0;
    }

    @Override
    public boolean removeAttributeModifier(@Nonnull Attribute attribute, @Nonnull AttributeModifier modifier) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        Preconditions.checkNotNull(modifier, "AttributeModifier cannot be null");
        checkAttributeList();
        int removed = 0;
        Iterator<Map.Entry<Attribute, AttributeModifier>> iter = attributeModifiers.entries().iterator();

        while (iter.hasNext()) {
            Map.Entry<Attribute, AttributeModifier> entry = iter.next();
            if (entry.getKey() == null || entry.getValue() == null) {
                iter.remove();
                ++removed;
                continue; // remove all null values while we are here
            }

            if (entry.getKey() == attribute && entry.getValue().getKey().equals(modifier.getKey())) {
                iter.remove();
                ++removed;
            }
        }
        return removed > 0;
    }

    @Override
    public String getAsString() {
        CraftMetaItem.Applicator tag = new CraftMetaItem.Applicator();
        applyToItem(tag);
        net.minecraft.core.component.DataComponentPatch patch = tag.build();
        net.minecraft.nbt.Tag nbt = net.minecraft.core.component.DataComponentPatch.CODEC.encodeStart(EthyleneStatic.getDefaultRegistryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), patch).getOrThrow();
        return nbt.toString();
    }

    @Override
    public String getAsComponentString() {
        CraftMetaItem.Applicator tag = new CraftMetaItem.Applicator();
        applyToItem(tag);
        net.minecraft.core.component.DataComponentPatch patch = tag.build();

        net.minecraft.core.RegistryAccess registryAccess = CraftRegistry.getMinecraftRegistry();
        DynamicOps<net.minecraft.nbt.Tag> ops = registryAccess.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        net.minecraft.core.Registry<net.minecraft.core.component.DataComponentType<?>> componentTypeRegistry = registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE);

        StringJoiner componentString = new StringJoiner(",", "[", "]");
        for (Entry<net.minecraft.core.component.DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
            net.minecraft.core.component.DataComponentType<?> componentType = entry.getKey();
            Optional<?> componentValue = entry.getValue();
            String componentKey = componentTypeRegistry.getResourceKey(componentType).orElseThrow().location().toString();

            if (componentValue.isPresent()) {
                net.minecraft.nbt.Tag componentValueAsNBT = (net.minecraft.nbt.Tag) ((net.minecraft.core.component.DataComponentType) componentType).codecOrThrow().encodeStart(ops, componentValue.get()).getOrThrow();
                String componentValueAsNBTString = new net.minecraft.nbt.SnbtPrinterTagVisitor("", 0, new ArrayList<>()).visit(componentValueAsNBT);
                componentString.add(componentKey + "=" + componentValueAsNBTString);
            } else {
                componentString.add("!" + componentKey);
            }
        }

        return componentString.toString();
    }

    @Override
    public CustomItemTagContainer getCustomTagContainer() {
        return new DeprecatedCustomTagContainer(this.getPersistentDataContainer());
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return this.persistentDataContainer;
    }

    private static boolean compareModifiers(Multimap<Attribute, AttributeModifier> first, Multimap<Attribute, AttributeModifier> second) {
        if (first == null || second == null) {
            return false;
        }
        for (Map.Entry<Attribute, AttributeModifier> entry : first.entries()) {
            if (!second.containsEntry(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        for (Map.Entry<Attribute, AttributeModifier> entry : second.entries()) {
            if (!first.containsEntry(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasDamage() {
        return damage > 0;
    }

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public void setDamage(int damage) {
        this.damage = damage;
    }

    @Override
    public boolean hasMaxDamage() {
        return this.maxDamage != null;
    }

    @Override
    public int getMaxDamage() {
        Preconditions.checkState(hasMaxDamage(), "We don't have max_damage! Check hasMaxDamage first!");
        return this.maxDamage;
    }

    @Override
    public void setMaxDamage(Integer maxDamage) {
        this.maxDamage = maxDamage;
    }

    @Override
    public final boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (!(object instanceof CraftMetaItem)) {
            return false;
        }
        return CraftItemFactory.instance().equals(this, (ItemMeta) object);
    }

    /**
     * This method is almost as weird as notUncommon.
     * Only return false if your common internals are unequal.
     * Checking your own internals is redundant if you are not common, as notUncommon is meant for checking those 'not common' variables.
     */
    @Overridden
    boolean equalsCommon(CraftMetaItem that) {
        return ((this.hasDisplayName() ? that.hasDisplayName() && this.displayName.equals(that.displayName) : !that.hasDisplayName()))
                && (this.hasItemName() ? that.hasItemName() && this.itemName.equals(that.itemName) : !that.hasItemName())
                && (this.hasEnchants() ? that.hasEnchants() && this.enchantments.equals(that.enchantments) : !that.hasEnchants())
                && (Objects.equals(this.lore, that.lore))
                && (this.hasCustomModelData() ? that.hasCustomModelData() && this.customModelData.equals(that.customModelData) : !that.hasCustomModelData())
                && (this.hasBlockData() ? that.hasBlockData() && this.blockData.equals(that.blockData) : !that.hasBlockData())
                && (this.hasRepairCost() ? that.hasRepairCost() && this.repairCost == that.repairCost : !that.hasRepairCost())
                && (this.hasAttributeModifiers() ? that.hasAttributeModifiers() && compareModifiers(this.attributeModifiers, that.attributeModifiers) : !that.hasAttributeModifiers())
                && (this.unhandledTags.equals(that.unhandledTags))
                && (this.removedTags.equals(that.removedTags))
                && (Objects.equals(this.customTag, that.customTag))
                && (this.persistentDataContainer.equals(that.persistentDataContainer))
                && (this.hideFlag == that.hideFlag)
                && (this.isHideTooltip() == that.isHideTooltip())
                && (this.isUnbreakable() == that.isUnbreakable())
                && (this.hasEnchantmentGlintOverride() ? that.hasEnchantmentGlintOverride() && this.enchantmentGlintOverride.equals(that.enchantmentGlintOverride) : !that.hasEnchantmentGlintOverride())
                && (this.fireResistant == that.fireResistant)
                && (this.hasMaxStackSize() ? that.hasMaxStackSize() && this.maxStackSize.equals(that.maxStackSize) : !that.hasMaxStackSize())
                && (this.rarity == that.rarity)
                && (this.hasFood() ? that.hasFood() && this.food.equals(that.food) : !that.hasFood())
                && (this.hasTool() ? that.hasTool() && this.tool.equals(that.tool) : !that.hasTool())
                && (this.hasJukeboxPlayable() ? that.hasJukeboxPlayable() && this.jukebox.equals(that.jukebox) : !that.hasJukeboxPlayable())
                && (this.hasDamage() ? that.hasDamage() && this.damage == that.damage : !that.hasDamage())
                && (this.hasMaxDamage() ? that.hasMaxDamage() && this.maxDamage.equals(that.maxDamage) : !that.hasMaxDamage())
                && (this.version == that.version);
    }

    /**
     * This method is a bit weird...
     * Return true if you are a common class OR your uncommon parts are empty.
     * Empty uncommon parts implies the NBT data would be equivalent if both were applied to an item
     */
    @Overridden
    boolean notUncommon(CraftMetaItem meta) {
        return true;
    }

    @Override
    public final int hashCode() {
        return applyHash();
    }

    @Overridden
    int applyHash() {
        int hash = 3;
        hash = 61 * hash + (hasDisplayName() ? this.displayName.hashCode() : 0);
        hash = 61 * hash + (hasItemName() ? this.itemName.hashCode() : 0);
        hash = 61 * hash + ((lore != null) ? this.lore.hashCode() : 0);
        hash = 61 * hash + (hasCustomModelData() ? this.customModelData.hashCode() : 0);
        hash = 61 * hash + (hasBlockData() ? this.blockData.hashCode() : 0);
        hash = 61 * hash + (hasEnchants() ? this.enchantments.hashCode() : 0);
        hash = 61 * hash + (hasRepairCost() ? this.repairCost : 0);
        hash = 61 * hash + unhandledTags.hashCode();
        hash = 61 * hash + removedTags.hashCode();
        hash = 61 * hash + ((customTag != null) ? this.customTag.hashCode() : 0);
        hash = 61 * hash + (!persistentDataContainer.isEmpty() ? persistentDataContainer.hashCode() : 0);
        hash = 61 * hash + hideFlag;
        hash = 61 * hash + (isHideTooltip() ? 1231 : 1237);
        hash = 61 * hash + (isUnbreakable() ? 1231 : 1237);
        hash = 61 * hash + (hasEnchantmentGlintOverride() ? this.enchantmentGlintOverride.hashCode() : 0);
        hash = 61 * hash + (isFireResistant() ? 1231 : 1237);
        hash = 61 * hash + (hasMaxStackSize() ? this.maxStackSize.hashCode() : 0);
        hash = 61 * hash + (hasRarity() ? this.rarity.hashCode() : 0);
        hash = 61 * hash + (hasFood() ? this.food.hashCode() : 0);
        hash = 61 * hash + (hasTool() ? this.tool.hashCode() : 0);
        hash = 61 * hash + (hasJukeboxPlayable() ? this.jukebox.hashCode() : 0);
        hash = 61 * hash + (hasDamage() ? this.damage : 0);
        hash = 61 * hash + (hasMaxDamage() ? 1231 : 1237);
        hash = 61 * hash + (hasAttributeModifiers() ? this.attributeModifiers.hashCode() : 0);
        hash = 61 * hash + version;
        return hash;
    }

    @Overridden
    @Override
    public CraftMetaItem clone() {
        try {
            CraftMetaItem clone = (CraftMetaItem) super.clone();
            if (this.lore != null) {
                clone.lore = new ArrayList<net.minecraft.network.chat.Component>(this.lore);
            }
            clone.customModelData = this.customModelData;
            clone.blockData = this.blockData;
            if (this.enchantments != null) {
                clone.enchantments = new LinkedHashMap<Enchantment, Integer>(this.enchantments);
            }
            if (this.hasAttributeModifiers()) {
                clone.attributeModifiers = LinkedHashMultimap.create(this.attributeModifiers);
            }
            if (this.customTag != null) {
                clone.customTag = this.customTag.copy();
            }
            clone.removedTags = Sets.newHashSet(this.removedTags);
            clone.persistentDataContainer = new CraftPersistentDataContainer(this.persistentDataContainer.getRaw(), DATA_TYPE_REGISTRY);
            clone.hideFlag = this.hideFlag;
            clone.hideTooltip = this.hideTooltip;
            clone.unbreakable = this.unbreakable;
            clone.enchantmentGlintOverride = this.enchantmentGlintOverride;
            clone.fireResistant = fireResistant;
            clone.maxStackSize = maxStackSize;
            clone.rarity = rarity;
            if (this.hasFood()) {
                clone.food = new CraftFoodComponent(food);
            }
            if (this.hasTool()) {
                clone.tool = new CraftToolComponent(tool);
            }
            if (this.hasJukeboxPlayable()) {
                clone.jukebox = new CraftJukeboxComponent(jukebox);
            }
            clone.damage = this.damage;
            clone.maxDamage = this.maxDamage;
            clone.version = this.version;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    @Override
    public final Map<String, Object> serialize() {
        ImmutableMap.Builder<String, Object> map = ImmutableMap.builder();
        map.put(SerializableMeta.TYPE_FIELD, SerializableMeta.classMap.get(getClass()));
        serialize(map);
        return map.build();
    }

    @Overridden
    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        if (hasDisplayName()) {
            builder.put(NAME.BUKKIT, CraftChatMessage.toJSON(displayName));
        }

        if (hasItemName()) {
            builder.put(ITEM_NAME.BUKKIT, CraftChatMessage.toJSON(itemName));
        }

        if (hasLore()) {
            // SPIGOT-7625: Convert lore to json before serializing it
            List<String> jsonLore = new ArrayList<>();

            for (net.minecraft.network.chat.Component component : lore) {
                jsonLore.add(CraftChatMessage.toJSON(component));
            }

            builder.put(LORE.BUKKIT, jsonLore);
        }

        if (hasCustomModelData()) {
            builder.put(CUSTOM_MODEL_DATA.BUKKIT, customModelData);
        }
        if (hasBlockData()) {
            builder.put(BLOCK_DATA.BUKKIT, blockData);
        }

        serializeEnchantments(enchantments, builder, ENCHANTMENTS);
        serializeModifiers(attributeModifiers, builder, ATTRIBUTES);

        if (hasRepairCost()) {
            builder.put(REPAIR.BUKKIT, repairCost);
        }

        List<String> hideFlags = new ArrayList<String>();
        for (ItemFlag hideFlagEnum : getItemFlags()) {
            hideFlags.add(CraftItemFlag.bukkitToString(hideFlagEnum));
        }
        if (!hideFlags.isEmpty()) {
            builder.put(HIDEFLAGS.BUKKIT, hideFlags);
        }

        if (isHideTooltip()) {
            builder.put(HIDE_TOOLTIP.BUKKIT, hideTooltip);
        }

        if (isUnbreakable()) {
            builder.put(UNBREAKABLE.BUKKIT, unbreakable);
        }

        if (hasEnchantmentGlintOverride()) {
            builder.put(ENCHANTMENT_GLINT_OVERRIDE.BUKKIT, enchantmentGlintOverride);
        }

        if (isFireResistant()) {
            builder.put(FIRE_RESISTANT.BUKKIT, fireResistant);
        }

        if (hasMaxStackSize()) {
            builder.put(MAX_STACK_SIZE.BUKKIT, maxStackSize);
        }

        if (hasRarity()) {
            builder.put(RARITY.BUKKIT, rarity.name());
        }

        if (hasFood()) {
            builder.put(FOOD.BUKKIT, food);
        }

        if (hasTool()) {
            builder.put(TOOL.BUKKIT, tool);
        }

        if (hasJukeboxPlayable()) {
            builder.put(JUKEBOX_PLAYABLE.BUKKIT, jukebox);
        }

        if (hasDamage()) {
            builder.put(DAMAGE.BUKKIT, damage);
        }

        if (hasMaxDamage()) {
            builder.put(MAX_DAMAGE.BUKKIT, maxDamage);
        }

        final Map<String, net.minecraft.nbt.Tag> internalTags = new HashMap<String, net.minecraft.nbt.Tag>();
        serializeInternal(internalTags);
        if (!internalTags.isEmpty()) {
            net.minecraft.nbt.CompoundTag internal = new net.minecraft.nbt.CompoundTag();
            for (Map.Entry<String, net.minecraft.nbt.Tag> e : internalTags.entrySet()) {
                internal.put(e.getKey(), e.getValue());
            }
            try {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                net.minecraft.nbt.NbtIo.writeCompressed(internal, buf);
                builder.put("internal", Base64.getEncoder().encodeToString(buf.toByteArray()));
            } catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!unhandledTags.isEmpty()) {
            net.minecraft.nbt.Tag unhandled = net.minecraft.core.component.DataComponentPatch.CODEC.encodeStart(EthyleneStatic.getDefaultRegistryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), unhandledTags.build()).getOrThrow(IllegalStateException::new);
            try {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                net.minecraft.nbt.NbtIo.writeCompressed((net.minecraft.nbt.CompoundTag) unhandled, buf);
                builder.put("unhandled", Base64.getEncoder().encodeToString(buf.toByteArray()));
            } catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!this.removedTags.isEmpty()) {
            net.minecraft.core.RegistryAccess registryAccess = CraftRegistry.getMinecraftRegistry();
            net.minecraft.core.Registry<net.minecraft.core.component.DataComponentType<?>> componentTypeRegistry = registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE);

            List<String> removedTags = new ArrayList<>();
            for (net.minecraft.core.component.DataComponentType<?> removed : this.removedTags) {
                String componentKey = componentTypeRegistry.getResourceKey(removed).orElseThrow().location().toString();

                removedTags.add(componentKey);
            }

            builder.put("removed", removedTags);
        }

        if (!persistentDataContainer.isEmpty()) { // Store custom tags, wrapped in their compound
            builder.put(BUKKIT_CUSTOM_TAG.BUKKIT, persistentDataContainer.serialize());
        }

        if (customTag != null) {
            try {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                net.minecraft.nbt.NbtIo.writeCompressed(customTag, buf);
                builder.put("custom", Base64.getEncoder().encodeToString(buf.toByteArray()));
            } catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return builder;
    }

    void serializeInternal(final Map<String, net.minecraft.nbt.Tag> unhandledTags) {
    }

    static void serializeEnchantments(Map<Enchantment, Integer> enchantments, ImmutableMap.Builder<String, Object> builder, ItemMetaKey key) {
        if (enchantments == null || enchantments.isEmpty()) {
            return;
        }

        ImmutableMap.Builder<String, Integer> enchants = ImmutableMap.builder();
        for (Map.Entry<? extends Enchantment, Integer> enchant : enchantments.entrySet()) {
            enchants.put(CraftEnchantment.bukkitToString(enchant.getKey()), enchant.getValue());
        }

        builder.put(key.BUKKIT, enchants.build());
    }

    static void serializeModifiers(Multimap<Attribute, AttributeModifier> modifiers, ImmutableMap.Builder<String, Object> builder, ItemMetaKey key) {
        if (modifiers == null || modifiers.isEmpty()) {
            return;
        }

        Map<String, List<Object>> mods = new LinkedHashMap<>();
        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
            if (entry.getKey() == null) {
                continue;
            }
            Collection<AttributeModifier> modCollection = modifiers.get(entry.getKey());
            if (modCollection == null || modCollection.isEmpty()) {
                continue;
            }
            mods.put(CraftAttribute.bukkitToString(entry.getKey()), new ArrayList<>(modCollection));
        }
        builder.put(key.BUKKIT, mods);
    }

    static void safelyAdd(Iterable<?> addFrom, Collection<net.minecraft.network.chat.Component> addTo, boolean possiblyJsonInput) {
        if (addFrom == null) {
            return;
        }

        for (Object object : addFrom) {
            if (!(object instanceof String)) {
                if (object != null) {
                    // SPIGOT-7399: Null check via if is important,
                    // otherwise object.getClass().getName() could throw an error for a valid argument -> when it is null which is valid,
                    // when using Preconditions
                    throw new IllegalArgumentException(addFrom + " cannot contain non-string " + object.getClass().getName());
                }

                addTo.add(net.minecraft.network.chat.Component.empty());
            } else {
                String entry = object.toString();
                net.minecraft.network.chat.Component component = (possiblyJsonInput) ? CraftChatMessage.fromJSONOrString(entry) : CraftChatMessage.fromStringOrNull(entry);

                if (component != null) {
                    addTo.add(component);
                } else {
                    addTo.add(net.minecraft.network.chat.Component.empty());
                }
            }
        }
    }

    static boolean checkConflictingEnchants(Map<Enchantment, Integer> enchantments, Enchantment ench) {
        if (enchantments == null || enchantments.isEmpty()) {
            return false;
        }

        for (Enchantment enchant : enchantments.keySet()) {
            if (enchant.conflictsWith(ench)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public final String toString() {
        return SerializableMeta.classMap.get(getClass()) + "_META:" + serialize(); // TODO: cry
    }

    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    public static Set<net.minecraft.core.component.DataComponentType> getHandledTags() {
        synchronized (HANDLED_TAGS) {
            if (HANDLED_TAGS.isEmpty()) {
                HANDLED_TAGS.addAll(Arrays.asList(
                        NAME.TYPE,
                        ITEM_NAME.TYPE,
                        LORE.TYPE,
                        CUSTOM_MODEL_DATA.TYPE,
                        BLOCK_DATA.TYPE,
                        REPAIR.TYPE,
                        ENCHANTMENTS.TYPE,
                        HIDE_ADDITIONAL_TOOLTIP.TYPE,
                        HIDE_TOOLTIP.TYPE,
                        UNBREAKABLE.TYPE,
                        ENCHANTMENT_GLINT_OVERRIDE.TYPE,
                        FIRE_RESISTANT.TYPE,
                        MAX_STACK_SIZE.TYPE,
                        RARITY.TYPE,
                        FOOD.TYPE,
                        TOOL.TYPE,
                        JUKEBOX_PLAYABLE.TYPE,
                        DAMAGE.TYPE,
                        MAX_DAMAGE.TYPE,
                        CUSTOM_DATA.TYPE,
                        ATTRIBUTES.TYPE,
                        CraftMetaArmor.TRIM.TYPE,
                        CraftMetaArmorStand.ENTITY_TAG.TYPE,
                        CraftMetaBanner.PATTERNS.TYPE,
                        CraftMetaEntityTag.ENTITY_TAG.TYPE,
                        CraftMetaLeatherArmor.COLOR.TYPE,
                        CraftMetaMap.MAP_POST_PROCESSING.TYPE,
                        CraftMetaMap.MAP_COLOR.TYPE,
                        CraftMetaMap.MAP_ID.TYPE,
                        CraftMetaPotion.POTION_CONTENTS.TYPE,
                        CraftMetaShield.BASE_COLOR.TYPE,
                        CraftMetaSkull.SKULL_PROFILE.TYPE,
                        CraftMetaSkull.NOTE_BLOCK_SOUND.TYPE,
                        CraftMetaSpawnEgg.ENTITY_TAG.TYPE,
                        CraftMetaBlockState.BLOCK_ENTITY_TAG.TYPE,
                        CraftMetaBook.BOOK_CONTENT.TYPE,
                        CraftMetaBookSigned.BOOK_CONTENT.TYPE,
                        CraftMetaFirework.FIREWORKS.TYPE,
                        CraftMetaEnchantedBook.STORED_ENCHANTMENTS.TYPE,
                        CraftMetaCharge.EXPLOSION.TYPE,
                        CraftMetaKnowledgeBook.BOOK_RECIPES.TYPE,
                        CraftMetaTropicalFishBucket.ENTITY_TAG.TYPE,
                        CraftMetaTropicalFishBucket.BUCKET_ENTITY_TAG.TYPE,
                        CraftMetaAxolotlBucket.ENTITY_TAG.TYPE,
                        CraftMetaAxolotlBucket.BUCKET_ENTITY_TAG.TYPE,
                        CraftMetaCrossbow.CHARGED_PROJECTILES.TYPE,
                        CraftMetaSuspiciousStew.EFFECTS.TYPE,
                        CraftMetaCompass.LODESTONE_TARGET.TYPE,
                        CraftMetaBundle.ITEMS.TYPE,
                        CraftMetaMusicInstrument.GOAT_HORN_INSTRUMENT.TYPE,
                        CraftMetaOminousBottle.OMINOUS_BOTTLE_AMPLIFIER.TYPE
                ));
            }
            return HANDLED_TAGS;
        }
    }

    protected static <T> Optional<? extends T> getOrEmpty(net.minecraft.core.component.DataComponentPatch tag, ItemMetaKeyType<T> type) {
        Optional<? extends T> result = tag.get(type.TYPE);

        return (result != null) ? result : Optional.empty();
    }
}
