package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

public class CraftLootTable implements org.bukkit.loot.LootTable {

    public static org.bukkit.loot.LootTable minecraftToBukkit(net.minecraft.resources.ResourceLocation minecraft) {
        return (minecraft == null) ? null : Bukkit.getLootTable(CraftNamespacedKey.fromMinecraft(minecraft));
    }

    public static org.bukkit.loot.LootTable minecraftToBukkit(net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> minecraft) {
        return (minecraft == null) ? null : Bukkit.getLootTable(minecraftToBukkitKey(minecraft));
    }

    public static NamespacedKey minecraftToBukkitKey(net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> minecraft) {
        return (minecraft == null) ? null : CraftNamespacedKey.fromMinecraft(minecraft.location());
    }

    public static net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> bukkitToMinecraft(org.bukkit.loot.LootTable table) {
        return (table == null) ? null : bukkitKeyToMinecraft(table.getKey());
    }

    public static net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> bukkitKeyToMinecraft(NamespacedKey key) {
        return (key == null) ? null : net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, CraftNamespacedKey.toMinecraft(key));
    }

    private final net.minecraft.world.level.storage.loot.LootTable handle;
    private final NamespacedKey key;

    public CraftLootTable(NamespacedKey key, net.minecraft.world.level.storage.loot.LootTable handle) {
        this.handle = handle;
        this.key = key;
    }

    public net.minecraft.world.level.storage.loot.LootTable getHandle() {
        return handle;
    }

    @Override
    public Collection<ItemStack> populateLoot(Random random, LootContext context) {
        Preconditions.checkArgument(context != null, "LootContext cannot be null");
        net.minecraft.world.level.storage.loot.LootParams nmsContext = convertContext(context, random);
        List<net.minecraft.world.item.ItemStack> nmsItems = handle.getRandomItems(nmsContext);
        Collection<ItemStack> bukkit = new ArrayList<>(nmsItems.size());

        for (net.minecraft.world.item.ItemStack item : nmsItems) {
            if (item.isEmpty()) {
                continue;
            }
            bukkit.add(CraftItemStack.asBukkitCopy(item));
        }

        return bukkit;
    }

    @Override
    public void fillInventory(Inventory inventory, Random random, LootContext context) {
        Preconditions.checkArgument(inventory != null, "Inventory cannot be null");
        Preconditions.checkArgument(context != null, "LootContext cannot be null");
        net.minecraft.world.level.storage.loot.LootParams nmsContext = convertContext(context, random);
        CraftInventory craftInventory = (CraftInventory) inventory;
        net.minecraft.world.Container handle = craftInventory.getInventory();

        // TODO: When events are added, call event here w/ custom reason?
        getHandle().fillInventory(handle, nmsContext, random.nextLong(), true);
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    private net.minecraft.world.level.storage.loot.LootParams convertContext(LootContext context, Random random) {
        Preconditions.checkArgument(context != null, "LootContext cannot be null");
        Location loc = context.getLocation();
        Preconditions.checkArgument(loc.getWorld() != null, "LootContext.getLocation#getWorld cannot be null");
        net.minecraft.server.level.ServerLevel handle = ((CraftWorld) loc.getWorld()).getHandle();

        net.minecraft.world.level.storage.loot.LootParams.Builder builder = new net.minecraft.world.level.storage.loot.LootParams.Builder(handle);
        if (random != null) {
            // builder = builder.withRandom(new RandomSourceWrapper(random));
        }
        setMaybe(builder, net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, CraftLocation.toVec3D(loc));
        if (getHandle() != net.minecraft.world.level.storage.loot.LootTable.EMPTY) {
            // builder.luck(context.getLuck());

            if (context.getLootedEntity() != null) {
                net.minecraft.world.entity.Entity nmsLootedEntity = ((CraftEntity) context.getLootedEntity()).getHandle();
                setMaybe(builder, net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, nmsLootedEntity);
                setMaybe(builder, net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE, handle.damageSources().generic());
                setMaybe(builder, net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, nmsLootedEntity.position());
            }

            if (context.getKiller() != null) {
                net.minecraft.world.entity.player.Player nmsKiller = ((CraftHumanEntity) context.getKiller()).getHandle();
                setMaybe(builder, net.minecraft.world.level.storage.loot.parameters.LootContextParams.ATTACKING_ENTITY, nmsKiller);
                // If there is a player killer, damage source should reflect that in case loot tables use that information
                setMaybe(builder, net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE, handle.damageSources().playerAttack(nmsKiller));
                setMaybe(builder, net.minecraft.world.level.storage.loot.parameters.LootContextParams.LAST_DAMAGE_PLAYER, nmsKiller); // SPIGOT-5603 - Set minecraft:killed_by_player
                setMaybe(builder, net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, nmsKiller.getUseItem()); // SPIGOT-6925 - Set minecraft:match_tool
            }
        }

        // SPIGOT-5603 - Avoid IllegalArgumentException in net.minecraft.world.level.storage.loot.LootContext#build()
        net.minecraft.world.level.storage.loot.parameters.LootContextParamSet.Builder nmsBuilder = new net.minecraft.world.level.storage.loot.parameters.LootContextParamSet.Builder();
        for (net.minecraft.world.level.storage.loot.parameters.LootContextParam<?> param : getHandle().getParamSet().getRequired()) {
            nmsBuilder.required(param);
        }
        for (net.minecraft.world.level.storage.loot.parameters.LootContextParam<?> param : getHandle().getParamSet().getAllowed()) {
            if (!getHandle().getParamSet().getRequired().contains(param)) {
                nmsBuilder.optional(param);
            }
        }

        return builder.create(getHandle().getParamSet());
    }

    private <T> void setMaybe(net.minecraft.world.level.storage.loot.LootParams.Builder builder, net.minecraft.world.level.storage.loot.parameters.LootContextParam<T> param, T value) {
        if (getHandle().getParamSet().getRequired().contains(param) || getHandle().getParamSet().getAllowed().contains(param)) {
            builder.withParameter(param, value);
        }
    }

    public static LootContext convertContext(net.minecraft.world.level.storage.loot.LootContext info) {
        net.minecraft.world.phys.Vec3 position = info.getParamOrNull(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN);
        if (position == null) {
            position = info.getParamOrNull(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY).position(); // Every vanilla context has origin or this_entity, see LootContextParameterSets
        }
        Location location = CraftLocation.toBukkit(position, info.getLevel().getWorld());
        LootContext.Builder contextBuilder = new LootContext.Builder(location);

        if (info.hasParam(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ATTACKING_ENTITY)) {
            CraftEntity killer = info.getParamOrNull(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ATTACKING_ENTITY).getBukkitEntity();
            if (killer instanceof CraftHumanEntity) {
                contextBuilder.killer((CraftHumanEntity) killer);
            }
        }

        if (info.hasParam(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY)) {
            contextBuilder.lootedEntity(info.getParamOrNull(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY).getBukkitEntity());
        }

        contextBuilder.luck(info.getLuck());
        return contextBuilder.build();
    }

    @Override
    public String toString() {
        return getKey().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof org.bukkit.loot.LootTable)) {
            return false;
        }

        org.bukkit.loot.LootTable table = (org.bukkit.loot.LootTable) obj;
        return table.getKey().equals(this.getKey());
    }
}
