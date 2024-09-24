package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

public class CraftBlockType<B extends BlockData> implements BlockType.Typed<B>, Handleable<net.minecraft.world.level.block.Block> {

    private final NamespacedKey key;
    private final net.minecraft.world.level.block.Block block;
    private final Class<B> blockDataClass;
    private final boolean interactable;

    public static Material minecraftToBukkit(net.minecraft.world.level.block.Block block) {
        return CraftMagicNumbers.getMaterial(block);
    }

    public static net.minecraft.world.level.block.Block bukkitToMinecraft(Material material) {
        return CraftMagicNumbers.getBlock(material);
    }

    public static BlockType minecraftToBukkitNew(net.minecraft.world.level.block.Block minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.BLOCK, Registry.BLOCK);
    }

    public static net.minecraft.world.level.block.Block bukkitToMinecraftNew(BlockType bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    private static boolean hasMethod(Class<?> clazz, Class<?>... params) {
        boolean hasMethod = false;
        for (Method method : clazz.getDeclaredMethods()) {
            if (Arrays.equals(method.getParameterTypes(), params)) {
                Preconditions.checkArgument(!hasMethod, "More than one matching method for %s, args %s", clazz, Arrays.toString(params));

                hasMethod = true;
            }
        }

        return hasMethod;
    }

    private static final Class<?>[] USE_WITHOUT_ITEM_ARGS = new Class[]{
        net.minecraft.world.level.block.state.BlockState.class, net.minecraft.world.level.Level.class, net.minecraft.core.BlockPos.class, net.minecraft.world.entity.player.Player.class, net.minecraft.world.phys.BlockHitResult.class
    };
    private static final Class<?>[] USE_ITEM_ON_ARGS = new Class[]{
        net.minecraft.world.item.ItemStack.class, net.minecraft.world.level.block.state.BlockState.class, net.minecraft.world.level.Level.class, net.minecraft.core.BlockPos.class, net.minecraft.world.entity.player.Player.class, net.minecraft.world.InteractionHand.class, net.minecraft.world.phys.BlockHitResult.class
    };

    private static boolean isInteractable(net.minecraft.world.level.block.Block block) {
        Class<?> clazz = block.getClass();

        boolean hasMethod = hasMethod(clazz, USE_WITHOUT_ITEM_ARGS) || hasMethod(clazz, USE_ITEM_ON_ARGS);

        if (!hasMethod && clazz.getSuperclass() != net.minecraft.world.level.block.state.BlockBehaviour.class) {
            clazz = clazz.getSuperclass();

            hasMethod = hasMethod(clazz, USE_WITHOUT_ITEM_ARGS) || hasMethod(clazz, USE_ITEM_ON_ARGS);
        }

        return hasMethod;
    }

    public CraftBlockType(NamespacedKey key, net.minecraft.world.level.block.Block block) {
        this.key = key;
        this.block = block;
        this.blockDataClass = (Class<B>) CraftBlockData.fromData(block.defaultBlockState()).getClass().getInterfaces()[0];
        this.interactable = isInteractable(block);
    }

    @Override
    public net.minecraft.world.level.block.Block getHandle() {
        return block;
    }

    @NotNull
    @Override
    public Typed<BlockData> typed() {
        return this.typed(BlockData.class);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <Other extends BlockData> Typed<Other> typed(@NotNull Class<Other> blockDataType) {
        if (blockDataType.isAssignableFrom(this.blockDataClass)) return (Typed<Other>) this;
        throw new IllegalArgumentException("Cannot type block type " + this.key.toString() + " to blockdata type " + blockDataType.getSimpleName());
    }

    @Override
    public boolean hasItemType() {
        if (this == AIR) {
            return true;
        }

        return block.asItem() != net.minecraft.world.item.Items.AIR;
    }

    @NotNull
    @Override
    public ItemType getItemType() {
        if (this == AIR) {
            return ItemType.AIR;
        }

        net.minecraft.world.item.Item item = block.asItem();
        Preconditions.checkArgument(item != net.minecraft.world.item.Items.AIR, "The block type %s has no corresponding item type", getKey());
        return CraftItemType.minecraftToBukkitNew(item);
    }

    @Override
    public Class<B> getBlockDataClass() {
        return blockDataClass;
    }

    @Override
    public B createBlockData() {
        return createBlockData((String) null);
    }

    @Override
    public B createBlockData(Consumer<? super B> consumer) {
        B data = createBlockData();

        if (consumer != null) {
            consumer.accept(data);
        }

        return data;
    }

    @Override
    public B createBlockData(String data) {
        return (B) CraftBlockData.newData(this, data);
    }

    @Override
    public boolean isSolid() {
        return block.defaultBlockState().blocksMotion();
    }

    @Override
    public boolean isAir() {
        return block.defaultBlockState().isAir();
    }

    @Override
    public boolean isEnabledByFeature(@NotNull World world) {
        Preconditions.checkNotNull(world, "World cannot be null");
        return getHandle().isEnabled(((CraftWorld) world).getHandle().enabledFeatures());
    }

    @Override
    public boolean isFlammable() {
        return block.defaultBlockState().ignitedByLava();
    }

    @Override
    public boolean isBurnable() {
        return ((net.minecraft.world.level.block.FireBlock) net.minecraft.world.level.block.Blocks.FIRE).igniteOdds.getOrDefault(block, 0) > 0;
    }

    @Override
    public boolean isOccluding() {
        return block.defaultBlockState().isRedstoneConductor(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, net.minecraft.core.BlockPos.ZERO);
    }

    @Override
    public boolean hasGravity() {
        return block instanceof net.minecraft.world.level.block.Fallable;
    }

    @Override
    public boolean isInteractable() {
        return interactable;
    }

    @Override
    public float getHardness() {
        return block.defaultBlockState().destroySpeed;
    }

    @Override
    public float getBlastResistance() {
        return block.getExplosionResistance();
    }

    @Override
    public float getSlipperiness() {
        return block.getFriction();
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return block.getDescriptionId();
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
