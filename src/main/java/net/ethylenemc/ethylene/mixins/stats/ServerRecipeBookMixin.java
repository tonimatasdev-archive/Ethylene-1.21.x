package net.ethylenemc.ethylene.mixins.stats;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@SuppressWarnings("ConstantValue")
@Mixin(ServerRecipeBook.class)
public class ServerRecipeBookMixin {
    @ModifyExpressionValue(method = "addRecipes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Recipe;isSpecial()Z"))
    private boolean ethylene$addRecipes(boolean original, Collection<RecipeHolder<?>> collection, ServerPlayer serverPlayer, @Local ResourceKey<Recipe<?>> resourceKey) {
        return original && CraftEventFactory.handlePlayerRecipeListUpdateEvent(serverPlayer, resourceKey.location());
    }

    @ModifyExpressionValue(method = "addRecipes", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    private boolean ethylene$addRecipes$1(boolean original, Collection<RecipeHolder<?>> collection, ServerPlayer serverPlayer) {
        return original && serverPlayer.connection != null;
    }

    @ModifyExpressionValue(method = "removeRecipes", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    private boolean ethylene$removeRecipes(boolean original, Collection<RecipeHolder<?>> collection, ServerPlayer serverPlayer) {
        return original && serverPlayer.connection != null;
    }
}
