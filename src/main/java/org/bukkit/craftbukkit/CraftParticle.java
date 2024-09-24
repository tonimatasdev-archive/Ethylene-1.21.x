package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Vibration;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.legacy.FieldRename;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.joml.Vector3f;

public abstract class CraftParticle<D> implements Keyed {

    private static final Registry<CraftParticle<?>> CRAFT_PARTICLE_REGISTRY = new CraftParticleRegistry(CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.PARTICLE_TYPE));

    public static Particle minecraftToBukkit(net.minecraft.core.particles.ParticleType<?> minecraft) {
        Preconditions.checkArgument(minecraft != null);

        net.minecraft.core.Registry<net.minecraft.core.particles.ParticleType<?>> registry = CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.PARTICLE_TYPE);
        Particle bukkit = Registry.PARTICLE_TYPE.get(CraftNamespacedKey.fromMinecraft(registry.getResourceKey(minecraft).orElseThrow().location()));

        Preconditions.checkArgument(bukkit != null);

        return bukkit;
    }

    public static net.minecraft.core.particles.ParticleType<?> bukkitToMinecraft(Particle bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.PARTICLE_TYPE)
                .getOptional(CraftNamespacedKey.toMinecraft(bukkit.getKey())).orElseThrow();
    }

    public static <D> net.minecraft.core.particles.ParticleOptions createParticleParam(Particle particle, D data) {
        Preconditions.checkArgument(particle != null, "particle cannot be null");

        data = CraftParticle.convertLegacy(data);
        if (particle.getDataType() != Void.class) {
            Preconditions.checkArgument(data != null, "missing required data %s", particle.getDataType());
        }
        if (data != null) {
            Preconditions.checkArgument(particle.getDataType().isInstance(data), "data (%s) should be %s", data.getClass(), particle.getDataType());
        }

        CraftParticle<D> craftParticle = (CraftParticle<D>) CRAFT_PARTICLE_REGISTRY.get(particle.getKey());

        Preconditions.checkArgument(craftParticle != null);

        return craftParticle.createParticleParam(data);
    }

    public static <T> T convertLegacy(T object) {
        if (object instanceof MaterialData mat) {
            return (T) CraftBlockData.fromData(CraftMagicNumbers.getBlock(mat));
        }

        return object;
    }

    private final NamespacedKey key;
    private final net.minecraft.core.particles.ParticleType<?> particle;
    private final Class<D> clazz;

    public CraftParticle(NamespacedKey key, net.minecraft.core.particles.ParticleType<?> particle, Class<D> clazz) {
        this.key = key;
        this.particle = particle;
        this.clazz = clazz;
    }

    public net.minecraft.core.particles.ParticleType<?> getHandle() {
        return particle;
    }

    public abstract net.minecraft.core.particles.ParticleOptions createParticleParam(D data);

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    public static class CraftParticleRegistry extends CraftRegistry<CraftParticle<?>, net.minecraft.core.particles.ParticleType<?>> {

        private static final Map<NamespacedKey, BiFunction<NamespacedKey, net.minecraft.core.particles.ParticleType<?>, CraftParticle<?>>> PARTICLE_MAP = new HashMap<>();

        private static final BiFunction<NamespacedKey, net.minecraft.core.particles.ParticleType<?>, CraftParticle<?>> VOID_FUNCTION = (name, particle) -> new CraftParticle<>(name, particle, Void.class) {
            @Override
            public net.minecraft.core.particles.ParticleOptions createParticleParam(Void data) {
                return (net.minecraft.core.particles.SimpleParticleType) getHandle();
            }
        };

        static {
            BiFunction<NamespacedKey, net.minecraft.core.particles.ParticleType<?>, CraftParticle<?>> dustOptionsFunction = (name, particle) -> new CraftParticle<>(name, particle, Particle.DustOptions.class) {
                @Override
                public net.minecraft.core.particles.ParticleOptions createParticleParam(Particle.DustOptions data) {
                    Color color = data.getColor();
                    return new net.minecraft.core.particles.DustParticleOptions(new Vector3f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f), data.getSize());
                }
            };

            BiFunction<NamespacedKey, net.minecraft.core.particles.ParticleType<?>, CraftParticle<?>> itemStackFunction = (name, particle) -> new CraftParticle<>(name, particle, ItemStack.class) {
                @Override
                public net.minecraft.core.particles.ParticleOptions createParticleParam(ItemStack data) {
                    return new net.minecraft.core.particles.ItemParticleOption((net.minecraft.core.particles.ParticleType<net.minecraft.core.particles.ItemParticleOption>) getHandle(), CraftItemStack.asNMSCopy(data));
                }
            };

            BiFunction<NamespacedKey, net.minecraft.core.particles.ParticleType<?>, CraftParticle<?>> blockDataFunction = (name, particle) -> new CraftParticle<>(name, particle, BlockData.class) {
                @Override
                public net.minecraft.core.particles.ParticleOptions createParticleParam(BlockData data) {
                    return new net.minecraft.core.particles.BlockParticleOption((net.minecraft.core.particles.ParticleType<net.minecraft.core.particles.BlockParticleOption>) getHandle(), ((CraftBlockData) data).getState());
                }
            };

            BiFunction<NamespacedKey, net.minecraft.core.particles.ParticleType<?>, CraftParticle<?>> dustTransitionFunction = (name, particle) -> new CraftParticle<>(name, particle, Particle.DustTransition.class) {
                @Override
                public net.minecraft.core.particles.ParticleOptions createParticleParam(Particle.DustTransition data) {
                    Color from = data.getColor();
                    Color to = data.getToColor();
                    return new net.minecraft.core.particles.DustColorTransitionOptions(new Vector3f(from.getRed() / 255.0f, from.getGreen() / 255.0f, from.getBlue() / 255.0f), new Vector3f(to.getRed() / 255.0f, to.getGreen() / 255.0f, to.getBlue() / 255.0f), data.getSize());
                }
            };

            BiFunction<NamespacedKey, net.minecraft.core.particles.ParticleType<?>, CraftParticle<?>> vibrationFunction = (name, particle) -> new CraftParticle<>(name, particle, Vibration.class) {
                @Override
                public net.minecraft.core.particles.ParticleOptions createParticleParam(Vibration data) {
                    net.minecraft.world.level.gameevent.PositionSource source;
                    if (data.getDestination() instanceof Vibration.Destination.BlockDestination) {
                        Location destination = ((Vibration.Destination.BlockDestination) data.getDestination()).getLocation();
                        source = new net.minecraft.world.level.gameevent.BlockPositionSource(CraftLocation.toBlockPosition(destination));
                    } else if (data.getDestination() instanceof Vibration.Destination.EntityDestination) {
                        net.minecraft.world.entity.Entity destination = ((CraftEntity) ((Vibration.Destination.EntityDestination) data.getDestination()).getEntity()).getHandle();
                        source = new net.minecraft.world.level.gameevent.EntityPositionSource(destination, destination.getEyeHeight());
                    } else {
                        throw new IllegalArgumentException("Unknown vibration destination " + data.getDestination());
                    }

                    return new net.minecraft.core.particles.VibrationParticleOption(source, data.getArrivalTime());
                }
            };

            BiFunction<NamespacedKey, net.minecraft.core.particles.ParticleType<?>, CraftParticle<?>> floatFunction = (name, particle) -> new CraftParticle<>(name, particle, Float.class) {
                @Override
                public net.minecraft.core.particles.ParticleOptions createParticleParam(Float data) {
                    return new net.minecraft.core.particles.SculkChargeParticleOptions(data);
                }
            };

            BiFunction<NamespacedKey, net.minecraft.core.particles.ParticleType<?>, CraftParticle<?>> integerFunction = (name, particle) -> new CraftParticle<>(name, particle, Integer.class) {
                @Override
                public net.minecraft.core.particles.ParticleOptions createParticleParam(Integer data) {
                    return new net.minecraft.core.particles.ShriekParticleOption(data);
                }
            };

            BiFunction<NamespacedKey, net.minecraft.core.particles.ParticleType<?>, CraftParticle<?>> colorFunction = (name, particle) -> new CraftParticle<>(name, particle, Color.class) {
                @Override
                public net.minecraft.core.particles.ParticleOptions createParticleParam(Color color) {
                    return net.minecraft.core.particles.ColorParticleOption.create((net.minecraft.core.particles.ParticleType<net.minecraft.core.particles.ColorParticleOption>) particle, color.asARGB());
                }
            };

            add("dust", dustOptionsFunction);
            add("item", itemStackFunction);
            add("block", blockDataFunction);
            add("falling_dust", blockDataFunction);
            add("dust_color_transition", dustTransitionFunction);
            add("vibration", vibrationFunction);
            add("sculk_charge", floatFunction);
            add("shriek", integerFunction);
            add("block_marker", blockDataFunction);
            add("entity_effect", colorFunction);
            add("dust_pillar", blockDataFunction);
        }

        private static void add(String name, BiFunction<NamespacedKey, net.minecraft.core.particles.ParticleType<?>, CraftParticle<?>> function) {
            PARTICLE_MAP.put(NamespacedKey.fromString(name), function);
        }

        public CraftParticleRegistry(net.minecraft.core.Registry<net.minecraft.core.particles.ParticleType<?>> minecraftRegistry) {
            super(CraftParticle.class, minecraftRegistry, null, FieldRename.PARTICLE_TYPE_RENAME);
        }

        @Override
        public CraftParticle<?> createBukkit(NamespacedKey namespacedKey, net.minecraft.core.particles.ParticleType<?> particle) {
            if (particle == null) {
                return null;
            }

            BiFunction<NamespacedKey, net.minecraft.core.particles.ParticleType<?>, CraftParticle<?>> function = PARTICLE_MAP.getOrDefault(namespacedKey, VOID_FUNCTION);

            return function.apply(namespacedKey, particle);
        }
    }
}
