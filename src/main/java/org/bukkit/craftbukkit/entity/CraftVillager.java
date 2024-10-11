package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import java.util.Locale;

import net.ethylenemc.EthyleneStatic;
import net.ethylenemc.interfaces.world.entity.EthyleneEntity;
import net.ethylenemc.interfaces.world.level.EthyleneWorldGenLevel;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CraftVillager extends CraftAbstractVillager implements Villager {

    public CraftVillager(CraftServer server, net.minecraft.world.entity.npc.Villager entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.npc.Villager getHandle() {
        return (net.minecraft.world.entity.npc.Villager) entity;
    }

    @Override
    public String toString() {
        return "CraftVillager";
    }

    @Override
    public void remove() {
        getHandle().releaseAllPois();

        super.remove();
    }

    @Override
    public Profession getProfession() {
        return CraftProfession.minecraftToBukkit(getHandle().getVillagerData().getProfession());
    }

    @Override
    public void setProfession(Profession profession) {
        Preconditions.checkArgument(profession != null, "Profession cannot be null");
        getHandle().setVillagerData(getHandle().getVillagerData().setProfession(CraftProfession.bukkitToMinecraft(profession)));
    }

    @Override
    public Type getVillagerType() {
        return CraftType.minecraftToBukkit(getHandle().getVillagerData().getType());
    }

    @Override
    public void setVillagerType(Type type) {
        Preconditions.checkArgument(type != null, "Type cannot be null");
        getHandle().setVillagerData(getHandle().getVillagerData().setType(CraftType.bukkitToMinecraft(type)));
    }

    @Override
    public int getVillagerLevel() {
        return getHandle().getVillagerData().getLevel();
    }

    @Override
    public void setVillagerLevel(int level) {
        Preconditions.checkArgument(1 <= level && level <= 5, "level (%s) must be between [1, 5]", level);

        getHandle().setVillagerData(getHandle().getVillagerData().setLevel(level));
    }

    @Override
    public int getVillagerExperience() {
        return getHandle().getVillagerXp();
    }

    @Override
    public void setVillagerExperience(int experience) {
        Preconditions.checkArgument(experience >= 0, "Experience (%s) must be positive", experience);

        getHandle().setVillagerXp(experience);
    }

    @Override
    public boolean sleep(Location location) {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "Location needs to be in a world");
        Preconditions.checkArgument(location.getWorld().equals(getWorld()), "Cannot sleep across worlds");
        Preconditions.checkState(!((EthyleneEntity) getHandle()).getGeneration(), "Cannot sleep during world generation");

        net.minecraft.core.BlockPos position = CraftLocation.toBlockPosition(location);
        net.minecraft.world.level.block.state.BlockState iblockdata = getHandle().level().getBlockState(position);
        if (!(iblockdata.getBlock() instanceof net.minecraft.world.level.block.BedBlock)) {
            return false;
        }

        getHandle().startSleeping(position);
        return true;
    }

    @Override
    public void wakeup() {
        Preconditions.checkState(isSleeping(), "Cannot wakeup if not sleeping");
        Preconditions.checkState(!((EthyleneEntity) getHandle()).getGeneration(), "Cannot wakeup during world generation");

        getHandle().stopSleeping();
    }

    @Override
    public void shakeHead() {
        getHandle().setUnhappy();
    }

    @Override
    public ZombieVillager zombify() {
        net.minecraft.world.entity.monster.ZombieVillager entityzombievillager = EthyleneStatic.zombifyVillager(((EthyleneWorldGenLevel) getHandle().level()).getMinecraftWorld(), getHandle(), getHandle().blockPosition(), isSilent(), CreatureSpawnEvent.SpawnReason.CUSTOM);
        return (entityzombievillager != null) ? (ZombieVillager) ((EthyleneEntity) entityzombievillager).getBukkitEntity() : null;
    }

    public static class CraftType implements Type, Handleable<net.minecraft.world.entity.npc.VillagerType> {
        private static int count = 0;

        public static Type minecraftToBukkit(net.minecraft.world.entity.npc.VillagerType minecraft) {
            return CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.VILLAGER_TYPE, Registry.VILLAGER_TYPE);
        }

        public static net.minecraft.world.entity.npc.VillagerType bukkitToMinecraft(Type bukkit) {
            return CraftRegistry.bukkitToMinecraft(bukkit);
        }

        private final NamespacedKey key;
        private final net.minecraft.world.entity.npc.VillagerType villagerType;
        private final String name;
        private final int ordinal;

        public CraftType(NamespacedKey key, net.minecraft.world.entity.npc.VillagerType villagerType) {
            this.key = key;
            this.villagerType = villagerType;
            // For backwards compatibility, minecraft values will still return the uppercase name without the namespace,
            // in case plugins use for example the name as key in a config file to receive type specific values.
            // Custom types will return the key with namespace. For a plugin this should look than like a new type
            // (which can always be added in new minecraft versions and the plugin should therefore handle it accordingly).
            if (NamespacedKey.MINECRAFT.equals(key.getNamespace())) {
                this.name = key.getKey().toUpperCase(Locale.ROOT);
            } else {
                this.name = key.toString();
            }
            this.ordinal = count++;
        }

        @Override
        public net.minecraft.world.entity.npc.VillagerType getHandle() {
            return villagerType;
        }

        @Override
        public NamespacedKey getKey() {
            return key;
        }

        @Override
        public int compareTo(Type type) {
            return ordinal - type.ordinal();
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int ordinal() {
            return ordinal;
        }

        @Override
        public String toString() {
            // For backwards compatibility
            return name();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof CraftType)) {
                return false;
            }

            return getKey().equals(((Type) other).getKey());
        }

        @Override
        public int hashCode() {
            return getKey().hashCode();
        }
    }

    public static class CraftProfession implements Profession, Handleable<net.minecraft.world.entity.npc.VillagerProfession> {
        private static int count = 0;

        public static Profession minecraftToBukkit(net.minecraft.world.entity.npc.VillagerProfession minecraft) {
            return CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.VILLAGER_PROFESSION, Registry.VILLAGER_PROFESSION);
        }

        public static net.minecraft.world.entity.npc.VillagerProfession bukkitToMinecraft(Profession bukkit) {
            return CraftRegistry.bukkitToMinecraft(bukkit);
        }

        private final NamespacedKey key;
        private final net.minecraft.world.entity.npc.VillagerProfession villagerProfession;
        private final String name;
        private final int ordinal;

        public CraftProfession(NamespacedKey key, net.minecraft.world.entity.npc.VillagerProfession villagerProfession) {
            this.key = key;
            this.villagerProfession = villagerProfession;
            // For backwards compatibility, minecraft values will still return the uppercase name without the namespace,
            // in case plugins use for example the name as key in a config file to receive profession specific values.
            // Custom professions will return the key with namespace. For a plugin this should look than like a new profession
            // (which can always be added in new minecraft versions and the plugin should therefore handle it accordingly).
            if (NamespacedKey.MINECRAFT.equals(key.getNamespace())) {
                this.name = key.getKey().toUpperCase(Locale.ROOT);
            } else {
                this.name = key.toString();
            }
            this.ordinal = count++;
        }

        @Override
        public net.minecraft.world.entity.npc.VillagerProfession getHandle() {
            return villagerProfession;
        }

        @Override
        public NamespacedKey getKey() {
            return key;
        }

        @Override
        public int compareTo(Profession profession) {
            return ordinal - profession.ordinal();
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int ordinal() {
            return ordinal;
        }

        @Override
        public String toString() {
            // For backwards compatibility
            return name();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof CraftProfession)) {
                return false;
            }

            return getKey().equals(((Profession) other).getKey());
        }

        @Override
        public int hashCode() {
            return getKey().hashCode();
        }
    }
}
